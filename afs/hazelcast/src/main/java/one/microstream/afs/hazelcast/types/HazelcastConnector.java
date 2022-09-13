package one.microstream.afs.hazelcast.types;

/*-
 * #%L
 * microstream-afs-hazelcast
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.projection.Projections;
import com.hazelcast.query.Predicates;
import com.hazelcast.query.QueryConstants;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for the <a href="https://hazelcast.org/imdg/">Hazelcast IMDG</a>.
 * <p>
 * First create a connection to a <a href="https://docs.hazelcast.org/docs/latest/manual/html-single/index.html#java-client">Hazelcast instance</a>.
 * <pre>
 * HazelcastInstance hazelcast = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	HazelcastConnector.New(hazelcast)
 * );
 * </pre>
 *
 * 
 *
 */
public interface HazelcastConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link HazelcastConnector}.
	 *
	 * @param hazelcast connection to the Hazelcast instance
	 * @return a new {@link HazelcastConnector}
	 */
	public static HazelcastConnector New(
		final HazelcastInstance hazelcast
	)
	{
		return new Default(
			notNull(hazelcast),
			false
		);
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link HazelcastConnector} with cache.
	 *
	 * @param hazelcast connection to the Hazelcast instance
	 * @return a new {@link HazelcastConnector}
	 */
	public static HazelcastConnector Caching(
		final HazelcastInstance hazelcast
	)
	{
		return new Default(
			notNull(hazelcast),
			true
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements HazelcastConnector
	{
		/*
		 * Blobs are stored as Lists:
		 * LinkedList[                // needed to get projection working ("this.getFirst")
		 *     ArrayList[             // metadata
		 *         String key
		 *         Long   size
		 *     ],
		 *     byte[] data
		 * ]
		 */

		private static List<Object> createBlobList(
			final String identifier,
			final Long   size      ,
			final byte[] data
		)
		{
			final List<Object> metadata = new ArrayList<>(3);
			metadata.add(identifier);
			metadata.add(size);

			final List<Object> blob = new LinkedList<>();
			blob.add(metadata);
			blob.add(data);
			return blob;
		}

		private static byte[] data(
			final List<Object> blobList
		)
		{
			return (byte[])blobList.get(1);
		}

		private final static long MAX_UPLOAD_BLOB_BYTES = 10_000_000L;

		private final HazelcastInstance hazelcast;

		Default(
			final HazelcastInstance hazelcast,
			final boolean           withCache
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size,
				withCache
			);
			this.hazelcast = hazelcast;
		}

		private IMap<String, List<Object>> map(
			final BlobStorePath path
		)
		{
			return this.hazelcast.getMap(path.container());
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			return this.map(file).project(
				Projections.<Entry<String, List<Object>>, List<Object>>singleAttribute(
					QueryConstants.THIS_ATTRIBUTE_NAME.value() + ".getFirst"
				),
				Predicates.regex(
					QueryConstants.KEY_ATTRIBUTE_NAME.value(),
					blobKeyRegex(toBlobKeyPrefix(file))
				)
			)
			.stream()
			.map(list -> BlobMetadata.New((String)list.get(0), (Long)list.get(1)))
			.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			return this.map(directory).project(
				Projections.<Entry<String, List<Object>>, List<Object>>singleAttribute(
					QueryConstants.THIS_ATTRIBUTE_NAME.value() + ".getFirst"
				),
				Predicates.regex(
					QueryConstants.KEY_ATTRIBUTE_NAME.value(),
					childKeysRegex(directory)
				)
			)
			.stream()
			.map(list -> (String)list.get(0))
			;
		}

		@Override
		protected void internalReadBlobData(
			final BlobStorePath file        ,
			final BlobMetadata  metadata    ,
			final ByteBuffer    targetBuffer,
			final long          offset      ,
			final long          length
		)
		{
			final byte[] data = data(
				this.map(file).get(metadata.key())
			);
			targetBuffer.put(
				data,
				checkArrayRange(offset),
				checkArrayRange(length)
			);
		}

		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                file ,
			final List<? extends BlobMetadata> blobs
		)
		{
			final IMap<String, List<Object>> map = this.map(file);
			blobs.forEach(
				metadata -> map.delete(metadata.key())
			);
			return true;
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			      long                       nextBlobNumber     = this.nextBlobNumber(file);
			final long                       totalSize          = this.totalSize(sourceBuffers);
			final IMap<String, List<Object>> map                = this.map(file);
			final ByteBufferInputStream      buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			      long                       available          = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					MAX_UPLOAD_BLOB_BYTES
				);

				try(LimitedInputStream limitedInputStream = LimitedInputStream.New(
					new BufferedInputStream(buffersInputStream),
					currentBatchSize
				))
				{
					final int    batchSize = checkArrayRange(currentBatchSize);
					final byte[] batch     = new byte[batchSize];
					      int    read      = 0;
					do
					{
						read += limitedInputStream.read(batch, read, batch.length - read);
					}
					while(read < batchSize);

					final String       identifier = toBlobKey(file, nextBlobNumber++);
					final List<Object> blob       = createBlobList(
						identifier,
						currentBatchSize,
						batch
					);
					map.set(identifier, blob);
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
		protected void internalMoveFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final IMap<String, List<Object>> sourceMap = this.map(sourceFile);
			final IMap<String, List<Object>> targetMap = this.map(targetFile);
			final AtomicInteger              nr        = new AtomicInteger();
			this.blobs(sourceFile).forEach(metadata -> {
				this.copyBlob(metadata, targetFile, sourceMap, targetMap, nr);
				sourceMap.delete(metadata.key());
			});
		}

		private void copyBlob(
			final BlobMetadata               metadata  ,
			final BlobStorePath              targetFile,
			final IMap<String, List<Object>> sourceMap ,
			final IMap<String, List<Object>> targetMap ,
			final AtomicInteger              nr
		)
		{
			final List<Object> blob             = sourceMap.get(metadata.key());
			final String       targetIdentifier = toBlobKey(targetFile, nr.getAndIncrement());
			final List<Object> newBlob          = createBlobList(
				targetIdentifier,
				metadata.size(),
				data(blob)
			);
			targetMap.put(targetIdentifier, newBlob);
		}

	}

}
