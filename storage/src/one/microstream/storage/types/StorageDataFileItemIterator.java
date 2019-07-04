package one.microstream.storage.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;

import one.microstream.memory.PlatformInternals;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;


public interface StorageDataFileItemIterator
{
	public void iterateStoredItems(FileChannel storageData, long startPosition, final long length);



	@FunctionalInterface
	public interface ItemProcessor
	{
		/**
		 * Processes the entity at the given address for the passed availableEntityLength.
		 * If the available length is not sufficient (i.e. the processor requires the full entity but
		 * the available data is incomplete), the processor aborts and returns {@literal false}, signaling the iterator
		 * to reload the entity in the next batch in full.
		 * Otherwise, the processor processes the entity (e.g. only register header values) and returns {@literal true}.
		 *
		 * @param address the address at which the entity data is located (starting with the entity header).
		 * @param remaninigBufferedData the remaining entity data in the buffer, potentially less then the actual entity length.
		 * @return {@literal true} if the entity has been processed, {@literal false} (impliying recall) otherwise.
		 */
		public boolean accept(long address, long remaninigBufferedData);
	}

	@FunctionalInterface
	public interface BufferProvider
	{
		public default ByteBuffer provideInitialBuffer()
		{
			// page-sized direct byte buffer as default
			return ByteBuffer.allocateDirect(XMemory.defaultBufferSize());
		}

		/**
		 * The passed and returned byte buffer instances are guaranteed to be direct bytebuffer.
		 * This has to be ensured via contract instead of proper typing because of the lack of competent typing
		 * in the JDK.
		 * All byte buffer instances passed to this method are guaranteed to have been created by it.
		 * See {@link #provideInitialBuffer()}.
		 *
		 * @param byteBuffer the direct byte buffer used so far.
		 * @param nextEntityLength the length of the next entity to be read to the buffer.
		 * @return a byte buffer of apriorate size to hold the next entity's data to be processed sufficiently.
		 */
		public ByteBuffer provideBuffer(ByteBuffer byteBuffer, long nextEntityLength);

		public default void cleanUp()
		{
			// no explicit clean up be default, content with garbage collector
		}



		public static BufferProvider New()
		{
			return NewConstantSized(0);
		}

		public static BufferProvider NewConstantSized(final int bufferCapacity)
		{
			/* anything below page size is unreasonable and slows down initialization significantly.
			 * Also, this capping passively / automatically defends against nonsense values (<= header length).
			 */
			return new ConstantSizedBufferProvider(Math.max(bufferCapacity, XMemory.defaultBufferSize()));
		}

		/**
		 * Simple implementation that provides always the same {@link ByteBuffer} instance with a fixed size.
		 * This is sufficient for reading all entity headers and having the data available for caching of "small"
		 * entities. Normally, the buffer size is downwards capped at the system's page size which also proved
		 * to be the optimal buffer size (anything large gives no significant advantage, anything lower dramatically
		 * reduces performance. Both not surprising, given the fundamental meaning of a page size).
		 *
		 * @author TM
		 */
		public final class ConstantSizedBufferProvider implements BufferProvider
		{
			private final ByteBuffer buffer;

			ConstantSizedBufferProvider(final int bufferCapacity)
			{
				super();
				this.buffer = ByteBuffer.allocateDirect(bufferCapacity);
			}

			@Override
			public final ByteBuffer provideInitialBuffer()
			{
				return this.buffer;
			}

			@Override
			public final ByteBuffer provideBuffer(final ByteBuffer byteBuffer, final long nextEntityLength)
			{
				// always return standard buffer, logic only reads headers, not content. Clear buffer for next batch.
				this.buffer.clear();

				/* if only one entity header fits int he buffer, limit the buffer to one header right away
				 * to avoid filling a giant buffer with an incomplete giant entity and then just read the header.
				 * This is not noticable in normal situations (tiny to large entities), but allows the initialization
				 * to skip huge parts of pure content data if the database contains giant entities.
				 */
				if(nextEntityLength > this.buffer.capacity())
				{
					this.buffer.limit(Binary.entityHeaderLength());
				}

				return this.buffer;
			}

		}

	}



	public static StorageDataFileItemIterator New(
		final BufferProvider bufferProvider ,
		final ItemProcessor  entityProcessor
	)
	{
		return new Default(bufferProvider, entityProcessor);
	}

	public final class Default implements StorageDataFileItemIterator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final BufferProvider bufferProvider;
		private final ItemProcessor  itemProcessor ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final BufferProvider bufferProvider, final ItemProcessor itemProcessor)
		{
			super();
			this.bufferProvider = bufferProvider;
			this.itemProcessor  = itemProcessor ;
		}



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		public static <P extends ItemProcessor> P processInputFile(
			final FileChannel fileChannel  ,
			final P           itemProcessor
		)
			throws IOException
		{
			return processInputFile(fileChannel, BufferProvider.New(), itemProcessor, 0, fileChannel.size());
		}

		public static <P extends ItemProcessor> P processInputFile(
			final SeekableByteChannel fileChannel    ,
			final BufferProvider      bufferProvider ,
			final P                   itemProcessor  ,
			final long                startPosition  ,
			final long                length
		)
			throws IOException
		{
//			DEBUGStorage.println("Reading file from " + startPosition + " for length " + length);

			final long actualFileLength    = fileChannel.size()    ;
			final long boundPosition       = startPosition + length;
		          long currentFilePosition = startPosition         ;

			if(currentFilePosition < 0 || currentFilePosition > actualFileLength)
			{
				throw new IllegalArgumentException(); // (10.06.2014)EXCP: proper exception
			}
			if(boundPosition < 0 || boundPosition > actualFileLength)
			{
				throw new IllegalArgumentException(); // (10.06.2014)EXCP: proper exception
			}

			      long nextEntityLength = 0;
			ByteBuffer buffer           = bufferProvider.provideInitialBuffer();

			fileChannel.position(startPosition);

			try
			{
				// process whole file part by part
				while(currentFilePosition < boundPosition)
				{
					// ensure buffer size according to buffer size provider
					buffer = bufferProvider.provideBuffer(buffer, nextEntityLength);

					// end of file special case: adjust buffer limit if buffer would exceed the bounds
					if(currentFilePosition + buffer.limit() >= boundPosition)
					{
						// cast (value range) safety is guaranteed by if above
						buffer.limit((int)(boundPosition - currentFilePosition));
					}

					// loop is guaranteed to terminate as it depends on the buffer capacity and the file length
					do
					{
						fileChannel.read(buffer);
					}
					while(buffer.hasRemaining());

					// buffer is guaranteed to be filled exactely to its limit in any case
					nextEntityLength = processBufferedEntities(
						PlatformInternals.getDirectBufferAddress(buffer),
						buffer.limit(),
						fileChannel,
						itemProcessor
					);
					currentFilePosition = fileChannel.position();
				}
			}
			catch(final Exception e)
			{
				// (04.12.2014 TM)EXCP: proper exception
				throw new RuntimeException(
					"currentFilePosition = " + currentFilePosition + ". nextEntityLength = " + nextEntityLength, e
				);
			}

			bufferProvider.cleanUp();
			return itemProcessor;
		}

		private static long processBufferedEntities(
			final long                startAddress    ,
			final long                bufferDataLength,
			final SeekableByteChannel fileChannel     ,
			final ItemProcessor       entityProcessor
		)
			throws IOException
		{
			// bufferBound is the bounding address (exclusive) of the data available in the buffer
			final long bufferBound      = startAddress + bufferDataLength;

			// every entity start must be at least one long size before the actual bound to safely read its length
			final long entityStartBound = bufferBound - Long.BYTES;

			// iteration variable, initialized with the data start address
			long address = startAddress;

			// total byte length of the current entity. Invalid initial value, must be replaced in any case
			long itemLength = 0;

			// iterate over and process every complete entity record, skip all gaps, revert trailing complete entity
			while(true) // loop gets terminated by end-of-data recognition logic specific to the found case
			{
				// read length of current item (entity or gap)
				itemLength = Binary.getEntityLengthRawValue(address);

				if(itemLength == 0)
				{
					// entity length may never be 0 or the iteration will hang forever
					throw new RuntimeException("Zero length data item."); // (29.08.2014)EXCP: proper exception
				}

//				DEBUGStorage.println("processing entity at " + (fileChannel.position() + address - bufferBound) + " / " + fileChannel.size());

				// depending on the processor logic, incomplete entity data can still be enough (e.g. only needs header)
				if(!entityProcessor.accept(address, bufferBound - address))
				{
					// revert fileChannel position to start of current incomplete entity, return its length
					fileChannel.position(fileChannel.position() + address - bufferBound); // current address!
					return Math.abs(itemLength);
				}

				// advance iteration and check for end of current buffered data
				if((address += Math.abs(itemLength)) > entityStartBound)
				{
					// revert fileChannel position to start of next item (of currently unknowable length)
					fileChannel.position(fileChannel.position() + address - bufferBound);
					return 0;
				}
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void iterateStoredItems(
			final FileChannel fileChannel  ,
			final long        startPosition,
			final long        length
		)
		{
			try
			{
				processInputFile(fileChannel, this.bufferProvider, this.itemProcessor, startPosition, length);
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e); // (02.10.2014 TM)EXCP: proper exception
			}
		}

	}

}
