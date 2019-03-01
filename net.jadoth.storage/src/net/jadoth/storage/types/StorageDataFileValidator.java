package net.jadoth.storage.types;

import static net.jadoth.X.notNull;

import net.jadoth.meta.XDebug;
import net.jadoth.persistence.binary.types.Binary;


public interface StorageDataFileValidator
{
	public void validateFile(
		StorageNumberedFile file           ,
		long                fileOffset     ,
		long                iterationLength
	);
	
	public default void validateFile(final StorageNumberedFile file)
	{
		this.validateFile(file, 0, file.length());
	}
	
	public void validateEntity(long length, long typeId, long objectId);
	
	public default void freeMemory()
	{
		// no-op by default
	}
	
	
	public static StorageDataFileValidator New(
		final StorageTypeDictionary         typeDictionary,
		final StorageFileEntityDataIterator fileIterator
	)
	{
		return new StorageDataFileValidator.Implementation(
			notNull(typeDictionary),
			notNull(fileIterator)
		);
	}
	
	@Deprecated
	public static StorageDataFileValidator DebugLogging(
		final StorageTypeDictionary         typeDictionary,
		final StorageFileEntityDataIterator fileIterator
	)
	{
		return new StorageDataFileValidator.Implementation(
			notNull(typeDictionary),
			notNull(fileIterator)
		){
			@Override
			public void validateFile(final StorageNumberedFile file, final long fileOffset, final long iterationLength)
			{
				if(!Storage.isDataFile(file))
				{
					return;
				}
				
				XDebug.println("Validating file " + file.identifier() + "[" + fileOffset + ";" + (fileOffset + iterationLength) + "[");
				super.validateFile(file, fileOffset, iterationLength);
			}
			
			@Override
			public void validateEntity(final long length, final long typeId, final long objectId)
			{
				XDebug.println("Validating entity [" + length + "][" + typeId + "][" + objectId + "]");
				super.validateEntity(length, typeId, objectId);
			}
		};
	}
	
	public class Implementation implements StorageDataFileValidator, StorageFileEntityDataIterator.EntityDataAcceptor
	{
		///////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageTypeDictionary         typeDictionary;
		private final StorageFileEntityDataIterator fileIterator  ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(
			final StorageTypeDictionary         typeDictionary,
			final StorageFileEntityDataIterator fileIterator
		)
		{
			super();
			this.typeDictionary = notNull(typeDictionary);
			this.fileIterator   = notNull(fileIterator)  ;
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
		public void validateFile(
			final StorageNumberedFile file           ,
			final long                fileOffset     ,
			final long                iterationLength
		)
		{
			if(!Storage.isDataFile(file))
			{
				return;
			}
			
			this.fileIterator.iterateEntityData(file, fileOffset, iterationLength, this);
		}

		@Override
		public boolean acceptEntityData(
			final long entityStartAddress,
			final long dataBoundAddress
		)
		{
			if(entityStartAddress + Binary.entityHeaderLength() > dataBoundAddress)
			{
				return false;
			}
			
			this.validateEntity(
				Binary.getEntityLengthRawValue(entityStartAddress)  ,
				Binary.getEntityTypeIdRawValue(entityStartAddress)  ,
				Binary.getEntityObjectIdRawValue(entityStartAddress)
			);
			
			return true;
		}
		
		@Override
		public void validateEntity(final long length, final long typeId, final long objectId)
		{
			final StorageEntityTypeHandler typeHandler = this.typeDictionary.lookupTypeHandlerChecked(typeId);
			typeHandler.validateEntityGuaranteedType(length, objectId);
		}
		
	}
	
	
	public static StorageDataFileValidator.Creator Creator()
	{
		return new StorageDataFileValidator.Creator.Default();
	}
	
	public static StorageDataFileValidator.Creator CreatorDebugLogging()
	{
		return new StorageDataFileValidator.Creator.DebugLogging();
	}
	
	public interface Creator
	{
		public StorageDataFileValidator createDataFileValidator(StorageTypeDictionary typeDictionary);
		
		
		public final class Default implements StorageDataFileValidator.Creator
		{
			Default()
			{
				super();
			}
			
			@Override
			public StorageDataFileValidator createDataFileValidator(final StorageTypeDictionary typeDictionary)
			{
				return StorageDataFileValidator.New(typeDictionary, StorageFileEntityDataIterator.New());
			}
			
		}
		
		public final class DebugLogging implements StorageDataFileValidator.Creator
		{
			DebugLogging()
			{
				super();
			}
			
			@Override
			public StorageDataFileValidator createDataFileValidator(final StorageTypeDictionary typeDictionary)
			{
				return StorageDataFileValidator.DebugLogging(typeDictionary, StorageFileEntityDataIterator.New());
			}
			
		}
		
	}
	
}
