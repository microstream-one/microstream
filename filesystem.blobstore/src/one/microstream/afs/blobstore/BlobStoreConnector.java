package one.microstream.afs.blobstore;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.function.ToLongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import one.microstream.reference.Reference;

/**
 * Connector for blob stores which handles the concrete IO operations on a specific connection.
 * <p>
 * All operations must be implemented thread-safe.
 *
 * @author FH
 *
 */
public interface BlobStoreConnector extends AutoCloseable
{
	public long fileSize(BlobStorePath file);

	public boolean directoryExists(BlobStorePath directory);

	public boolean fileExists(BlobStorePath file);

	public void visitChildren(BlobStorePath directory, BlobStorePathVisitor visitor);

	public boolean createDirectory(BlobStorePath directory);

	public boolean createFile(BlobStorePath file);

	public boolean deleteFile(BlobStorePath file);

	public ByteBuffer readData(BlobStorePath file, long offset, long length);

	public long readData(BlobStorePath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(BlobStorePath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(BlobStorePath sourceFile, BlobStorePath targetFile);

	public long copyFile(BlobStorePath sourceFile, BlobStorePath targetFile);

	public long copyFile(BlobStorePath sourceFile, BlobStorePath targetFile, long offset, long length);

	public void truncateFile(BlobStorePath file, long newLength);

	@Override
	public void close();


	/**
	 *
	 * @param <B> the blob type
	 */
	public static abstract class Abstract<B> implements BlobStoreConnector
	{
		protected final static String  NUMBER_SUFFIX_SEPARATOR      = ".";
		protected final static char    NUMBER_SUFFIX_SEPARATOR_CHAR = '.';
		protected final static String  NUMBER_SUFFIX_REGEX          = "\\d+";

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

		protected static String toBlobKey(
			final BlobStorePath file,
			final long          nr
		)
		{
			return toBlobKeyPrefix(file).concat(Long.toString(nr));
		}

		protected static String toBlobKeyWithContainer(
			final BlobStorePath file,
			final long          nr
		)
		{
			return toBlobKeyPrefixWithContainer(file).concat(Long.toString(nr));
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

		protected static String toBlobKeyPrefixWithContainer(
			final BlobStorePath file
		)
		{
			return Arrays.stream(file.pathElements())
				.collect(Collectors.joining(BlobStorePath.SEPARATOR, "", NUMBER_SUFFIX_SEPARATOR))
			;
		}

		protected static String blobKeyRegex(
			final String prefix
		)
		{
			return Pattern.quote(prefix).concat(NUMBER_SUFFIX_REGEX);
		}

		protected static String toChildKeysPrefix(
			final BlobStorePath directory
		)
		{
			return Arrays.stream(directory.pathElements())
				.skip(1L) // skip container
				.collect(Collectors.joining(BlobStorePath.SEPARATOR, "", BlobStorePath.SEPARATOR))
			;
		}

		protected static String toChildKeysPrefixWithContainer(
			final BlobStorePath directory
		)
		{
			return Arrays.stream(directory.pathElements())
				.collect(Collectors.joining(BlobStorePath.SEPARATOR, "", BlobStorePath.SEPARATOR))
			;
		}

		protected static String childKeysRegex(
			final BlobStorePath directory
		)
		{
			return Pattern.quote(toChildKeysPrefix(directory)) + "[^" + BlobStorePath.SEPARATOR + "]+";
		}

		protected static String childKeysRegexWithContainer(
			final BlobStorePath directory
		)
		{
			return Pattern.quote(toChildKeysPrefixWithContainer(directory)) + "[^" + BlobStorePath.SEPARATOR + "]+";
		}

		protected static String removeNumberSuffix(
			final String key
		)
		{
			return key.substring(
				0,
				key.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR)
			);
		}

		protected static boolean isBlobKey(
			final String key
		)
		{
			return !isDirectoryKey(key);
		}

		protected static boolean isDirectoryKey(
			final String key
		)
		{
			return key.endsWith(BlobStorePath.SEPARATOR);
		}

		protected static String directoryNameOfKey(
			final String key
		)
		{
			int lastSeparator = -1;
			for(int i = key.length() - 1; --i >= 0; )
			{
				if(key.charAt(i) == BlobStorePath.SEPARATOR_CHAR)
				{
					lastSeparator = i;
					break;
				}
			}
			return key.substring(
				lastSeparator + 1,
				key.length() - 1
			);
		}


		private final Function<B, String>     blobKeyProvider       ;
		private final ToLongFunction<B>       blobSizeProvider      ;
		private final BlobStorePath.Validator blobStorePathValidator;
		private final AtomicBoolean           open                  ;
		private final Map<String, Boolean>    directoryExistsCache  = new HashMap<>();
		private final Map<String, Boolean>    fileExistsCache       = new HashMap<>();
		private final Map<String, Long>       fileSizeCache         = new HashMap<>();

		protected Abstract(
			final Function<B, String> blobKeyProvider ,
			final ToLongFunction<B>   blobSizeProvider
		)
		{
			this(
				blobKeyProvider,
				blobSizeProvider,
				null
			);
		}

		protected Abstract(
			final Function<B, String>     blobKeyProvider       ,
			final ToLongFunction<B>       blobSizeProvider      ,
			final BlobStorePath.Validator blobStorePathValidator
		)
		{
			super();
			this.blobKeyProvider        = notNull(blobKeyProvider) ;
			this.blobSizeProvider       = notNull(blobSizeProvider);
			this.blobStorePathValidator = blobStorePathValidator != null
				? blobStorePathValidator
				: BlobStorePath.Validator.NO_OP
			;
			this.open                   = new AtomicBoolean(true);
		}

		protected abstract Stream<? extends B> blobs(
			BlobStorePath file
		);

		protected abstract Stream<String> childKeys(
			BlobStorePath directory
		);

		protected abstract boolean internalDeleteBlobs(
			BlobStorePath     file ,
			List<? extends B> blobs
		);

		protected abstract void internalReadBlobData(
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
				.mapToLong(this.blobSizeProvider)
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

		protected void internalVisitChildren(
			final BlobStorePath        directory,
			final BlobStorePathVisitor visitor
		)
		{
			final Set<String> directoryNames = new LinkedHashSet<>();
			final Set<String> fileNames      = new LinkedHashSet<>();

			this.childKeys(directory).forEach(key ->
			{
				if(isDirectoryKey(key))
				{
					directoryNames.add(directoryNameOfKey(key));
				}
				else
				{
					fileNames.add(this.fileNameOfKey(key));
				}
			});

			directoryNames.forEach(name -> visitor.visitDirectory(directory, name));
			fileNames     .forEach(name -> visitor.visitFile     (directory, name));
		}

		protected String fileNameOfKey(
			final String key
		)
		{
			return key.substring(
				key.lastIndexOf(BlobStorePath.SEPARATOR_CHAR) + 1,
				key.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR)
			);
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

		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final List<? extends B> blobs = this.blobs(file).collect(toList());
			return blobs.isEmpty()
				? false
				: this.internalDeleteBlobs(file, blobs)
			;
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

		protected void internalTruncateFile(
			final BlobStorePath file     ,
			final long          newLength
		)
		{
			final List<? extends B> blobs = this.blobs(file).collect(toList());
			      long              offset = 0L;
			      B                 blob   = null;
			for(final B b : blobs)
			{
				final long size  = this.blobSizeProvider.applyAsLong(b);
				final long start = offset;
				final long end   = offset + size - 1L;
				if(start <= newLength && end >= newLength)
				{
					blob = b;
					break;
				}
				offset += size;
			}

			if(blob == null)
			{
				throw new IllegalArgumentException("new length > file length");
			}

			final long blobStart = offset;
			final long blobEnd   = this.blobSizeProvider.applyAsLong(blob) - 1L;
			final int  blobIndex = blobs.indexOf(blob);
			final int  blobCount = blobs.size();

			if(blobStart == newLength)
			{
				this.internalDeleteBlobs(
					file,
					blobs.subList(blobIndex, blobCount)
				);
			}
			else if(blobEnd == newLength - 1)
			{
				this.internalDeleteBlobs(
					file,
					blobs.subList(blobIndex + 1, blobCount)
				);
			}
			else
			{
				final long       newBlobLength = newLength - blobStart;
				final ByteBuffer buffer        = ByteBuffer.allocateDirect(
					checkArrayRange(newBlobLength)
				);
				this.internalReadBlobData(file, blob, buffer, 0L, newBlobLength);
				buffer.flip();

				this.internalDeleteBlobs(
					file,
					blobs.subList(blobIndex, blobCount)
				);

				this.internalWriteData(file, Arrays.asList(buffer));
			}
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

		protected Comparator<B> blobComparator()
		{
			return (b1, b2) -> Long.compare(this.blobNumber(b1), this.blobNumber(b2));
		}

		protected long blobNumber(
			final B blob
		)
		{
			final String key            = this.blobKeyProvider.apply(blob);
			final int    separatorIndex = key.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR);
			return Long.parseLong(key.substring(separatorIndex + 1));
		}

		protected long nextBlobNumber(
			final BlobStorePath file
		)
		{
			final OptionalLong maxBlobNumber = this.blobs(file)
				.mapToLong(this::blobNumber)
				.max()
			;
			return maxBlobNumber.isPresent()
				? maxBlobNumber.getAsLong() + 1
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
				.mapToLong(this.blobSizeProvider)
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
				final long blobSize = this.blobSizeProvider.applyAsLong(blob);
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
				this.internalReadBlobData(
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
			this.blobStorePathValidator.validate(file);

			return this.fileSizeCache.computeIfAbsent(
				file.fullQualifiedName(),
				name -> this.internalFileSize(file)
			);
		}

		@Override
		public final boolean directoryExists(final BlobStorePath directory)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(directory);

			synchronized(this)
			{
				return this.directoryExistsCache.computeIfAbsent(
					directory.fullQualifiedName(),
					name -> this.internalDirectoryExists(directory)
				);
			}
		}

		@Override
		public final boolean fileExists(
			final BlobStorePath file
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(file);

			synchronized(this)
			{
				return this.fileExistsCache.computeIfAbsent(
					file.fullQualifiedName(),
					name -> this.internalFileExists(file)
				);
			}
		}

		@Override
		public void visitChildren(
			final BlobStorePath        directory,
			final BlobStorePathVisitor visitor
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(directory);

			this.internalVisitChildren(directory, visitor);
		}

		@Override
		public final boolean createDirectory(
			final BlobStorePath directory
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(directory);

			return this.internalCreateDirectory(directory);
		}

		@Override
		public final boolean createFile(
			final BlobStorePath file
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(file);

			return this.internalCreateFile(file);
		}

		@Override
		public final boolean deleteFile(
			final BlobStorePath file
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(file);

			synchronized(this)
			{
				this.fileExistsCache.remove(file.fullQualifiedName());
				this.fileSizeCache.remove(file.fullQualifiedName());
			}
			
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
			this.blobStorePathValidator.validate(file);

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
			this.blobStorePathValidator.validate(file);

			return this.internalReadData(file, targetBuffer, offset, length);
		}

		@Override
		public final long writeData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(file);

			final long addedSize = this.internalWriteData(file, sourceBuffers);
			
			synchronized(this)
			{
				this.fileSizeCache.computeIfPresent(
					file.fullQualifiedName(),
					(fileName, fileSize) -> fileSize + addedSize
				);
			}
			
			return addedSize;
		}

		@Override
		public final void moveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(sourceFile);
			this.blobStorePathValidator.validate(targetFile);

			this.internalMoveFile(sourceFile, targetFile);
			
			synchronized(this)
			{
				this.fileExistsCache.put(sourceFile.fullQualifiedName(), Boolean.FALSE);
				this.fileExistsCache.put(targetFile.fullQualifiedName(), Boolean.TRUE);
				
				final Long fileSize = this.fileSizeCache.remove(sourceFile.fullQualifiedName());
				if(fileSize != null)
				{
					this.fileSizeCache.put(targetFile.fullQualifiedName(), fileSize);
				}
			}
		}

		@Override
		public final long copyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(sourceFile);
			this.blobStorePathValidator.validate(targetFile);

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
			this.blobStorePathValidator.validate(sourceFile);
			this.blobStorePathValidator.validate(targetFile);

			return this.internalCopyFile(sourceFile, targetFile, offset, length);
		}

		@Override
		public void truncateFile(
			final BlobStorePath file     ,
			final long          newLength
		)
		{
			this.ensureOpen();
			this.blobStorePathValidator.validate(file);

			if(newLength == 0L)
			{
				this.internalDeleteFile(file);
				
				synchronized(this)
				{
					this.fileExistsCache.remove(file.fullQualifiedName());
					this.fileSizeCache.remove(file.fullQualifiedName());
				}
			}
			else
			{
				this.internalTruncateFile(file, newLength);
				
				synchronized(this)
				{
					this.fileSizeCache.computeIfPresent(
						file.fullQualifiedName(),
						(fileName, fileSize) -> newLength
					);
				}
			}
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
