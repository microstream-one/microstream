package one.microstream.afs.googlecloud.storage.types;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.memory.XMemory;

/**
 * Connector for the <a href="https://cloud.google.com/storage">Google cloud storage</a>.
 * <p>
 * First create a <a href="https://cloud.google.com/storage/docs/reference/libraries#client-libraries-install-java">storage client</a>.
 * <pre>
 * Storage storage = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	GoogleCloudStorageConnector.New(storage)
 * );
 * </pre>
 *
 * 
 *
 */
public interface GoogleCloudStorageConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link GoogleCloudStorageConnector}.
	 *
	 * @param storage connection to the Google storage service
	 * @return a new {@link GoogleCloudStorageConnector}
	 */
	public static GoogleCloudStorageConnector New(
		final Storage storage
	)
	{
		return new GoogleCloudStorageConnector.Default(
			notNull(storage),
			false
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link GoogleCloudStorageConnector} with cache.
	 *
	 * @param storage connection to the Google storage service
	 * @return a new {@link GoogleCloudStorageConnector}
	 */
	public static GoogleCloudStorageConnector Caching(
		final Storage storage
	)
	{
		return new GoogleCloudStorageConnector.Default(
			notNull(storage),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Blob>
	implements GoogleCloudStorageConnector
	{
		private final Storage storage;

		Default(
			final Storage storage  ,
			final boolean withCache
		)
		{
			super(
				Blob::getName,
				Blob::getSize,
				GoogleCloudStoragePathValidator.New(),
				withCache
			);
			this.storage = storage;
		}

		@Override
		protected Stream<Blob> blobs(
			final BlobStorePath file
		)
		{
			final List<Blob> blobs   = new ArrayList<>();
			final String     prefix  = toBlobKeyPrefix(file);
			final Pattern    pattern = Pattern.compile(blobKeyRegex(prefix));
			      Page<Blob> page    = this.storage.list(
				file.container(),
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
				.filter(blob -> pattern.matcher(blob.getName()).matches())
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final List<String> keys    = new ArrayList<>();
			final String       prefix  = toChildKeysPrefix(directory);
			final Pattern      pattern = Pattern.compile(childKeysRegex(directory));
			      Page<Blob>   page    = this.storage.list(
				directory.container(),
				BlobListOption.prefix(prefix)
			);
			while(page != null)
			{
				page.getValues().forEach(blob ->
				{
					final String key = blob.getName();
					if(pattern.matcher(key).matches())
					{
						keys.add(key);
					}
				});

				page = page.hasNextPage()
					? page.getNextPage()
					: null
				;
			}
			return keys.stream();
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final Blob          blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
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
						// empty loop
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
						// empty loop
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

		@Override
		protected boolean internalDirectoryExists(
			final BlobStorePath directory
		)
		{
			final String     key  = toContainerKey(directory);
			      Page<Blob> page = this.storage.list(
			    	  directory.container(),
			    	  BlobListOption.currentDirectory(),
			    	  BlobListOption.prefix(key)
			      );
			while(page != null)
			{
				for(final Blob blob : page.getValues())
				{
					if(blob.getName().equals(key))
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
		protected boolean internalDeleteBlobs(
			final BlobStorePath        file ,
			final List<? extends Blob> blobs
		)
		{
			final List<BlobId>  blobIds = blobs.stream()
				.map(Blob::getBlobId)
				.collect(toList())
			;
			final List<Boolean> results = this.storage.delete(blobIds);
			return !results.stream()
				.anyMatch(b -> b.booleanValue() == false)
			;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			final BlobInfo blobInfo = BlobInfo.newBuilder(
				file.container(),
				toBlobKey(file, nextBlobNumber)
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

			return totalSize;
		}

	}

}
