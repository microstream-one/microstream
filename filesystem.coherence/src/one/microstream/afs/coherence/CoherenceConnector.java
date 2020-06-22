package one.microstream.afs.coherence;

import static one.microstream.X.checkArrayRange;
import static one.microstream.X.notNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import com.tangosol.coherence.memcached.processor.DeleteProcessor;
import com.tangosol.net.NamedCache;
import com.tangosol.net.cache.CacheMap;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.LongSum;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.RegexFilter;
import com.tangosol.util.processor.ExtractorProcessor;

import one.microstream.afs.blobstore.BlobStoreConnector;
import one.microstream.afs.blobstore.BlobStorePath;
import one.microstream.exceptions.IORuntimeException;
import one.microstream.io.ByteBufferInputStream;
import one.microstream.io.LimitedInputStream;


public interface CoherenceConnector extends BlobStoreConnector
{

	public static CoherenceConnector New(
		final NamedCache cache
	)
	{
		return new Default(
			notNull(cache)
		);
	}


	public static class Default
	extends    BlobStoreConnector.Abstract<BlobMetadata>
	implements CoherenceConnector
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

		private static ValueExtractor valueExtractor(
			final String key
		)
		{
			return new ReflectionExtractor(
				"get",
				new Object[] {key}
			);
		}

		private static Filter fileFilter(
			final BlobStorePath file
		)
		{
			return new RegexFilter(
				new KeyExtractor(),
				blobKeyRegex(toBlobKeyPrefix(file))
			);
		}

		private final NamedCache cache;

		Default(
			final NamedCache cache
		)
		{
			super(
				BlobMetadata::key,
				BlobMetadata::size
			);
			this.cache = cache;
		}

		@Override
		protected Stream<BlobMetadata> blobs(
			final BlobStorePath file
		)
		{
			@SuppressWarnings("unchecked")
			final Map<String, Long> result = this.cache.invokeAll(
				fileFilter(file),
				new ExtractorProcessor(valueExtractor(SIZE))
			);
			return result.entrySet().stream()
				.map(entry -> BlobMetadata.New(entry.getKey(), entry.getValue()))
				.sorted(this.blobComparator())
			;
		}

		@Override
		protected long internalFileSize(final BlobStorePath file)
		{
			final Long size = (Long)this.cache.aggregate(
				fileFilter(file),
				new LongSum(valueExtractor(SIZE))
			);
			return size != null
				? size
				: -1
			;
		}

		private byte[] data(
			final BlobMetadata metadata
		)
		{
			return (byte[])this.cache.invoke(
				metadata.key(),
				new ExtractorProcessor(valueExtractor(DATA))
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

					final String              key  = toBlobKey(file, nextBlobNumber++);
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

		@Override
		protected long internalCopyFile(
			final BlobStorePath sourceFile,
			final BlobStorePath targetFile
		)
		{
			final AtomicInteger nr   = new AtomicInteger();
			final AtomicLong    size = new AtomicLong();
			this.blobs(sourceFile).forEach(metadata ->{
				this.copyBlob(metadata, targetFile, nr);
				size.addAndGet(metadata.size());
			});
			return size.get();
		}

		private void copyBlob(
			final BlobMetadata  metadata  ,
			final BlobStorePath targetFile,
			final AtomicInteger nr
		)
		{
			this.cache.put(
				toBlobKey(
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
