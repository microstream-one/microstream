package one.microstream.afs.googlecloud.firestore.types;

/*-
 * #%L
 * microstream-afs-googlecloud-firestore
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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Blob;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.WriteBatch;
import com.google.cloud.firestore.WriteResult;
import com.google.protobuf.UnsafeByteOperations;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.chars.VarString;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for the <a href="https://cloud.google.com/firestore">Google cloud firestore</a>.
 * <p>
 * First create a <a href="https://cloud.google.com/firestore/docs/quickstart-servers">firestore connection</a>.
 * <pre>
 * Firestore firestore = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	GoogleCloudFirestoreConnector.New(firestore)
 * );
 * </pre>
 *
 * 
 *
 */
public interface GoogleCloudFirestoreConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link GoogleCloudFirestoreConnector}.
	 *
	 * @param firestore connection to the Google firestore service
	 * @return a new {@link GoogleCloudFirestoreConnector}
	 */
	public static GoogleCloudFirestoreConnector New(
		final Firestore firestore
	)
	{
		return new GoogleCloudFirestoreConnector.Default(
			notNull(firestore),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link GoogleCloudFirestoreConnector} with cache.
	 *
	 * @param firestore connection to the Google firestore service
	 * @return a new {@link GoogleCloudFirestoreConnector}
	 */
	public static GoogleCloudFirestoreConnector Caching(
		final Firestore firestore
	)
	{
		return new GoogleCloudFirestoreConnector.Default(
			notNull(firestore),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<DocumentSnapshot>
	implements GoogleCloudFirestoreConnector
	{
		private final static String FIELD_KEY     = "key" ;
		private final static String FIELD_SIZE    = "size";
		private final static String FIELD_DATA    = "data";

		// https://firebase.google.com/docs/firestore/quotas
		private final static long MAX_BLOB_SIZE    =  1_000_000L;
		private final static long MAX_REQUEST_SIZE = 10_000_000L;

		private final Firestore firestore;

		Default(
			final Firestore firestore,
			final boolean   withCache
		)
		{
			super(
				blob -> blob.getString(FIELD_KEY ),
				blob -> blob.getLong  (FIELD_SIZE),
				GoogleCloudFirestorePathValidator.New(),
				withCache
			);
			this.firestore = firestore;
		}

		private CollectionReference collection(
			final BlobStorePath path
		)
		{
			return this.firestore.collection(path.container());
		}

		private Stream<? extends DocumentSnapshot> blobs(
			final BlobStorePath file    ,
			final boolean       withData
		)
		{
			try
			{
				final String prefix = toBlobKeyPrefix(file);
				final Pattern pattern = Pattern.compile(blobKeyRegex(prefix));
				Query query = this.collection(file)
					.whereGreaterThan(FIELD_KEY, prefix);
				if(!withData)
				{
					query = query.select(FIELD_KEY, FIELD_SIZE);
				}
				return StreamSupport.stream(
					query.get().get().spliterator(),
					false
				)
				.filter(document -> pattern.matcher(document.getString(FIELD_KEY)).matches())
				.sorted(this.blobComparator())
				;
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			try
			{
				final String  prefix  = toChildKeysPrefix(directory);
				final Pattern pattern = Pattern.compile(childKeysRegex(directory));
				final Query   query   = this.collection(directory)
					.whereGreaterThan(FIELD_KEY, prefix)
					.select(FIELD_KEY)
				;
				return StreamSupport.stream(
					query.get().get().spliterator(),
					false
				)
				.map(document -> document.getString(FIELD_KEY))
				.filter(key -> pattern.matcher(key).matches())
				;
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		private String documentPath(
			final BlobStorePath file  ,
			final long          blobNr
		)
		{
			return VarString.New()
				.add(file.container())
				.add("/")
				.add(
					Arrays.stream(file.pathElements())
						.skip(1L)
						.collect(Collectors.joining("_"))
				)
				.add(NUMBER_SUFFIX_SEPARATOR_CHAR)
				.add(blobNr)
				.toString();
		}

		@Override
		protected Stream<? extends DocumentSnapshot> blobs(
			final BlobStorePath file
		)
		{
			return this.blobs(file, false);
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath    file        ,
			final DocumentSnapshot blob        ,
			final ByteBuffer       targetBuffer,
			final long             offset      ,
			final long             length
		)
		{
			try
			{
				/*
				 *  Fetch blob again with data.
				 *  Per default they are loaded without it.
				 */
				final DocumentSnapshot fullBlob = this.firestore.document(blob.getReference()
					.getPath()).get().get();
				final byte[]           bytes    = fullBlob.getBlob(FIELD_DATA).toBytes();
				targetBuffer.put(
					bytes,
					checkArrayRange(offset),
					checkArrayRange(length)
				);
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                    file ,
			final List<? extends DocumentSnapshot> blobs
		)
		{
			try
			{
				final List<WriteResult> results = ApiFutures.allAsList(
					blobs.stream()
						.map(blob -> blob.getReference().delete())
						.collect(Collectors.toList())
				)
				.get();

				return results.size() == blobs.size();
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			try
			{
				      WriteBatch            writeBatch         = this.firestore.batch();
				final long                  totalSize          = this.totalSize(sourceBuffers);
				final ByteBufferInputStream buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			          long                  nextBlobNumber     = this.nextBlobNumber(file);
				      long                  available          = totalSize;
				while(available > 0)
				{
					final long currentBatchSize = Math.min(
						available,
						MAX_BLOB_SIZE
					);
					try(LimitedInputStream limitedInputStream = LimitedInputStream.New(
						new BufferedInputStream(buffersInputStream),
						currentBatchSize))
					{
						final byte[] batch = new byte[checkArrayRange(currentBatchSize)];
							  int    remaining = batch.length;
					          int    read;
						while(remaining > 0 &&
							(read = limitedInputStream.read(
								batch,
								0,
								Math.min(batch.length, remaining))
							) != -1
						)
						{
							remaining -= read;
						}

						final Map<String, Object> map = new HashMap<>();
						map.put(FIELD_KEY, toBlobKey(file, nextBlobNumber++));
						map.put(FIELD_SIZE, currentBatchSize);
						map.put(FIELD_DATA, Blob.fromByteString(
							// unsafe wrap is enough because batch is already a copy
							UnsafeByteOperations.unsafeWrap(batch)
						));

						writeBatch.set(
							this.firestore.document(
								this.documentPath(file, nextBlobNumber++)
							),
							map
						);

						if(writeBatch.getMutationsSize() * MAX_BLOB_SIZE >= MAX_REQUEST_SIZE)
						{
							writeBatch.commit().get();
							writeBatch = this.firestore.batch();
						}
					}
					catch(final IOException e)
					{
						throw new IORuntimeException(e);
					}

					available -= currentBatchSize;
				}

				if(writeBatch.getMutationsSize() > 0)
				{
					writeBatch.commit().get();
				}

				return totalSize;
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
	}

}
