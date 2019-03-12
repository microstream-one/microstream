package one.microstream.persistence.binary.types;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;


public interface BinaryEntityDataIterator
{
	public long iterateFilledBuffer(
		long                     startAddress      ,
		long                     boundAddress      ,
		BinaryEntityDataAcceptor entityDataAcceptor
	);
	
	
	public static BinaryEntityDataIterator New()
	{
		return new BinaryEntityDataIterator.Implementation();
	}
	
	public final class Implementation implements BinaryEntityDataIterator
	{
		
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
		public long iterateFilledBuffer(
			final long                     startAddress      ,
			final long                     boundAddress      ,
			final BinaryEntityDataAcceptor entityDataAcceptor
		)
		{
			// the loop condition must be safe to read the item length
			final long itemStartBoundAddress = boundAddress - Binary.lengthLength() + 1;
			
			long address = startAddress;
			while(address < itemStartBoundAddress)
			{
				final long itemLength = XMemory.get_long(address);
				if(itemLength > 0)
				{
					// if the logic did not accept the entity data, iteration is aborted at the start of that entity.
					if(!entityDataAcceptor.acceptEntityData(address, boundAddress))
					{
						break;
					}
					
					// otherwise, the iteration advances to the next item (comment or entity)
					address += itemLength;
				}
				else if(itemLength < 0)
				{
					// comments (indicated by negative length) just get skipped.
					address -= itemLength;
				}
				else
				{
					// (28.02.2019 TM)EXCP: proper exception
					// entity length may never be 0 or the iteration will hang forever
					throw new BinaryPersistenceException("Zero length data item.");
				}
			}
			
			// the total length of processed items is returned so the calling context can validate/advance/etc.
			return address - startAddress;
		}
		
	}
	
	public static BinaryEntityDataIterator.Provider Provider()
	{
		return new BinaryEntityDataIterator.Provider.Implementation();
	}
	
	public interface Provider
	{
		public BinaryEntityDataIterator provideEntityDataIterator();
		
		public final class Implementation implements BinaryEntityDataIterator.Provider
		{
			@Override
			public BinaryEntityDataIterator provideEntityDataIterator()
			{
				return BinaryEntityDataIterator.New();
			}
			
		}
		
	}
		
}
