package one.microstream.afs.aws.s3.types;

/*-
 * #%L
 * microstream-afs-aws-s3
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

import static java.util.stream.Collectors.toList;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.internal.util.Mimetype;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Connector for the <a href="https://aws.amazon.com/s3/">Amazon Simple Storage Service (Amazon S3)</a>.
 * <p>
 * First create a connection to the <a href="https://docs.aws.amazon.com/sdk-for-java/v2/developer-guide/s3-examples.html">S3 storage</a>.
 * <pre>
 * S3Client client = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	S3Connector.Caching(client)
 * );
 * </pre>
 *
 * 
 *
 */
public interface S3Connector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link S3Connector}.
	 *
	 * @param s3 connection to the S3 storage
	 * @return a new {@link S3Connector}
	 */
	public static S3Connector New(
		final S3Client s3
	)
	{
		return new S3Connector.Default(
			notNull(s3),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link S3Connector} with cache.
	 *
	 * @param s3 connection to the S3 storage
	 * @return a new {@link S3Connector}
	 */
	public static S3Connector Caching(
		final S3Client s3
	)
	{
		return new S3Connector.Default(
			notNull(s3),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<S3Object>
	implements S3Connector
	{
		private final S3Client s3;

		Default(
			final S3Client s3      ,
			final boolean  useCache
		)
		{
			super(
				S3Object::key,
				S3Object::size,
				S3PathValidator.New(),
				useCache
			);
			this.s3 = s3;
		}

		@Override
		protected Stream<S3Object> blobs(final BlobStorePath file)
		{
			final String  prefix  = toBlobKeyPrefix(file);
			final Pattern pattern = Pattern.compile(blobKeyRegex(prefix));
			final ListObjectsV2Request request = ListObjectsV2Request
                .builder()
                .bucket(file.container())
                .prefix(prefix)
                .build();
			return this.s3.listObjectsV2(request)
				.contents()
				.stream()
				.filter(obj -> pattern.matcher(obj.key()).matches())
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final ListObjectsV2Request request = ListObjectsV2Request
                .builder()
                .bucket(directory.container())
                .prefix(toChildKeysPrefix(directory))
                .delimiter(BlobStorePath.SEPARATOR)
                .build();
			return this.s3.listObjectsV2(request)
				.contents()
				.stream()
				.map(S3Object::key)
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final S3Object      blob        ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final GetObjectRequest request = GetObjectRequest.builder()
				.bucket(file.container())
				.key(blob.key())
				.range("bytes=" + offset + "-" + (offset + length - 1))
				.build()
			;
			final ResponseBytes<GetObjectResponse> response = this.s3.getObjectAsBytes(request);
			targetBuffer.put(response.asByteBuffer());
		}

		@Override
		protected boolean internalDirectoryExists(
			final BlobStorePath directory
		)
		{
			try
			{
				final HeadObjectRequest request = HeadObjectRequest.builder()
					.bucket(directory.container())
					.key(toContainerKey(directory))
					.build()
				;
				this.s3.headObject(request);
				return true;
			}
			catch(final NoSuchKeyException e)
			{
				return false;
			}
		}
		
		@Override
		protected boolean internalFileExists(
			final BlobStorePath file
		)
		{
			try
			{
				return super.internalFileExists(file);
			}
			catch(final NoSuchBucketException e)
			{
				return false;
			}
		}

		@Override
		protected boolean internalCreateDirectory(
			final BlobStorePath directory
		)
		{
			final PutObjectRequest request = PutObjectRequest.builder()
				.bucket(directory.container())
				.key(toContainerKey(directory))
				.build()
			;
			final RequestBody body = RequestBody.empty();
			this.s3.putObject(request, body);
			
			return true;
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath            file,
			final List<? extends S3Object> blobs
		)
		{
			final List<ObjectIdentifier> objects = blobs.stream()
				.map(obj -> ObjectIdentifier.builder().key(obj.key()).build())
				.collect(toList())
			;
			final DeleteObjectsRequest request = DeleteObjectsRequest.builder()
				.bucket(file.container())
				.delete(Delete.builder().objects(objects).build())
				.build()
			;
			final DeleteObjectsResponse response = this.s3.deleteObjects(request);
			return response.deleted().size() == blobs.size();
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			final long nextBlobNumber = this.nextBlobNumber(file);
			final long totalSize      = this.totalSize(sourceBuffers);

			final PutObjectRequest request = PutObjectRequest.builder()
				.bucket(file.container())
				.key(toBlobKey(file, nextBlobNumber))
				.build()
			;
			
			try(final BufferedInputStream inputStream = new BufferedInputStream(
				ByteBufferInputStream.New(sourceBuffers)
			))
			{
				final RequestBody body = RequestBody.fromContentProvider(
					() -> inputStream,
					totalSize,
					Mimetype.MIMETYPE_OCTET_STREAM
				);
				
				this.s3.putObject(request, body);
			}
			catch(final IOException e)
			{
				throw new IORuntimeException(e);
			}

			return totalSize;
		}

	}

}
