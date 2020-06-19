package one.microstream.afs.azure.storage;

import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.ByteBufferOutputStream;
import one.microstream.io.LimitedInputStream;


public interface AzureStorageConnector extends BlobStoreConnector
{

	public static AzureStorageConnector New(
		final BlobServiceClient serviceClient
	)
	{
		return new AzureStorageConnector.Default(
			notNull(serviceClient)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobItem>
	implements AzureStorageConnector
	{
		private final BlobServiceClient serviceClient;

		Default(
			final BlobServiceClient serviceClient
		)
		{
			super(
				BlobItem::getName,
				b -> b.getProperties().getContentLength()
			);
			this.serviceClient = serviceClient;
		}

		@Override
		protected Stream<BlobItem> blobs(
			final BlobStorePath file
		)
		{
			final String                  prefix  = toBlobKeyPrefix(file);
			final Pattern                 pattern = Pattern.compile(blobKeyRegex(prefix));
			final PagedIterable<BlobItem> blobs   = this.serviceClient.getBlobContainerClient(
				file.container()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			);
			return blobs.stream()
				.filter(summary -> pattern.matcher(summary.getName()).matches())
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final BlobItem      blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			try(ByteBufferOutputStream outputStream = ByteBufferOutputStream.New(targetBuffer))
			{
				this.serviceClient.getBlobContainerClient(file.container())
					.getBlobClient(blob.getName())
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

		@Override
		protected boolean internalDirectoryExists(
			final BlobStorePath directory
		)
		{
			final String key = toContainerKey(directory);
			final PagedIterable<BlobItem> blobItems = this.serviceClient.getBlobContainerClient(
				directory.container()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(key),
				null
			);
			for(final BlobItem blobItem : blobItems)
			{
				if(blobItem.getName().equals(key))
				{
					return true;
				}
			}

			return false;
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final String                  prefix          = toBlobKeyPrefix(file);
			final Pattern                 pattern         = Pattern.compile(blobKeyRegex(prefix));
			final BlobContainerClient     containerClient = this.serviceClient.getBlobContainerClient(
				file.container()
			);
			final PagedIterable<BlobItem> blobs           = containerClient
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			);
			final AtomicBoolean           deleted         = new AtomicBoolean(false);
			blobs.stream()
				.filter(summary -> pattern.matcher(summary.getName()).matches())
				.forEach(blobItem ->
				{
					containerClient.getBlobClient(blobItem.getName()).delete();
					deleted.set(true);
				})
			;

			return deleted.get();
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			      long nextBlobNr = this.nextBlobNr(file);
			final long totalSize  = this.totalSize(sourceBuffers);

			final ByteBufferInputStream buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			long available = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					BlockBlobClient.MAX_UPLOAD_BLOB_BYTES
				);

				try(LimitedInputStream limitedInputStream = LimitedInputStream.New(
					new BufferedInputStream(buffersInputStream),
					currentBatchSize
				))
				{
					this.serviceClient.getBlobContainerClient(file.container())
						.getBlobClient(toBlobKey(file, nextBlobNr++))
						.getBlockBlobClient()
						.upload(limitedInputStream, currentBatchSize)
					;
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				available -= currentBatchSize;
			}

			return totalSize;
		}

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final BlobContainerClient sourceContainerClient = this.serviceClient.getBlobContainerClient(
				sourceFile.container()
			);
			final BlobContainerClient targetContainerClient = this.serviceClient.getBlobContainerClient(
				targetFile.container()
			);
			this.blobs(sourceFile).forEach(sourceItem ->
			{
				final String url = sourceContainerClient.getBlobClient(
					sourceItem.getName()
				)
				.getBlobUrl();

				targetContainerClient.getBlobClient(
					toBlobKey(
						targetFile,
						this.blobNr(sourceItem)
					)
				)
				.beginCopy(url, null)
				.getFinalResult();
			});

			return this.fileSize(targetFile);
		}

	}

}
