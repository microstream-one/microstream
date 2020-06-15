package one.microstream.afs.aws.s3;

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
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

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;


public interface S3Connector extends BlobStoreConnector
{

	public static S3Connector New(
		final AmazonS3 s3
	)
	{
		return new S3Connector.Default(
			notNull(s3)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<S3ObjectSummary>
	implements S3Connector
	{
		private final AmazonS3 s3;

		Default(
			final AmazonS3 s3
		)
		{
			super();
			this.s3 = s3;
		}

		@Override
		protected String key(
			final S3ObjectSummary blob
		)
		{
			return blob.getKey();
		}

		@Override
		protected long size(
			final S3ObjectSummary blob
		)
		{
			return blob.getSize();
		}

		@Override
		protected Stream<S3ObjectSummary> blobs(final BlobStorePath file)
		{
			final String prefix = toBlobKeyPrefix(file);
			return this.s3.listObjectsV2(
				file.container(),
				prefix
			)
			.getObjectSummaries().stream()
			.filter(summary -> isBlobKey(prefix, summary.getKey()))
			.sorted(this.blobComparator())
			;
		}

		@Override
		protected void readBlobData(
			final BlobStorePath   file        ,
			final S3ObjectSummary blob        ,
			final ByteBuffer      targetBuffer,
			final long            offset      ,
			final long            length
		)
		{
			final S3Object            object      = this.s3.getObject(
				new GetObjectRequest(
					blob.getBucketName(),
					blob.getKey()
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
						Math.min(buffer.length, checkArrayRange(remaining)))
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

		@Override
		protected boolean internalDirectoryExists(
			final BlobStorePath directory
		)
		{
			return this.s3.doesObjectExist(
				directory.container(),
				toContainerKey(directory)
			);
		}

		@Override
		protected boolean internalCreateDirectory(
			final BlobStorePath directory
		)
		{
			this.s3.putObject(
				directory.container(),
				toContainerKey(directory),
				""
			);
			return true;
		}

		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			final List<KeyVersion> keys = this.blobs(file)
				.map(summary -> new KeyVersion(summary.getKey()))
				.collect(Collectors.toList())
			;
			if(keys.size() > 0)
			{
				this.s3.deleteObjects(
					new DeleteObjectsRequest(file.container())
						.withKeys(keys)
				);

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

			final ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentLength(totalSize);

			try(final BufferedInputStream inputStream = new BufferedInputStream(
				ByteBufferInputStream.New(sourceBuffers),
				RequestClientOptions.DEFAULT_STREAM_BUFFER_SIZE
			))
			{
				this.s3.putObject(
					file.container(),
					toBlobKeyPrefix(file) + nextBlobNr,
					inputStream,
					objectMetadata
				);
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
			this.blobs(sourceFile).forEach(sourceFileSummary ->
			{
				final long fileNr = this.getBlobNr(sourceFileSummary);
				this.s3.copyObject(
					 sourceFile.container(),
					 sourceFileSummary.getKey(),
					 targetFile.container(),
					 targetKeyPrefix + fileNr
				);
			});

			return this.fileSize(targetFile);
		}

	}

}
