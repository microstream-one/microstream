package one.microstream.afs.oracle.coherence.types;

/*-
 * #%L
 * microstream-afs-oracle-coherence
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

import static java.util.stream.Collectors.toSet;
import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.tangosol.coherence.memcached.processor.DeleteProcessor;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.LongSum;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.InFilter;
import com.tangosol.util.filter.RegexFilter;
import com.tangosol.util.processor.ExtractorProcessor;

import one.microstream.afs.blobstore.types.BlobStoreConnector;
import one.microstream.afs.blobstore.types.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;

/**
 * Connector for the <a href="https://www.oracle.com/middleware/technologies/coherence.html">Oracle Coherence data grid</a>.
 * <p>
 * First create a connection to a <a href="https://docs.oracle.com/cd/E15357_01/coh.360/e15726/gs_example.htm">named cache</a>.
 * <pre>
 * NamedCache cache = ...
 * BlobStoreFileSystem fileSystem = BlobStoreFileSystem.New(
 * 	OracleCoherenceConnector.New(cache)
 * );
 * </pre>
 *
 * 
 *
 */
public interface OracleCoherenceConnector extends BlobStoreConnector
{
	/**
	 * Pseudo-constructor method which creates a new {@link OracleCoherenceConnector}.
	 *
	 * @param cacheName name of the coherence cache
	 * @return a new {@link OracleCoherenceConnector}
	 */
	public static OracleCoherenceConnector New(final String cacheName)
	{
		return New(CacheFactory.getCache(cacheName));
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link OracleCoherenceConnector} with cache.
	 *
	 * @param cacheName name of the coherence cache
	 * @return a new {@link OracleCoherenceConnector}
	 */
	public static OracleCoherenceConnector Caching(final String cacheName)
	{
		return Caching(CacheFactory.getCache(cacheName));
	}
	
	/**
	 * Pseudo-constructor method which creates a new {@link OracleCoherenceConnector}.
	 *
	 * @param cache connection to the coherence caching service
	 * @return a new {@link OracleCoherenceConnector}
	 */
	public static OracleCoherenceConnector New(
		final NamedCache<String, Map<String, Object>> cache
	)
	{
		return new Default(
			notNull(cache),
			false
		);
	}

	/**
	 * Pseudo-constructor method which creates a new {@link OracleCoherenceConnector} with cache.
	 *
	 * @param cache connection to the coherence caching service
	 * @return a new {@link OracleCoherenceConnector}
	 */
	public static OracleCoherenceConnector Caching(
		final NamedCache<String, Map<String, Object>> cache
	)
	{
		return new Default(
			notNull(cache),
			true
		);
	}

	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements OracleCoherenceConnector
	{
		/*
		 * Blobs are stored as Maps:
		 * Cache {
		 *     "key"   : String
		 *     "value" : Map {
		 *         "size" : Long
		 *         "data" : byte[]
		 *     }
		 * }
		 */

		private final static String SIZE = "size";
		private final static String DATA = "data";

		private final static long MAX_BLOB_SIZE = 16_777_216;

		private static Map<String, Object> createBlobValue(
			final Long   size,
			final byte[] data
		)
		{
			final Map<String, Object> blob = new HashMap<>(2);
			blob.put(SIZE, size);
			blob.put(DATA, data);
			return blob;
		}

		private static <T> ValueExtractor<Map<String, Object>, T> valueExtractor(
			final String key
		)
		{
			return new ReflectionExtractor<>(
				"get",
				new Object[] {key}
			);
		}

		private static Filter<String> fileFilter(
			final BlobStorePath file
		)
		{
			return new RegexFilter<>(
				new KeyExtractor<>(),
				blobKeyRegex(toBlobKeyPrefixWithContainer(file))
			);
		}

		private static Filter<String> childKeysFilter(
			final BlobStorePath directory
		)
		{
			return new RegexFilter<>(
				new KeyExtractor<>(),
				childKeysRegexWithContainer(directory)
			);
		}

		private static Filter<String> blobsFilter(
			final List<? extends BlobMetadata> blobs
		)
		{
			return new InFilter<>(
				new KeyExtractor<>(),
				blobs.stream()
					.map(BlobMetadata::key)
					.collect(toSet())
			);
		}

		private final NamedCache<String, Map<String, Object>> cache;

		Default(
			final NamedCache<String, Map<String, Object>> cache    ,
			final boolean                                 withCache
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size,
				withCache
			);
			this.cache = cache;
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			final Map<String, Long> result = this.cache.invokeAll(
				fileFilter(file),
				new ExtractorProcessor<>(valueExtractor(SIZE))
			);
			return result.entrySet().stream()
				.map(entry -> BlobMetadata.New(entry.getKey(), entry.getValue()))
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected Stream<String> childKeys(
			final BlobStorePath directory
		)
		{
			final Map<String, ?> result = this.cache.invokeAll(
				childKeysFilter(directory),
				new ExtractorProcessor<>(new KeyExtractor<>())
			);
			return result.keySet().stream();
		}

		@Override
		protected long internalFileSize(final BlobStorePath file)
		{
			final Long size = this.cache.aggregate(
				fileFilter(file),
				new LongSum<>(valueExtractor(SIZE))
			);
			return size != null
				? size
				: 0L
			;
		}

		private byte[] data(
			final BlobMetadata metadata
		)
		{
			return (byte[])this.cache.invoke(
				metadata.key(),
				new ExtractorProcessor<>(valueExtractor(DATA))
			);
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
			targetBuffer.put(
				this.data(metadata),
				checkArrayRange(offset),
				checkArrayRange(length)
			);
		}

		@SuppressWarnings("unchecked") // DeleteProcessor is not typed
		@Override
		protected boolean internalDeleteFile(
			final BlobStorePath file
		)
		{
			return !this.cache.invokeAll(
				fileFilter(file),
				new DeleteProcessor()
			)
			.isEmpty();
		}

		@SuppressWarnings("unchecked") // DeleteProcessor is not typed
		@Override
		protected boolean internalDeleteBlobs(
			final BlobStorePath                file ,
			final List<? extends BlobMetadata> blobs
		)
		{
			return !this.cache.invokeAll(
				blobsFilter(blobs),
				new DeleteProcessor()
			)
			.isEmpty();
		}

		@Override
		protected long internalWriteData(
			final BlobStorePath                  file         ,
			final Iterable<? extends ByteBuffer> sourceBuffers
		)
		{
			      long                  nextBlobNumber     = this.nextBlobNumber(file);
			final long                  totalSize          = this.totalSize(sourceBuffers);
			final ByteBufferInputStream buffersInputStream = ByteBufferInputStream.New(sourceBuffers);
			      long                  available          = totalSize;
			while(available > 0)
			{
				final long currentBatchSize = Math.min(
					available,
					MAX_BLOB_SIZE
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

					final String              key  = toBlobKeyWithContainer(file, nextBlobNumber++);
					final Map<String, Object> blob = createBlobValue(
						currentBatchSize,
						batch
					);
					this.cache.put(
						key,
						blob,
						CacheMap.EXPIRY_NEVER
					);
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
			final AtomicInteger nr = new AtomicInteger();
			this.blobs(sourceFile).forEach(metadata -> {
				this.copyBlob(metadata, targetFile, nr);
				this.cache.remove(metadata.key());
			});
		}

		private void copyBlob(
			final BlobMetadata  metadata  ,
			final BlobStorePath targetFile,
			final AtomicInteger nr
		)
		{
			this.cache.put(
				toBlobKeyWithContainer(
					targetFile,
					nr.getAndIncrement()
				),
				createBlobValue(
					metadata.size(),
					this.data(metadata)
				)
			);
		}

	}

}
