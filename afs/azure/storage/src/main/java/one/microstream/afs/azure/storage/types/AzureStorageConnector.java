package one.microstream.afs.azure.storage.types;

/*-
 * #%L
 * microstream-afs-azure-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
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

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.ByteBufferOutputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for the <a href="https://azure.microsoft.com/services/storage/blobs/">Azure blob storage</a>.
 * <p>
 * First create a <a href="https://docs.microsoft.com/azure/storage/common/storage-samples-java">BlobServiceClient</a>.
 * <pre>
 * BlobServiceClient serviceClient = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	AzureStorageConnector.New(serviceClient)
 * );
 * </pre>
 *
 * 
 *
 */
public interface AzureStorageConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link AzureStorageConnector}.
	 *
	 * @param serviceClient connection to the Azure storage service
	 * @return a new {@link AzureStorageConnector}
	 */
	public static AzureStorageConnector New(
		final BlobServiceClient serviceClient
	)
	{
		return new AzureStorageConnector.Default(
			notNull(serviceClient),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link AzureStorageConnector} with cache.
	 *
	 * @param serviceClient connection to the Azure storage service
	 * @return a new {@link AzureStorageConnector}
	 */
	public static AzureStorageConnector Caching(
		final BlobServiceClient serviceClient
	)
	{
		return new AzureStorageConnector.Default(
			notNull(serviceClient),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobItem>
	implements AzureStorageConnector
	{
		private final BlobServiceClient serviceClient;

		Default(
			final BlobServiceClient serviceClient,
			final boolean           useCache
		)
		{
			super(
				BlobItem::getName,
				b -> b.getProperties().getContentLength(),
				AzureStoragePathValidator.New(),
				useCache
			);
			this.serviceClient = serviceClient;
		}

		@Override
		protected Stream<BlobItem> blobs(
			final BlobStorePath file
		)
		{
			final String  prefix  = toBlobKeyPrefix(file);
			final Pattern pattern = Pattern.compile(blobKeyRegex(prefix));
			return this.serviceClient.getBlobContainerClient(
				file.container()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			)
			.stream()
			.filter(summary -> pattern.matcher(summary.getName()).matches())
			.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final String       prefix  = toChildKeysPrefix(directory);
			final Pattern      pattern = Pattern.compile(childKeysRegex(directory));
			return this.serviceClient.getBlobContainerClient(
				directory.container()
			)
			.listBlobs(
				new ListBlobsOptions().setPrefix(prefix),
				null
			)
			.stream()
			.map(BlobItem::getName)
			.filter(key -> pattern.matcher(key).matches())
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
		protected boolean internalDeleteBlobs(
			final BlobStorePath            file ,
			final List<? extends BlobItem> blobs
		)
		{
			final BlobContainerClient containerClient = this.serviceClient.getBlobContainerClient(
				file.container()
			);
			blobs.forEach(
				blobItem -> containerClient.getBlobClient(blobItem.getName()).delete()
			);
			return true;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			      long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			final ByteBufferInputStream buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			long available = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					BlockBlobClient.MAX_STAGE_BLOCK_BYTES_LONG
				);

				try(LimitedInputStream limitedInputStream = LimitedInputStream.New(
					new BufferedInputStream(buffersInputStream),
					currentBatchSize
				))
				{
					this.serviceClient.getBlobContainerClient(file.container())
						.getBlobClient(toBlobKey(file, nextBlobNumber++))
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

	}

}
