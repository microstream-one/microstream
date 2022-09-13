package one.microstream.afs.oraclecloud.objectstorage.types;

/*-
 * #%L
 * microstream-afs-oraclecloud-objectstorage
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

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.oracle.bmc.io.DuplicatableInputStream;
import com.oracle.bmc.model.Range;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.model.ObjectSummary;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetNamespaceRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.ListObjectsRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration;
import com.oracle.bmc.objectstorage.transfer.UploadManager;
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for the <a href="https://www.oracle.com/cloud/storage/object-storage.html">Oracle cloud object storage</a>.
 * <p>
 * First create a <a href="https://docs.cloud.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm">Oracle storage client</a>.
 * <pre>
 * ObjectStorageClient client = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	OracleCloudObjectStorageConnector.New(client)
 * );
 * </pre>
 *
 * 
 *
 */
public interface OracleCloudObjectStorageConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link OracleCloudObjectStorageConnector}.
	 *
	 * @param client connection to the Oracle cloud object storage
	 * @return a new {@link OracleCloudObjectStorageConnector}
	 */
	public static OracleCloudObjectStorageConnector New(
		final ObjectStorageClient client
	)
	{
		return new OracleCloudObjectStorageConnector.Default(
			notNull(client),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link OracleCloudObjectStorageConnector} with cache.
	 *
	 * @param client connection to the Oracle cloud object storage
	 * @return a new {@link OracleCloudObjectStorageConnector}
	 */
	public static OracleCloudObjectStorageConnector Caching(
		final ObjectStorageClient client
	)
	{
		return new OracleCloudObjectStorageConnector.Default(
			notNull(client),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<ObjectSummary>
	implements OracleCloudObjectStorageConnector
	{
		private final static long MAX_BLOB_SIZE = 53_687_091_200L; // 50 GiB

		private final ObjectStorageClient client;
		private String                    namespaceName;

		Default(
			final ObjectStorageClient client   ,
			final boolean             withCache
		)
		{
			super(
				ObjectSummary::getName,
				ObjectSummary::getSize,
				OracleCloudObjectStoragePathValidator.New(),
				withCache
			);
			this.client = client;
		}

		private String namespaceName()
		{
			if(this.namespaceName == null)
			{
				this.namespaceName = this.client.getNamespace(
					GetNamespaceRequest.builder().build()
				)
				.getValue();
			}

			return this.namespaceName;
		}

		private UploadManager uploadManager()
		{
			return new UploadManager(
				this.client,
				UploadConfiguration.builder()
					.allowMultipartUploads(true)
					.allowParallelUploads(true)
					.build()
			);
		}

		@Override
		protected Stream<ObjectSummary> blobs(
			final BlobStorePath file
		)
		{
			final String  prefix  = toBlobKeyPrefix(file);
			final Pattern pattern = Pattern.compile(blobKeyRegex(prefix));
			return this.client.listObjects(ListObjectsRequest.builder()
				.namespaceName(this.namespaceName())
				.bucketName(file.container())
				.prefix(prefix)
				.fields("name,size")
				.build()
			)
			.getListObjects()
			.getObjects()
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
			return this.client.listObjects(ListObjectsRequest.builder()
				.namespaceName(this.namespaceName())
				.bucketName(directory.container())
				.prefix(toChildKeysPrefix(directory))
				.delimiter(BlobStorePath.SEPARATOR)
				.fields("name")
				.build()
			)
			.getListObjects()
			.getObjects()
			.stream()
			.map(ObjectSummary::getName)
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final ObjectSummary blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final GetObjectResponse response = this.client.getObject(
				GetObjectRequest.builder()
					.namespaceName(this.namespaceName())
					.bucketName(file.container())
					.objectName(blob.getName())
					.range(new Range(offset, offset + length - 1))
					.build()
			);
			try(final InputStream inputStream = response.getInputStream())
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
				throw new IORuntimeException(e);
			}
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                 file ,
			final List<? extends ObjectSummary> blobs
		)
		{
			final AtomicBoolean deleted = new AtomicBoolean(false);
			blobs.forEach(summary ->
			{
				this.client.deleteObject(
					DeleteObjectRequest.builder()
						.namespaceName(this.namespaceName())
						.bucketName(file.container())
						.objectName(summary.getName())
						.build()
				);
				deleted.set(true);
			});

			return deleted.get();
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final UploadManager         uploadManager       = this.uploadManager();
			      long                  nextBlobNumber      = this.nextBlobNumber(file);
			final long                  totalSize           = this.totalSize(sourceBuffers);
			final ByteBufferInputStream buffersInputStream  = ByteBufferInputStream.New(sourceBuffers);
			      long                  available           = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					MAX_BLOB_SIZE
				);

				try(final UploadInputStream uploadInputStream = new UploadInputStream(
					LimitedInputStream.New(
						new BufferedInputStream(buffersInputStream),
						currentBatchSize
					)
				))
				{
					final PutObjectRequest putObjectRequest = PutObjectRequest.builder()
						.namespaceName(this.namespaceName())
						.bucketName(file.container())
						.objectName(toBlobKey(file, nextBlobNumber++))
						.contentLength(currentBatchSize)
						.buildWithoutInvocationCallback()
					;
					final UploadRequest    uploadRequest    = UploadRequest.builder(
							uploadInputStream,
							currentBatchSize
						)
						.allowOverwrite(true)
						.build(putObjectRequest)
					;
					uploadManager.upload(uploadRequest);
				}
				catch(final IOException e)
				{
					throw new IORuntimeException(e);
				}

				available -= currentBatchSize;
			}

			return totalSize;
		}


		private static final class UploadInputStream
		extends FilterInputStream
		implements DuplicatableInputStream
		{
			UploadInputStream(final InputStream in)
			{
				super(in);
			}

			@Override
			public InputStream duplicate()
			{
				return new UploadInputStream(this.in);
			}

		}

	}

}
