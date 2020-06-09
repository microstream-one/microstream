package one.microstream.afs.azure.storage;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ListBlobsOptions;

import one.microstream.X;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.reference.Reference;


public interface AzureStorageConnector
{
	public long fileSize(AzureStoragePath file);

	public boolean directoryExists(AzureStoragePath directory);

	public boolean fileExists(AzureStoragePath file);

	public boolean createDirectory(AzureStoragePath directory);

	public boolean createFile(AzureStoragePath file);

	public boolean deleteFile(AzureStoragePath file);

	public ByteBuffer readData(AzureStoragePath file, long offset, long length);

	public long readData(AzureStoragePath file, ByteBuffer targetBuffer, long offset, long length);

	public long writeData(AzureStoragePath file, Iterable<? extends ByteBuffer> sourceBuffers);

	public void moveFile(AzureStoragePath sourceFile, AzureStoragePath targetFile);

	public long copyFile(AzureStoragePath sourceFile, AzureStoragePath targetFile);

	public long copyFile(AzureStoragePath sourceFile, AzureStoragePath targetFile, long offset, long length);



	public static AzureStorageConnector New(
		final BlobServiceClient serviceClient
	)
	{
		return new Default(
			notNull(serviceClient)
		);
	}


	public static class Default implements AzureStorageConnector
	{
		final static String  NUMBER_SUFFIX_SEPARATOR      = "."                    ;
		final static char    NUMBER_SUFFIX_SEPARATOR_CHAR = '.'                    ;
		final static Pattern NUMBER_SUFFIX_PATTERN        = Pattern.compile("\\d+");

		static String toDirectoryName(
			final AzureStoragePath path
		)
		{
			// directories have a trailing /
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(AzureStoragePath.SEPARATOR, "", AzureStoragePath.SEPARATOR))
			;
		}

		static String toFileKeyPrefix(
			final AzureStoragePath path
		)
		{
			return Arrays.stream(path.pathElements())
				.skip(1L) // skip bucket
				.collect(Collectors.joining(AzureStoragePath.SEPARATOR, "", NUMBER_SUFFIX_SEPARATOR))
			;
		}

		static boolean isFileName(
			final String prefix,
			final String name
		)
		{
			return isFile(name)
				&& name.length() > prefix.length()
				&& name.startsWith(prefix)
				&& name.indexOf(AzureStoragePath.SEPARATOR_CAHR, prefix.length()) == -1
				&& NUMBER_SUFFIX_PATTERN.matcher(name.substring(prefix.length())).matches()
			;
		}

		static boolean isDirectory(
			final String key
		)
		{
			return key.endsWith(AzureStoragePath.SEPARATOR);
		}

		static boolean isFile(
			final String key
		)
		{
			return !isDirectory(key);
		}


		private final BlobServiceClient serviceClient;

		Default(
			final BlobServiceClient serviceClient
		)
		{
			super();
			this.serviceClient = serviceClient;
		}

		private Stream<BlobItem> blobItems(
			final AzureStoragePath file
		)
		{
			final String prefix = toFileKeyPrefix(file);
			final PagedIterable<BlobItem> blobs = this.serviceClient.getBlobContainerClient(
				file.storageContainer()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			);
			return blobs.stream()
				.filter(summary -> isFileName(prefix, summary.getName()))
				.sorted((s1, s2) -> Long.compare(this.getFileNr(s1), this.getFileNr(s2)))
			;
		}

		private long internalReadData(
			final AzureStoragePath                   file          ,
			final LongFunction<ByteBuffer> bufferProvider,
			final long                     offset        ,
			final long                     length
		)
		{
			final List<BlobItem>     blobItems        = this.blobItems(file).collect(toList());
			final long               sizeTotal        = blobItems.stream()
				.mapToLong(blobItem -> blobItem.getProperties().getContentLength())
				.sum()
			;
			final Iterator<BlobItem> iterator        = blobItems.iterator();
		          long               remaining       = length > 0L
		        	  ? length
		        	  : sizeTotal - offset
		          ;
		          long               readTotal       = 0L;
		          long               skipped         = 0L;
		          ByteBuffer         targetBuffer    = null;
			while(remaining > 0 && iterator.hasNext())
			{
				final BlobItem blobItem   = iterator.next();
				final long     objectSize = blobItem.getProperties().getContentLength();
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
					file,
					blobItem,
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
			final AzureStoragePath file,
			final BlobItem   blobItem    ,
			final ByteBuffer targetBuffer,
			final long       offset      ,
			final long       length
		)
		{
			try(ByteBufferOutputStream outputStream = new ByteBufferOutputStream(targetBuffer))
			{
				this.serviceClient.getBlobContainerClient(file.storageContainer())
					.getBlobClient(blobItem.getName())
					.downloadWithResponse(
						outputStream,
						new BlobRange(offset, length),
						new DownloadRetryOptions().setMaxRetryRequests(3),
						null,
						false,
						null,
						null
					)
					.getStatusCode()
				;
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}
		}

		private long getFileNr(
			final BlobItem blobItem
		)
		{
			final String name           = blobItem.getName();
			final int    separatorIndex = name.lastIndexOf(NUMBER_SUFFIX_SEPARATOR_CHAR);
			return Long.parseLong(name.substring(separatorIndex + 1));
		}

		@Override
		public long fileSize(
			final AzureStoragePath file
		)
		{
			return this.blobItems(file)
				.mapToLong(blobItem -> blobItem.getProperties().getContentLength())
				.sum()
			;
		}

		@Override
		public boolean directoryExists(
			final AzureStoragePath directory
		)
		{
			final String name = toDirectoryName(directory);
			final PagedIterable<BlobItem> blobItems = this.serviceClient.getBlobContainerClient(
				directory.storageContainer()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(name),
				null
			);
			for(final BlobItem blobItem : blobItems)
			{
				if(blobItem.getName().equals(name))
				{
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean fileExists(
			final AzureStoragePath file
		)
		{
			return this.blobItems(file)
				.findAny()
				.isPresent()
			;
		}

		@Override
		public boolean createDirectory(
			final AzureStoragePath directory
		)
		{
			return true;
		}

		@Override
		public boolean createFile(
			final AzureStoragePath file
		)
		{
			return true;
		}

		@Override
		public boolean deleteFile(
			final AzureStoragePath file
		)
		{
			final String prefix = toFileKeyPrefix(file);
			final BlobContainerClient containerClient = this.serviceClient.getBlobContainerClient(
				file.storageContainer()
			);
			final PagedIterable<BlobItem> blobs = containerClient
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			);
			final AtomicBoolean deleted = new AtomicBoolean(false);
			blobs.stream()
				.filter(summary -> isFileName(prefix, summary.getName()))
				.forEach(blobItem ->
				{
					containerClient.getBlobClient(blobItem.getName()).delete();
					deleted.set(true);
				})
			;

			return deleted.get();
		}

		@Override
		public ByteBuffer readData(
			final AzureStoragePath file  ,
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
			final AzureStoragePath     file        ,
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
			final AzureStoragePath                         file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final OptionalLong maxFileNr = this.blobItems(file)
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

			try(final ByteBuffersInputStream inputStream = new ByteBuffersInputStream(
				sourceBuffers.iterator(),
				totalLength
			))
			{
				this.serviceClient.getBlobContainerClient(file.storageContainer())
					.getBlobClient(toFileKeyPrefix(file) + nextFileNr)
					.getBlockBlobClient()
					.upload(inputStream, totalLength)
				;
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			return totalLength;
		}

		@Override
		public void moveFile(
			final AzureStoragePath sourceFile,
			final AzureStoragePath targetFile
		)
		{
			this.copyFile(sourceFile, targetFile);
			this.deleteFile(sourceFile);
		}

		@Override
		public long copyFile(
			final AzureStoragePath sourceFile,
			final AzureStoragePath targetFile
		)
		{
			// TODO

			return this.fileSize(targetFile);
		}

		@Override
		public long copyFile(
			final AzureStoragePath sourceFile,
			final AzureStoragePath targetFile,
			final long   offset    ,
			final long   length
		)
		{
			final ByteBuffer buffer = this.readData(sourceFile, offset, length);
			return this.writeData(targetFile, Arrays.asList(buffer));
		}


		static class ByteBufferOutputStream extends OutputStream
		{
			private final ByteBuffer targetBuffer;

			ByteBufferOutputStream(final ByteBuffer targetBuffer)
			{
				super();
				this.targetBuffer = targetBuffer;
			}

			@Override
			public void write(final int b) throws IOException
			{
				this.targetBuffer.put((byte)b);
			}

			@Override
			public void write(final byte[] bytes, final int offset, final int length) throws IOException
			{
				if(bytes == null)
				{
					throw new NullPointerException();
				}
				if(offset < 0
				|| offset > bytes.length
				|| length < 0
				|| offset + length > bytes.length
				|| offset + length < 0)
				{
					throw new IndexOutOfBoundsException();
				}
				if(length == 0)
				{
					return;
				}

				this.targetBuffer.put(bytes, offset, length);
			}

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
