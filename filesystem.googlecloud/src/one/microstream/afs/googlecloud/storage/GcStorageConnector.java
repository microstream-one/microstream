package one.microstream.afs.googlecloud.storage;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.Storage.CopyRequest;

import one.microstream.X;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.memory.XMemory;
import one.microstream.reference.Reference;


public interface GcStorageConnector
{
	public long fileSize(GcStoragePath file);

	public boolean directoryExists(GcStoragePath directory);

	public boolean fileExists(GcStoragePath file);

	public boolean createDirectory(GcStoragePath directory);

	public boolean createFile(GcStoragePath file);

	public boolean deleteFile(GcStoragePath file);

	public ByteBuffer readData(GcStoragePath file, long offset, long length);

	public long readData(GcStoragePath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(GcStoragePath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(GcStoragePath sourceFile, GcStoragePath targetFile);

	public long copyFile(GcStoragePath sourceFile, GcStoragePath targetFile);

	public long copyFile(GcStoragePath sourceFile, GcStoragePath targetFile, long offset, long length);



	public static GcStorageConnector New(
		final Storage storage
	)
	{
		return new Default(
			notNull(storage)
		);
	}


	public static class Default implements GcStorageConnector
	{
		final static String  NUMBER_SUFFIX_SEPARATOR      = "."                    ;
		final static char    NUMBER_SUFFIX_SEPARATOR_CHAR = '.'                    ;
		final static Pattern NUMBER_SUFFIX_PATTERN        = Pattern.compile("\\d+");

		static String toDirectoryName(
			final GcStoragePath path
		)
		{
			// directories have a trailing /
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(GcStoragePath.SEPARATOR, "", GcStoragePath.SEPARATOR))
			;
		}

		static String toFileKeyPrefix(
			final GcStoragePath path
		)
		{
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(GcStoragePath.SEPARATOR, "", NUMBER_SUFFIX_SEPARATOR))
			;
		}

		static boolean isFileName(
			final String prefix,
			final String key
		)
		{
			return isFile(key)
				&& key.length() > prefix.length()
				&& key.startsWith(prefix)
				&& key.indexOf(GcStoragePath.SEPARATOR_CAHR, prefix.length()) == -1
				&& NUMBER_SUFFIX_PATTERN.matcher(key.substring(prefix.length())).matches()
			;
		}

		static boolean isDirectory(
			final String key
		)
		{
			return key.endsWith(GcStoragePath.SEPARATOR);
		}

		static boolean isFile(
			final String key
		)
		{
			return !isDirectory(key);
		}


		private final Storage storage;

		Default(
			final Storage storage
		)
		{
			super();
			this.storage = storage;
		}

		private Stream<Blob> blobs(
			final GcStoragePath file
		)
		{
			final List<Blob> blobs  = new ArrayList<>();
			final String     prefix = toFileKeyPrefix(file);
			      Page<Blob> page   = this.storage.list(
				file.bucket(),
				BlobListOption.currentDirectory(),
				BlobListOption.prefix(prefix)
			);
			while(page != null)
			{
				page.getValues().forEach(blobs::add);

				page = page.hasNextPage()
					? page.getNextPage()
					: null
				;
			}
			return blobs.stream()
				.filter(blob -> isFileName(prefix, blob.getName()))
				.sorted((s1, s2) -> Long.compare(this.getFileNr(s1), this.getFileNr(s2)))
			;
		}

		private long internalReadData(
			final GcStoragePath            file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length
		)
		{
			final List<Blob> blobs     = this.blobs(file).collect(toList());
			final long       sizeTotal = blobs.stream()
				.mapToLong(Blob::getSize)
				.sum()
			;
			final Iterator<Blob> iterator     = blobs.iterator();
		          long           remaining    = length > 0L
		        	  ? length
		        	  : sizeTotal - offset
		          ;
		          long           readTotal    = 0L;
		          long           skipped      = 0L;
		          ByteBuffer     targetBuffer = null;
			while(remaining > 0 && iterator.hasNext())
			{
				final Blob blob     = iterator.next();
				final long blobSize = blob.getSize();
				if(skipped + blobSize <= offset)
				{
					skipped += blobSize;
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
					blobSize - objectOffset,
					remaining
				);
				this.readObjectData(
					blob,
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
			final Blob       blob        ,
			final ByteBuffer targetBuffer,
			final long       offset      ,
			final long       length
		)
		{
			if(targetBuffer.remaining() == length)
			{
				try(ReadChannel readChannel = blob.reader())
				{
					if(offset > 0L)
					{
						readChannel.seek(offset);
					}
					while(targetBuffer.hasRemaining() && readChannel.read(targetBuffer) >= 0)
					{
						;
					}
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}
			}
			else
			{
				final ByteBuffer blobBuffer = ByteBuffer.allocateDirect(
					checkArrayRange(length)
				);

				try(ReadChannel readChannel = blob.reader())
				{
					if(offset > 0L)
					{
						readChannel.seek(offset);
					}
					while(targetBuffer.hasRemaining() && readChannel.read(blobBuffer) >= 0)
					{
						;
					}
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				blobBuffer.flip();
				targetBuffer.put(blobBuffer);
				XMemory.deallocateDirectByteBuffer(blobBuffer);
			}
		}

		private long getFileNr(
			final Blob blob
		)
		{
			final String name           = blob.getName();
			final int    separatorIndex = name.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR);
			return Long.parseLong(name.substring(separatorIndex + 1));
		}

		@Override
		public long fileSize(
			final GcStoragePath file
		)
		{
			return this.blobs(file)
				.mapToLong(Blob::getSize)
				.sum()
			;
		}

		@Override
		public boolean directoryExists(
			final GcStoragePath directory
		)
		{
			final String     name = toDirectoryName(directory);
			      Page<Blob> page = this.storage.list(
			    	  directory.bucket(),
				BlobListOption.currentDirectory(),
				BlobListOption.prefix(name  )
			);
			while(page != null)
			{
				for(final Blob blob : page.getValues())
				{
					if(blob.getName().equals(name))
					{
						return true;
					}
				}

				page = page.hasNextPage()
					? page.getNextPage()
					: null
				;
			}

			return false;
		}

		@Override
		public boolean fileExists(
			final GcStoragePath file
		)
		{
			return this.blobs(file)
				.findAny()
				.isPresent()
			;
		}

		@Override
		public boolean createDirectory(
			final GcStoragePath directory
		)
		{
			return true;
		}

		@Override
		public boolean createFile(
			final GcStoragePath file
		)
		{
			return true;
		}

		@Override
		public boolean deleteFile(
			final GcStoragePath file
		)
		{
			final List<BlobId> blobIds = this.blobs(file)
				.map(blob -> blob.getBlobId())
				.collect(Collectors.toList())
			;
			if(blobIds.size() > 0)
			{
				this.storage.delete(blobIds);

				return true;
			}

			return false;
		}

		@Override
		public ByteBuffer readData(
			final GcStoragePath file  ,
			final long          offset,
			final long          length
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
			final GcStoragePath file        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
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
			final GcStoragePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final OptionalLong maxFileNr = this.blobs(file)
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

			final BlobInfo blobInfo = BlobInfo.newBuilder(
				file.bucket(),
				toFileKeyPrefix(file) + nextFileNr
			)
			.build();

			try(final WriteChannel writeChannel = this.storage.writer(blobInfo))
			{
				for(final ByteBuffer sourceBuffer : sourceBuffers)
				{
					while(sourceBuffer.hasRemaining())
					{
						writeChannel.write(sourceBuffer);
					}
				}
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			return totalLength;
		}

		@Override
		public void moveFile(
			final GcStoragePath sourceFile,
			final GcStoragePath targetFile
		)
		{
			this.copyFile(sourceFile, targetFile);
			this.deleteFile(sourceFile);
		}

		@Override
		public long copyFile(
			final GcStoragePath sourceFile,
			final GcStoragePath targetFile
		)
		{
			final String targetKeyPrefix = toFileKeyPrefix(targetFile);
			this.blobs(sourceFile).forEach(sourceBlob ->
			{
				final BlobInfo targetBlobInfo = BlobInfo.newBuilder(
					targetFile.bucket(),
					targetKeyPrefix + this.getFileNr(sourceBlob)
				)
				.build();
				this.storage.copy(
					CopyRequest.of(sourceBlob.getBlobId(), targetBlobInfo)
				);
			});

			return this.fileSize(targetFile);
		}

		@Override
		public long copyFile(
			final GcStoragePath sourceFile,
			final GcStoragePath targetFile,
			final long          offset    ,
			final long          length
		)
		{
			final ByteBuffer buffer = this.readData(sourceFile, offset, length);
			return this.writeData(targetFile, Arrays.asList(buffer));
		}

	}

}
