package one.microstream.storage.types;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import one.microstream.X;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.storage.exceptions.StorageExceptionIoReading;


public interface StorageFileEntityDataIterator
{
	public long iterateEntityData(
		StorageFile        file           ,
		long               fileOffset     ,
		long               iterationLength,
		EntityDataAcceptor logic
	);
	
	public long bufferCapacity();
	
	public StorageFileEntityDataIterator ensureBufferCapacity(long requiredBufferCapacity);
	
	public void removeBuffer();
	
	
	/* (01.03.2019 TM)NOTE:
	 * Experimental concept to differentiate a type internally without inflating the external API.
	 * In order to being able to use wrapping implementations with that, every method of the internal API
	 * must pass the "actual instance" along to switch back to the wrapper instead of staying in the wrapped instance.
	 */
	public interface Internal extends StorageFileEntityDataIterator
	{
		@Override
		public default long iterateEntityData(
			final StorageFile        file           ,
			final long               fileOffset     ,
			final long               iterationLength,
			final EntityDataAcceptor logic
		)
		{
			this.fillBuffer(this, file, fileOffset, iterationLength);
			return this.iterateFilledBuffer(this, logic);
		}
		
		public default void prepareFile(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageFile                            file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			// no-op by default
		}
		
		public default void wrapUpFile(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageFile                            file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			// no-op by default
		}
		
		public void fillBuffer(
			StorageFileEntityDataIterator.Internal self           ,
			StorageFile                            file           ,
			long                                   fileOffset     ,
			long                                   iterationLength
		);
		
		public long iterateFilledBuffer(
			StorageFileEntityDataIterator.Internal self ,
			EntityDataAcceptor                     logic
		);
		
		public void validateIterationRange(
			StorageFileEntityDataIterator.Internal self            ,
			StorageFile                            file            ,
			long                                   actualFileLength,
			long                                   fileOffset      ,
			long                                   iterationLength
		);
		
	}
	
	
	
	public static StorageFileEntityDataIterator New()
	{
		return new StorageFileEntityDataIterator.Implementation();
	}
	
	public final class Implementation implements StorageFileEntityDataIterator.Internal
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private ByteBuffer directByteBuffer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final long bufferCapacity()
		{
			return this.directByteBuffer != null
				? this.directByteBuffer.capacity()
				: -1L // because 0L is a valid buffer capacity!
			;
		}

		@Override
		public StorageFileEntityDataIterator ensureBufferCapacity(final long requiredBufferCapacity)
		{
			if(this.bufferCapacity() < requiredBufferCapacity)
			{
				XMemory.deallocateDirectByteBuffer(this.directByteBuffer);
				this.directByteBuffer = ByteBuffer.allocateDirect(X.checkArrayRange(requiredBufferCapacity));
			}
			
			return this;
		}
		
		@Override
		public void removeBuffer()
		{
			this.directByteBuffer = null;
		}

		@Override
		public void fillBuffer(
			final StorageFileEntityDataIterator.Internal self           ,
			final StorageFile                            file           ,
			final long                                   fileOffset     ,
			final long                                   iterationLength
		)
		{
			try
			{
				this.prepareFile(self, file, fileOffset, iterationLength);
				
				final FileChannel fileChannel = file.fileChannel();
				self.validateIterationRange(self, file, fileChannel.size(), fileOffset, iterationLength);
				
				self.ensureBufferCapacity(iterationLength);
				final ByteBuffer buffer = this.directByteBuffer;
				buffer.clear();
				buffer.limit(X.checkArrayRange(iterationLength));

				fileChannel.position(fileOffset);
				// loop is guaranteed to terminate as it depends on validated buffer size and the file length
				do
				{
					fileChannel.read(buffer);
				}
				while(buffer.hasRemaining());
			}
			catch(final Exception e)
			{
				throw new StorageExceptionIoReading(e);
			}
			finally
			{
				self.wrapUpFile(self, file, fileOffset, iterationLength);
			}
		}
		
		@Override
		public void validateIterationRange(
			final StorageFileEntityDataIterator.Internal self            ,
			final StorageFile                            file            ,
			final long                                   actualFileLength,
			final long                                   fileOffset      ,
			final long                                   iterationLength
		)
		{
			if(fileOffset < 0 || fileOffset > actualFileLength)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid file offset " + fileOffset
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
			
			if(iterationLength < 0)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid negative iteration length " + iterationLength
					+ " specified for file \"" + file.identifier() + "\"."
				);
			}
			
			if(fileOffset + iterationLength > actualFileLength)
			{
				// (27.02.2019 TM)EXCP: proper exception
				throw new StorageExceptionIoReading(
					"Invalid iteration range [" + fileOffset + "; " + (fileOffset + iterationLength) + "]"
					+ " specified for " + actualFileLength
					+ " bytes long file \"" + file.identifier() + "\"."
				);
			}
		}
		
		@Override
		public long iterateFilledBuffer(
			final StorageFileEntityDataIterator.Internal self ,
			final EntityDataAcceptor                     logic
		)
		{
			final long bufferStartAddress = XMemory.getDirectByteBufferAddress(this.directByteBuffer);
			final long bufferBoundAddress = bufferStartAddress + this.directByteBuffer.position();
			
			// the loop condition must be safe to read the item length
			final long itemStartBoundAddress = bufferBoundAddress - Binary.lengthLength() + 1;
			
			long a = bufferStartAddress;
			while(a < itemStartBoundAddress)
			{
				final long itemLength = XMemory.get_long(a);
				if(itemLength > 0)
				{
					// if the logic did not accept the entity data, iteration is aborted at the start of that entity.
					if(!logic.acceptEntityData(a, bufferBoundAddress))
					{
						break;
					}
					
					// otherwise, the iteration advances to the next item (comment or entity)
					a += itemLength;
				}
				else if(itemLength < 0)
				{
					// comments (indicated by negative length) just get skipped.
					a -= itemLength;
				}
				else
				{
					// entity length may never be 0 or the iteration will hang forever
					throw new StorageException("Zero length data item."); // (28.02.2019 TM)EXCP: proper exception
				}
			}
			
			// the total length of processed items is returned so the calling context can validate/advance/etc.
			return a - bufferStartAddress;
		}
		
	}
	
	
	
	public interface EntityDataAcceptor
	{
		public boolean acceptEntityData(long entityStartAddress, long dataBoundAddress);
	}
	
	

		
}
