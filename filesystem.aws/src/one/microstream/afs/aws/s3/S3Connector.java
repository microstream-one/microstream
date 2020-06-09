package one.microstream.afs.aws.s3;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.RequestClientOptions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import one.microstream.X;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.reference.Reference;


public interface S3Connector
{
	public long fileSize(S3Path file);

	public boolean directoryExists(S3Path directory);

	public boolean fileExists(S3Path file);

	public boolean createDirectory(S3Path directory);

	public boolean createFile(S3Path file);

	public boolean deleteFile(S3Path file);

	public ByteBuffer readData(S3Path file, long offset, long length);

	public long readData(S3Path file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(S3Path file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(S3Path sourceFile, S3Path targetFile);

	public long copyFile(S3Path sourceFile, S3Path targetFile);

	public long copyFile(S3Path sourceFile, S3Path targetFile, long offset, long length);



	public static S3Connector New(
		final AmazonS3 s3
	)
	{
		return new Default(
			notNull(s3)
		);
	}


	public static class Default implements S3Connector
	{
		final static String  NUMBER_SUFFIX_SEPARATOR      = "."                    ;
		final static char    NUMBER_SUFFIX_SEPARATOR_CHAR = '.'                    ;
		final static Pattern NUMBER_SUFFIX_PATTERN        = Pattern.compile("\\d+");

		static String toDirectoryKey(
			final S3Path path
		)
		{
			// directories have a trailing /
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(S3Path.SEPARATOR, "", S3Path.SEPARATOR))
			;
		}

		static String toFileKeyPrefix(
			final S3Path path
		)
		{
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(S3Path.SEPARATOR, "", NUMBER_SUFFIX_SEPARATOR))
			;
		}

		static boolean isFileKey(
			final String prefix,
			final String key
		)
		{
			return isFile(key)
				&& key.length() > prefix.length()
				&& key.startsWith(prefix)
				&& key.indexOf(S3Path.SEPARATOR_CAHR, prefix.length()) == -1
				&& NUMBER_SUFFIX_PATTERN.matcher(key.substring(prefix.length())).matches()
			;
		}

		static boolean isDirectory(
			final String key
		)
		{
			return key.endsWith(S3Path.SEPARATOR);
		}

		static boolean isFile(
			final String key
		)
		{
			return !isDirectory(key);
		}


		private final AmazonS3 s3;

		Default(
			final AmazonS3 s3
		)
		{
			super();
			this.s3 = s3;
		}

		private Stream<S3ObjectSummary> fileObjectSummaries(
			final S3Path file
		)
		{
			final String prefix = toFileKeyPrefix(file);
			return this.s3.listObjectsV2(
				file.bucket(),
				prefix
			)
			.getObjectSummaries().stream()
			.filter(summary -> isFileKey(prefix, summary.getKey()))
			.sorted((s1, s2) -> Long.compare(this.getFileNr(s1), this.getFileNr(s2)))
			;
		}

		private long internalReadData(
			final S3Path                   file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length
		)
		{
			final List<S3ObjectSummary>     objectSummaries = this.fileObjectSummaries(file).collect(toList());
			final long                      sizeTotal       = objectSummaries.stream()
				.mapToLong(S3ObjectSummary::getSize)
				.sum()
			;
			final Iterator<S3ObjectSummary> iterator        = objectSummaries.iterator();
		          long                      remaining       = length > 0L
		        	  ? length
		        	  : sizeTotal - offset
		          ;
		          long                      readTotal       = 0L;
		          long                      skipped         = 0L;
		          ByteBuffer                targetBuffer    = null;
			while(remaining > 0 && iterator.hasNext())
			{
				final S3ObjectSummary objectSummary = iterator.next();
				final long            objectSize    = objectSummary.getSize();
				if(skipped + objectSize <= offset)
				{
					skipped += objectSize;
					continue;
				}

				if(targetBuffer == null)
				{
					targetBuffer = bufferProvider.apply(remaining);
				}

				final long objectOffset;
				if(skipped < offset)
				{
					objectOffset = offset - skipped;
					skipped = offset;
				}
				else
				{
					objectOffset = 0L;
				}
				final long amount = Math.min(
					objectSize - objectOffset,
					remaining
				);
				this.readObjectData(
					objectSummary,
					targetBuffer,
					objectOffset,
					amount
				);
				remaining -= amount;
				readTotal += amount;
			}

			return readTotal;
		}

		private void readObjectData(
			final S3ObjectSummary objectSummary,
			final ByteBuffer      targetBuffer ,
			final long            offset       ,
			final long            length
		)
		{
			final S3Object            object      = this.s3.getObject(
				new GetObjectRequest(
					objectSummary.getBucketName(),
					objectSummary.getKey()
				)
				.withRange(offset, offset + length - 1)
			);
			final S3ObjectInputStream inputStream = object.getObjectContent();
			try
			{
				final byte[] buffer    = new byte[1024 * 10];
				      long   remaining = length;
				      int    read;
				while(remaining > 0 &&
					(read = inputStream.read(
						buffer,
						0,
						Math.min(buffer.length, X.checkArrayRange(remaining)))
					) != -1
				)
				{
					targetBuffer.put(buffer, 0, read);
					remaining -= read;
				}
			}
			catch(final IOException e)
			{
				inputStream.abort();
				throw new IORuntimeException(e);
			}
			finally
			{
				try
				{
					inputStream.close();
				}
				catch(final IOException e)
				{
					// ignore
				}
			}
		}

		private long getFileNr(
			final S3ObjectSummary objectSummary
		)
		{
			final String key            = objectSummary.getKey();
			final int    separatorIndex = key.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR);
			return Long.parseLong(key.substring(separatorIndex + 1));
		}

		@Override
		public long fileSize(
			final S3Path file
		)
		{
			return this.fileObjectSummaries(file)
				.mapToLong(S3ObjectSummary::getSize)
				.sum()
			;
		}

		@Override
		public boolean directoryExists(
			final S3Path directory
		)
		{
			return this.s3.doesObjectExist(
				directory.bucket(),
				toDirectoryKey(directory)
			);
		}

		@Override
		public boolean fileExists(
			final S3Path file
		)
		{
			return this.fileObjectSummaries(file)
				.findAny()
				.isPresent()
			;
		}

		@Override
		public boolean createDirectory(
			final S3Path directory
		)
		{
			this.s3.putObject(
				directory.bucket(),
				toDirectoryKey(directory),
				""
			);
			return true;
		}

		@Override
		public boolean createFile(
			final S3Path file
		)
		{
			return true;
		}

		@Override
		public boolean deleteFile(
			final S3Path file
		)
		{
			final List<KeyVersion> keys = this.fileObjectSummaries(file)
				.map(summary -> new KeyVersion(summary.getKey()))
				.collect(Collectors.toList())
			;
			if(keys.size() > 0)
			{
				this.s3.deleteObjects(
					new DeleteObjectsRequest(file.bucket())
						.withKeys(keys)
				);

				return true;
			}

			return false;
		}

		@Override
		public ByteBuffer readData(
			final S3Path file  ,
			final long   offset,
			final long   length
		)
		{
			final Reference   <ByteBuffer> bufferRef      = Reference.New(null);
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				final ByteBuffer buffer = ByteBuffer.allocateDirect(X.checkArrayRange(capacity));
				bufferRef.set(buffer);
				return buffer;
			};

			this.internalReadData(file, bufferProvider, offset, length);

			final ByteBuffer buffer = bufferRef.get();
			if(buffer != null)
			{
				buffer.flip();
				return buffer;
			}

			return ByteBuffer.allocateDirect(0);
		}

		@Override
		public long readData(
			final S3Path     file        ,
			final ByteBuffer targetBuffer,
			final long       offset      ,
			final long       length
		)
		{
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				if(targetBuffer.remaining() < capacity)
				{
					// (07.06.2020 FH)EXCP: proper exception
					throw new IllegalArgumentException(
						"Provided target buffer has not enough space remaining to load the content: "
						+ targetBuffer.remaining() + " < " + capacity
					);
				}
				return targetBuffer;
			};
			return this.internalReadData(file, bufferProvider, offset, length);
		}

		@Override
		public long writeData(
			final S3Path                         file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final OptionalLong maxFileNr = this.fileObjectSummaries(file)
				.mapToLong(this::getFileNr)
				.max()
			;
			final long nextFileNr = maxFileNr.isPresent()
				? maxFileNr.getAsLong() + 1
				: 0L
			;

			long totalLength = 0L;
			for(final ByteBuffer buffer : sourceBuffers)
			{
				totalLength += buffer.remaining();
			}

			final ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(totalLength);

			try(final BufferedInputStream inputStream = new BufferedInputStream(
				new ByteBuffersInputStream(
					sourceBuffers.iterator(),
					totalLength
				),
				RequestClientOptions.DEFAULT_STREAM_BUFFER_SIZE
			))
			{
				this.s3.putObject(
					file.bucket(),
					toFileKeyPrefix(file) + nextFileNr,
					inputStream,
					objectMetadata
				);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			return totalLength;
		}

		@Override
		public void moveFile(
			final S3Path sourceFile,
			final S3Path targetFile
		)
		{
			this.copyFile(sourceFile, targetFile);
			this.deleteFile(sourceFile);
		}

		@Override
		public long copyFile(
			final S3Path sourceFile,
			final S3Path targetFile
		)
		{
			final String targetKeyPrefix = toFileKeyPrefix(targetFile);
			this.fileObjectSummaries(sourceFile).forEach(sourceFileSummary ->
			{
				final long fileNr = this.getFileNr(sourceFileSummary);
				this.s3.copyObject(
					 sourceFile.bucket(),
					 sourceFileSummary.getKey(),
					 targetFile.bucket(),
					 targetKeyPrefix + fileNr
				);
			});

			return this.fileSize(targetFile);
		}

		@Override
		public long copyFile(
			final S3Path sourceFile,
			final S3Path targetFile,
			final long   offset    ,
			final long   length
		)
		{
			final ByteBuffer buffer = this.readData(sourceFile, offset, length);
			return this.writeData(targetFile, Arrays.asList(buffer));
		}


		static class ByteBuffersInputStream extends InputStream
		{
			private final Iterator<? extends ByteBuffer> sourceBuffers;
			private       ByteBuffer                     currentBuffer;
			private final long                           sizeTotal    ;

			ByteBuffersInputStream(
				final Iterator<? extends ByteBuffer> sourceBuffers,
				final long sizeTotal
			)
			{
				super();
				this.sourceBuffers = sourceBuffers;
				this.sizeTotal     = sizeTotal    ;
			}

			private int internalRead(
				final Function<ByteBuffer, Integer> reader
			)
			{
				if(this.sourceBuffers == null)
				{
					return -1;
				}

				while(this.currentBuffer == null || !this.currentBuffer.hasRemaining())
				{
					if(!this.sourceBuffers.hasNext())
					{
						return -1;
					}
					this.currentBuffer = this.sourceBuffers.next();
				}

				return reader.apply(this.currentBuffer);
			}

			@Override
			public int available() throws IOException
			{
				return X.checkArrayRange(this.sizeTotal);
			}

			@Override
			public int read() throws IOException
			{
				return this.internalRead(
					buffer -> buffer.get() & 0xFF
				);
			}

			@Override
			public int read(
				final byte[] bytes ,
				final int    offset,
				final int    length
			)
			throws IOException
			{
				return this.internalRead(buffer ->
				{
					final int amount = Math.min(length, buffer.remaining());
			        buffer.get(bytes, offset, amount);
			        return amount;
				});
			}

		}

	}

}
