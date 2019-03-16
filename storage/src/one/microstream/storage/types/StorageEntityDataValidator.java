package one.microstream.storage.types;

import static one.microstream.X.notNull;

import one.microstream.meta.XDebug;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryEntityRawDataAcceptor;
import one.microstream.util.UtilStackTrace;


@FunctionalInterface
public interface StorageEntityDataValidator extends BinaryEntityRawDataAcceptor
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
		return new StorageEntityDataValidator.ByDictionary(
			notNull(typeDictionary)
		);
	}
	
	public static StorageEntityDataValidator New(
		final long lengthLowerValue  ,
		final long lengthUpperBound  ,
		final long typeIdLowerValue  ,
		final long typeIdUpperBound  ,
		final long objectIdLowerValue,
		final long objectIdUpperBound
	)
	{
		return new StorageEntityDataValidator.SimpleBounds(
			lengthLowerValue  ,
			lengthUpperBound  ,
			typeIdLowerValue  ,
			typeIdUpperBound  ,
			objectIdLowerValue,
			objectIdUpperBound
		);
	}
	
	@Deprecated
	public static StorageEntityDataValidator DebugLogging(
		final StorageEntityDataValidator delegate
	)
	{
		return new DebugLogger(
			notNull(delegate)
		);
	}
	
	@Deprecated
	public static StorageEntityDataValidator DebugLogging(
		final StorageTypeDictionary         typeDictionary
	)
	{
		return DebugLogging(New(typeDictionary));
	}
	
	public final class DebugLogger implements StorageEntityDataValidator
	{
		private final StorageEntityDataValidator delegate;

		DebugLogger(final StorageEntityDataValidator delegate)
		{
			super();
			this.delegate = delegate;
		}
		
		@Override
		public void validateEntity(final long length, final long typeId, final long objectId)
		{
			XDebug.println("Validating entity [" + length + "][" + typeId + "][" + objectId + "]");
			this.delegate.validateEntity(length, typeId, objectId);
		}
		
		
	}
	
	public class SimpleBounds implements StorageEntityDataValidator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long
			lengthLowerValue  ,
			lengthUpperBound  ,
			typeIdLowerValue  ,
			typeIdUpperBound  ,
			objectIdLowerValue,
			objectIdUpperBound
		;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		SimpleBounds(
			final long lengthLowerValue  ,
			final long lengthUpperBound  ,
			final long typeIdLowerValue  ,
			final long typeIdUpperBound  ,
			final long objectIdLowerValue,
			final long objectIdUpperBound
		)
		{
			super();
			this.lengthLowerValue   = lengthLowerValue  ;
			this.lengthUpperBound   = lengthUpperBound  ;
			this.typeIdLowerValue   = typeIdLowerValue  ;
			this.typeIdUpperBound   = typeIdUpperBound  ;
			this.objectIdLowerValue = objectIdLowerValue;
			this.objectIdUpperBound = objectIdUpperBound;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private static boolean isValid(final long lowerValue, final long upperBound, final long value)
		{
			if(value < lowerValue)
			{
				return false;
			}
			if(value >= upperBound)
			{
				return false;
			}
			
			return true;
		}
		
		private static RuntimeException createException(final long length, final long typeId, final long objectId)
		{
			return UtilStackTrace.cutStacktraceByOne(
				new RuntimeException("[" + length + "][" + typeId + "][" + objectId + "]")
			);
		}

		@Override
		public void validateEntity(final long length, final long typeId, final long objectId)
		{
			if(!isValid(this.lengthLowerValue, this.lengthUpperBound, length))
			{
				throw createException(length, typeId, objectId);
			}
			if(!isValid(this.typeIdLowerValue, this.typeIdUpperBound, typeId))
			{
				throw createException(length, typeId, objectId);
			}
			if(!isValid(this.objectIdLowerValue, this.objectIdUpperBound, objectId))
			{
				throw createException(length, typeId, objectId);
			}
		}
		
	}
	
	public class ByDictionary implements StorageEntityDataValidator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final StorageTypeDictionary typeDictionary;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected ByDictionary(final StorageTypeDictionary typeDictionary)
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
