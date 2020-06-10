package one.microstream.afs.blobstore;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import one.microstream.reference.Reference;


public interface BlobStoreConnector
{
	public long fileSize(BlobStorePath file);

	public boolean directoryExists(BlobStorePath directory);

	public boolean fileExists(BlobStorePath file);

	public boolean createDirectory(BlobStorePath directory);

	public boolean createFile(BlobStorePath file);

	public boolean deleteFile(BlobStorePath file);

	public ByteBuffer readData(BlobStorePath file, long offset, long length);

	public long readData(BlobStorePath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(BlobStorePath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(BlobStorePath sourceFile, BlobStorePath targetFile);

	public long copyFile(BlobStorePath sourceFile, BlobStorePath targetFile);

	public long copyFile(BlobStorePath sourceFile, BlobStorePath targetFile, long offset, long length);



	/**
	 *
	 * @param <B> the blob type
	 */
	public static abstract class Abstract<B> implements BlobStoreConnector
	{
		protected final static String  NUMBER_SUFFIX_SEPARATOR      = "."                    ;
		protected final static char    NUMBER_SUFFIX_SEPARATOR_CHAR = '.'                    ;
		protected final static Pattern NUMBER_SUFFIX_PATTERN        = Pattern.compile("\\d+");


		protected static String toContainerKey(
			final BlobStorePath directory
		)
		{
			// container keys have a trailing /
			return Arrays.stream(directory.pathElements())
				.skip(1L) // skip container
				.collect(Collectors.joining(BlobStorePath.SEPARATOR, "", BlobStorePath.SEPARATOR))
			;
		}

		protected static String toBlobKeyPrefix(
			final BlobStorePath file
		)
		{
			return Arrays.stream(file.pathElements())
				.skip(1L) // skip container
				.collect(Collectors.joining(BlobStorePath.SEPARATOR, "", NUMBER_SUFFIX_SEPARATOR))
			;
		}

		protected static boolean isBlobKey(
			final String prefix,
			final String key
		)
		{
			return isBlobKey(key)
				&& key.length() > prefix.length()
				&& key.startsWith(prefix)
				&& key.indexOf(BlobStorePath.SEPARATOR_CHAR, prefix.length()) == -1
				&& NUMBER_SUFFIX_PATTERN.matcher(key.substring(prefix.length())).matches()
			;
		}

		protected static boolean isBlobKey(
			final String key
		)
		{
			return !isContainerKey(key);
		}

		protected static boolean isContainerKey(
			final String key
		)
		{
			return key.endsWith(BlobStorePath.SEPARATOR);
		}


		protected Abstract()
		{
			super();
		}

		protected abstract String key(B blob);

		protected abstract long size(B blob);

		protected abstract Stream<B> blobs(BlobStorePath file);

		protected abstract void readBlobData(
			BlobStorePath file        ,
			B             blob        ,
			ByteBuffer    targetBuffer,
			long          offset      ,
			long          length
		);

		protected long getBlobNr(
			final B blob
		)
		{
			final String key            = this.key(blob);
			final int    separatorIndex = key.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR);
			return Long.parseLong(key.substring(separatorIndex + 1));
		}

		protected long nextBlobNr(
			final BlobStorePath file
		)
		{
			final OptionalLong maxBlobNr = this.blobs(file)
				.mapToLong(this::getBlobNr)
				.max()
			;
			return maxBlobNr.isPresent()
				? maxBlobNr.getAsLong() + 1
				: 0L
			;
		}

		protected long internalReadData(
			final BlobStorePath            file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length
		)
		{
			final List<B>     blobs        = this.blobs(file).collect(toList());
			final long        sizeTotal    = blobs.stream()
				.mapToLong(this::size)
				.sum()
			;
			final Iterator<B> iterator     = blobs.iterator();
		          long        remaining    = length > 0L
		        	  ? length
		        	  : sizeTotal - offset
		          ;
		          long        readTotal    = 0L;
		          long        skipped      = 0L;
		          ByteBuffer  targetBuffer = null;
			while(remaining > 0 && iterator.hasNext())
			{
				final B    blob     = iterator.next();
				final long blobSize = this.size(blob);
				if(skipped + blobSize <= offset)
				{
					skipped += blobSize;
					continue;
				}

				if(targetBuffer == null)
				{
					targetBuffer = bufferProvider.apply(remaining);
				}

				final long blobOffset;
				if(skipped < offset)
				{
					blobOffset = offset - skipped;
					skipped = offset;
				}
				else
				{
					blobOffset = 0L;
				}
				final long amount = Math.min(
					blobSize - blobOffset,
					remaining
				);
				this.readBlobData(
					file,
					blob,
					targetBuffer,
					blobOffset,
					amount
				);
				remaining -= amount;
				readTotal += amount;
			}

			return readTotal;
		}

		protected long totalSize(
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			long totalSize = 0L;
			for(final ByteBuffer buffer : sourceBuffers)
			{
				totalSize += buffer.remaining();
			}
			return totalSize;
		}

		@Override
		public boolean createDirectory(
			final BlobStorePath directory
		)
		{
			// 'directories' are just parent paths of existing blobs
			return true;
		}

		@Override
		public boolean createFile(
			final BlobStorePath file
		)
		{
			// 'files' consist of blobs, they are created with the first write
			return true;
		}

		@Override
		public long fileSize(
			final BlobStorePath file
		)
		{
			return this.blobs(file)
				.mapToLong(this::size)
				.sum()
			;
		}

		@Override
		public boolean fileExists(
			final BlobStorePath file
		)
		{
			return this.blobs(file)
				.findAny()
				.isPresent()
			;
		}

		@Override
		public ByteBuffer readData(
			final BlobStorePath file  ,
			final long          offset,
			final long          length
		)
		{
			final Reference   <ByteBuffer> bufferRef      = Reference.New(null);
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				final ByteBuffer buffer = ByteBuffer.allocateDirect(checkArrayRange(capacity));
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
			final BlobStorePath file        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final LongFunction<ByteBuffer> bufferProvider = capacity ->
			{
				if(targetBuffer.remaining() < capacity)
				{
					// (10.06.2020 FH)EXCP: proper exception
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
		public long copyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile,
			final long          offset    ,
			final long          length
		)
		{
			if(offset == 0
			&& length > 0
			&& length == this.fileSize(sourceFile))
			{
				return this.copyFile(sourceFile, targetFile);
			}

			final ByteBuffer buffer = this.readData(sourceFile, offset, length);
			return this.writeData(targetFile, Arrays.asList(buffer));
		}

		@Override
		public void moveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.copyFile(sourceFile, targetFile);
			this.deleteFile(sourceFile);
		}

	}

}
