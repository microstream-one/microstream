package one.microstream.afs.googlecloud.storage;

import static java.util.stream.Collectors.toList;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
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

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.memory.XMemory;


public interface GoogleCloudStorageConnector extends BlobStoreConnector
{

	public static GoogleCloudStorageConnector New(
		final Storage storage
	)
	{
		return new GoogleCloudStorageConnector.Default(
			notNull(storage)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<Blob>
	implements GoogleCloudStorageConnector
	{
		private final Storage storage;

		Default(
			final Storage storage
		)
		{
			super();
			this.storage = storage;
		}

		@Override
		protected String key(
			final Blob blob
		)
		{
			return blob.getName();
		}

		@Override
		protected long size(
			final Blob blob
		)
		{
			return blob.getSize();
		}

		@Override
		protected Stream<Blob> blobs(
			final BlobStorePath file
		)
		{
			final List<Blob> blobs  = new ArrayList<>();
			final String     prefix = toBlobKeyPrefix(file);
			      Page<Blob> page   = this.storage.list(
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
				.filter(blob -> isBlobKey(prefix, blob.getName()))
				.sorted((s1, s2) -> Long.compare(this.getBlobNr(s1), this.getBlobNr(s2)))
			;
		}

		@Override
		protected void readBlobData(
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
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final List<BlobId> blobIds = this.blobs(file)
				.map(Blob::getBlobId)
				.collect(toList())
			;
			if(blobIds.size() > 0)
			{
				this.storage.delete(blobIds);

				return true;
			}

			return false;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNr = this.nextBlobNr(file);
			final long totalSize  = this.totalSize(sourceBuffers);

			final BlobInfo blobInfo = BlobInfo.newBuilder(
				file.container(),
				toBlobKeyPrefix(file) + nextBlobNr
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

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final String targetKeyPrefix = toBlobKeyPrefix(targetFile);
			this.blobs(sourceFile).forEach(sourceBlob ->
			{
				final BlobInfo targetBlobInfo = BlobInfo.newBuilder(
					targetFile.container(),
					targetKeyPrefix + this.getBlobNr(sourceBlob)
				)
				.build();
				this.storage.copy(
					CopyRequest.of(sourceBlob.getBlobId(), targetBlobInfo)
				);
			});

			return this.fileSize(targetFile);
		}

	}

}
