package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryEntityDataAcceptor;


@FunctionalInterface
public interface StorageEntityDataValidator extends BinaryEntityDataAcceptor
{

	@Override
	public default boolean acceptEntityData(
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
	
	public void validateEntity(long length, long typeId, long objectId);
	
	
	
	public static StorageEntityDataValidator New(
		final StorageTypeDictionary typeDictionary
	)
	{
		return new StorageEntityDataValidator.Implementation(
			notNull(typeDictionary)
		);
	}
	
	@Deprecated
	public static StorageEntityDataValidator DebugLogging(
		final StorageTypeDictionary         typeDictionary
	)
	{
		return new StorageEntityDataValidator.Implementation(
			notNull(typeDictionary)
		){
			
			@Override
			public void validateEntity(final long length, final long typeId, final long objectId)
			{
				XDebug.println("Validating entity [" + length + "][" + typeId + "][" + objectId + "]");
				super.validateEntity(length, typeId, objectId);
			}
		};
	}
	
	public class Implementation implements StorageEntityDataValidator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageTypeDictionary typeDictionary;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Implementation(final StorageTypeDictionary typeDictionary)
		{
			super();
			this.typeDictionary = notNull(typeDictionary);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void validateEntity(final long length, final long typeId, final long objectId)
		{
			final StorageEntityTypeHandler typeHandler = this.typeDictionary.lookupTypeHandlerChecked(typeId);
			typeHandler.validateEntityGuaranteedType(length, objectId);
		}
		
	}
	
	
	public static StorageEntityDataValidator.Creator Creator()
	{
		return new StorageEntityDataValidator.Creator.Default();
	}
	
	@Deprecated
	public static StorageEntityDataValidator.Creator CreatorDebugLogging()
	{
		return new StorageEntityDataValidator.Creator.DebugLogging();
	}
	
	public interface Creator
	{
		public StorageEntityDataValidator createDataFileValidator(StorageTypeDictionary typeDictionary);
		
		
		public final class Default implements StorageEntityDataValidator.Creator
		{
			Default()
			{
				super();
			}
			
			@Override
			public StorageEntityDataValidator createDataFileValidator(final StorageTypeDictionary typeDictionary)
			{
				return StorageEntityDataValidator.New(typeDictionary);
			}
			
		}
		
		@Deprecated
		public final class DebugLogging implements StorageEntityDataValidator.Creator
		{
			DebugLogging()
			{
				super();
			}
			
			@Override
			public StorageEntityDataValidator createDataFileValidator(final StorageTypeDictionary typeDictionary)
			{
				return StorageEntityDataValidator.DebugLogging(typeDictionary);
			}
			
		}
		
	}
	
}
