package one.microstream.afs.blobstore;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import one.microstream.reference.Reference;


public interface BlobStoreConnector extends AutoCloseable
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

	@Override
	public void close();


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


		private final AtomicBoolean open = new AtomicBoolean(true);

		protected Abstract()
		{
			super();
		}

		protected abstract String key(B blob);

		protected abstract long size(B blob);

		protected abstract Stream<B> blobs(BlobStorePath file);

		protected abstract boolean internalDeleteFile(BlobStorePath file);

		protected abstract void readBlobData(
			BlobStorePath file        ,
			B             blob        ,
			ByteBuffer    targetBuffer,
			long          offset      ,
			long          length
		);

		protected abstract long internalWriteData(
			BlobStorePath                  file         ,
			Iterable<? extends ByteBuffer> sourceBuffers
		);

		protected abstract long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		);

		protected long internalFileSize(
			final BlobStorePath file
		)
		{
			return this.blobs(file)
				.mapToLong(this::size)
				.sum()
			;
		}

		protected boolean internalDirectoryExists(
			final BlobStorePath directory
		)
		{
			return true;
		}

		protected boolean internalFileExists(
			final BlobStorePath file
		)
		{
			return this.blobs(file)
				.findAny()
				.isPresent()
			;
		}

		protected boolean internalCreateDirectory(
			final BlobStorePath directory
		)
		{
			// 'directories' are just parent paths of existing blobs

			return true;
		}

		protected boolean internalCreateFile(
			final BlobStorePath file
		)
		{
			// 'files' consist of blobs, they are created with the first write

			return true;
		}

		protected ByteBuffer internalReadData(
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

		protected long internalReadData(
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

		protected void internalMoveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.internalCopyFile(sourceFile, targetFile);
			this.internalDeleteFile(sourceFile);
		}

		protected long internalCopyFile(
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

		protected final void ensureOpen()
		{
			if(!this.open.get())
			{
				throw new IllegalStateException("Connector is closed");
			}
		}

		protected void internalClose()
		{
			// no-op by default
		}

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
		public final long fileSize(
			final BlobStorePath file
		)
		{
			this.ensureOpen();

			return this.internalFileSize(file);
		}

		@Override
		public final boolean directoryExists(final BlobStorePath directory)
		{
			this.ensureOpen();

			return this.internalDirectoryExists(directory);
		}

		@Override
		public final boolean fileExists(
			final BlobStorePath file
		)
		{
			this.ensureOpen();

			return this.internalFileExists(file);
		}

		@Override
		public final boolean createDirectory(
			final BlobStorePath directory
		)
		{
			this.ensureOpen();

			return this.internalCreateDirectory(directory);
		}

		@Override
		public final boolean createFile(
			final BlobStorePath file
		)
		{
			this.ensureOpen();

			return this.internalCreateFile(file);
		}

		@Override
		public final boolean deleteFile(
			final BlobStorePath file
		)
		{
			this.ensureOpen();

			return this.internalDeleteFile(file);
		}

		@Override
		public final ByteBuffer readData(
			final BlobStorePath file  ,
			final long          offset,
			final long          length
		)
		{
			this.ensureOpen();

			return this.internalReadData(file, offset, length);
		}

		@Override
		public final long readData(
			final BlobStorePath file        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			this.ensureOpen();

			return this.internalReadData(file, targetBuffer, offset, length);
		}

		@Override
		public final long writeData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.ensureOpen();

			return this.internalWriteData(file, sourceBuffers);
		}

		@Override
		public final void moveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.ensureOpen();

			this.internalMoveFile(sourceFile, targetFile);
		}

		@Override
		public final long copyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.ensureOpen();

			return this.internalCopyFile(sourceFile, targetFile);
		}

		@Override
		public final long copyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile,
			final long          offset    ,
			final long          length
		)
		{
			this.ensureOpen();

			return this.internalCopyFile(sourceFile, targetFile, offset, length);
		}

		@Override
		public final void close()
		{
			if(!this.open.get())
			{
				this.open.set(false);

				this.internalClose();
			}
		}

	}

}
