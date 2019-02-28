package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;

@FunctionalInterface
public interface StorageDataFileValidator
{
	public void validateFile(
		StorageFile file           ,
		long        fileOffset     ,
		long        iterationLength
	);
	
	public default void validateFile(final StorageFile file)
	{
		this.validateFile(file, 0, file.length());
	}
	
	public default void freeMemory()
	{
		// no-op by default
	}
	
	
	public final class Implementation implements StorageDataFileValidator, StorageFileEntityDataIterator.EntityDataAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageTypeDictionary         typeDictionary;
		private final StorageFileEntityDataIterator fileIterator  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final StorageTypeDictionary         typeDictionary,
			final StorageFileEntityDataIterator fileIterator
		)
		{
			super();
			this.typeDictionary = typeDictionary;
			this.fileIterator   = fileIterator  ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void freeMemory()
		{
			this.fileIterator.removeBuffer();
		}

		@Override
		public final void validateFile(
			final StorageFile file           ,
			final long        fileOffset     ,
			final long        iterationLength
		)
		{
			this.fileIterator.iterateEntityData(file, fileOffset, iterationLength, this);
		}

		@Override
		public final boolean acceptEntityData(
			final long entityStartAddress,
			final long dataBoundAddress
		)
		{
			if(entityStartAddress + Binary.entityHeaderLength() > dataBoundAddress)
			{
				return false;
			}
			
			final long length   = Binary.getEntityLengthRawValue(entityStartAddress);
			final long typeId   = Binary.getEntityTypeIdRawValue(entityStartAddress);
			final long objectId = Binary.getEntityObjectIdRawValue(entityStartAddress);
			
			final StorageEntityTypeHandler typeHandler = this.typeDictionary.lookupTypeHandlerChecked(typeId);
			typeHandler.validateEntityGuaranteedType(length, objectId);
			
			return true;
		}
		
	}
	
}
