package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import java.io.IOException;
import java.nio.ByteBuffer;

import one.microstream.afs.types.AReadableFile;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionConsistency;
import one.microstream.storage.exceptions.StorageExceptionIo;


public interface StorageDataFileItemIterator
{
	public default void iterateStoredItems(final AReadableFile file)
	{
		this.iterateStoredItems(file, 0, file.size());
	}
	
	public void iterateStoredItems(AReadableFile file, long startPosition, final long length);



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
			// default-(page-ish)-sized direct byte buffer as default
			return XMemory.allocateDirectNativeDefault();
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
		 * @return a byte buffer of appropriate size to hold the next entity's data to be processed sufficiently.
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
		 */
		public final class ConstantSizedBufferProvider implements BufferProvider
		{
			private final ByteBuffer buffer;

			ConstantSizedBufferProvider(final int bufferCapacity)
			{
				super();
				this.buffer = XMemory.allocateDirectNative(bufferCapacity);
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

				/* if only one entity header fits in the buffer, limit the buffer to one header right away
				 * to avoid filling a giant buffer with an incomplete giant entity and then just read the header.
				 * This is not noticeable in normal situations (tiny to large entities), but allows the initialization
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
			final AReadableFile file         ,
			final P             itemProcessor
		)
			throws IOException
		{
			return processInputFile(file, BufferProvider.New(), itemProcessor, 0, file.size());
		}
		
		// kind of ugly, but I don't know a better way right now to transport two values from one method call.
		static final class NextItemLength
		{
			long value;
		}

		public static <P extends ItemProcessor> P processInputFile(
			final AReadableFile  file          ,
			final BufferProvider bufferProvider,
			final P              itemProcessor ,
			final long           startPosition ,
			final long           length
		)
			throws IOException
		{

			final long actualFileLength    = file.size()           ;
			final long boundPosition       = startPosition + length;
			      long currentFilePosition = startPosition         ;

			if(currentFilePosition < 0 || currentFilePosition > actualFileLength)
			{
				throw new IndexBoundsException(actualFileLength, currentFilePosition);
			}
			if(boundPosition < 0 || boundPosition > actualFileLength)
			{
				throw new IndexBoundsException(actualFileLength, boundPosition);
			}

			final NextItemLength nextItemLength = new NextItemLength();
			
			ByteBuffer buffer = bufferProvider.provideInitialBuffer();

			try
			{
				// process whole file part by part
				while(currentFilePosition < boundPosition)
				{
					// ensure buffer size according to buffer size provider
					buffer = bufferProvider.provideBuffer(buffer, nextItemLength.value);
					
					// end of file special case: adjust buffer limit if buffer would exceed the bounds
					if(currentFilePosition + buffer.limit() >= boundPosition)
					{
						// cast (value range) safety is guaranteed by if above
						buffer.limit((int)(boundPosition - currentFilePosition));
					}
					
					file.readBytes(buffer, currentFilePosition, buffer.limit());
					
					// buffer is guaranteed to be filled exactely to its limit in any case
					final long progress = processBufferedEntities(
						XMemory.getDirectByteBufferAddress(buffer),
						buffer.limit(),
						nextItemLength,
						itemProcessor
					);
					currentFilePosition += progress;
				}
			}
			catch(final Exception e)
			{
				throw new StorageException(
					"currentFilePosition = " + currentFilePosition + ". nextEntityLength = " + nextItemLength.value, e
				);
			}
			
			bufferProvider.cleanUp();
			return itemProcessor;
		}

		private static long processBufferedEntities(
			final long           startAddress    ,
			final long           bufferDataLength,
			final NextItemLength nextItemLength,
			final ItemProcessor  entityProcessor
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
					throw new StorageExceptionConsistency("Zero length data item.");
				}


				// depending on the processor logic, incomplete entity data can still be enough (e.g. only needs header)
				if(!entityProcessor.accept(address, bufferBound - address))
				{
					nextItemLength.value = Math.abs(itemLength);
					
					// advance position to start of current incomplete entity
					return address - startAddress;
				}

				// advance iteration and check for end of current buffered data
				if((address += Math.abs(itemLength)) > entityStartBound)
				{
					// 0 value just to indicate unknown next item length
					nextItemLength.value = 0;
					
					// advance position to start of next entity (or exactely end of data)
					return address - startAddress;
				}
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void iterateStoredItems(
			final AReadableFile file         ,
			final long          startPosition,
			final long          length
		)
		{
			try
			{
				processInputFile(file, this.bufferProvider, this.itemProcessor, startPosition, length);
			}
			catch(final IOException e)
			{
				throw new StorageExceptionIo(e);
			}
		}

	}

}
