package net.jadoth.storage.types;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import net.jadoth.X;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.storage.exceptions.StorageException;
import net.jadoth.storage.exceptions.StorageExceptionIoReading;

@FunctionalInterface
public interface StorageFileEntityDataIterator
{
	public long iterateEntityData(
		StorageFile        file           ,
		long               fileOffset     ,
		long               iterationLength,
		EntityDataAcceptor logic
	);
	
	
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
			this.fillBuffer(file, fileOffset, iterationLength);
			return this.iterateFilledBuffer(logic);
		}
		
		public default void prepareFile(
			final StorageFile file           ,
			final long        fileOffset     ,
			final long        iterationLength
		)
		{
			// no-op by default
		}
		
		public default void wrapUpFile(
			final StorageFile file           ,
			final long        fileOffset     ,
			final long        iterationLength
		)
		{
			// no-op by default
		}
		
		public void fillBuffer(
			StorageFile file           ,
			long        fileOffset     ,
			long        iterationLength
		);
		
		public long iterateFilledBuffer(
			EntityDataAcceptor logic
		);
		
		public void validateIterationRange(
			StorageFile file            ,
			long        actualFileLength,
			long        fileOffset      ,
			long        iterationLength
		);
		
	}
	
	
	
	public final class Implementation implements StorageFileEntityDataIterator.Internal
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ByteBuffer directByteBuffer;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final ByteBuffer directByteBuffer)
		{
			super();
			this.directByteBuffer = directByteBuffer;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void fillBuffer(final StorageFile file, final long fileOffset, final long iterationLength)
		{
			final ByteBuffer buffer = this.directByteBuffer;
			
			try
			{
				this.prepareFile(file, fileOffset, iterationLength);
				
				final FileChannel fileChannel = file.fileChannel();
				this.validateIterationRange(file, fileChannel.size(), fileOffset, iterationLength);
				fileChannel.position(fileOffset);

				buffer.clear();
				buffer.limit(X.checkArrayRange(iterationLength));

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
				this.wrapUpFile(file, fileOffset, iterationLength);
			}
		}
		
		@Override
		public void validateIterationRange(
			final StorageFile file            ,
			final long        actualFileLength,
			final long        fileOffset      ,
			final long        iterationLength
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
		public long iterateFilledBuffer(final EntityDataAcceptor logic)
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
	
	
	@FunctionalInterface
	public interface Creator
	{
		public StorageFileEntityDataIterator createStorageFileEntityIterator(
			long requiredBufferCapacity
		);
		
	}
	
}
