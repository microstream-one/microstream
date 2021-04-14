package one.microstream.persistence.types;

import one.microstream.collections.HashMapIdObject;
import one.microstream.collections.HashMapObjectId;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyWrongType;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyWrongTypeId;
import one.microstream.reference.Swizzling;

public interface PersistenceTypeRegistry extends PersistenceTypeLookup
{
	public boolean registerType(long typeId, Class<?> type) throws PersistenceExceptionConsistency;
	
	public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
		throws PersistenceExceptionConsistency;
	
	
	
	public static PersistenceTypeRegistry.Default New()
	{
		return new PersistenceTypeRegistry.Default();
	}
	
	public final class Default implements PersistenceTypeRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashMapIdObject<Class<?>> typesPerIds = HashMapIdObject.New();
		private final HashMapObjectId<Class<?>> idsPerTypes = HashMapObjectId.New();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized long lookupTypeId(final Class<?> type)
		{
			return this.idsPerTypes.get(type, Swizzling.notFoundId());
		}

		@SuppressWarnings("unchecked") // cast safety ensured by registration logic
		@Override
		public final synchronized <T> Class<T> lookupType(final long typeId)
		{
			return (Class<T>)this.typesPerIds.get(typeId);
		}

		@Override
		public final synchronized boolean validateTypeMapping(
			final long     typeId,
			final Class<?> type
		)
			throws PersistenceExceptionConsistency
		{
			if(Swizzling.isNotProperId(typeId))
			{
				throw new PersistenceException("Not a proper TypeId: " + typeId + " for type " + type);
			}
			
			final Class<?> registeredType   = this.typesPerIds.get(typeId);
			final long     registeredTypeId = this.idsPerTypes.get(type, Swizzling.notFoundId());
			
			if(registeredType == null)
			{
				if(Swizzling.isNotFoundId(registeredTypeId))
				{
					return false;
				}
				
				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}
			
			if(registeredType == type)
			{
				if(registeredTypeId == typeId)
				{
					return true;
				}

				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}

			throw new PersistenceExceptionConsistencyWrongType(typeId, registeredType, type);
		}

		@Override
		public final synchronized boolean validateTypeMappings(
			final Iterable<? extends PersistenceTypeLink> mappings
		)
			throws PersistenceExceptionConsistency
		{
			// the initial assumption is that all pairs are already contained/registered
			boolean containsAll = true;
			
			for(final PersistenceTypeLink type : mappings)
			{
				if(!this.validateTypeMapping(type.typeId(), type.type()))
				{
					// if only one pair is not registered yet, the return false is flipped to false.
					containsAll = false;
				}
			}
			
			return containsAll;
		}
		
		private void synchRegisterType(
			final long     typeId,
			final Class<?> type
		)
		{
			this.typesPerIds.add(typeId, type);
			this.idsPerTypes.add(type, typeId);
		}

		@Override
		public final synchronized boolean registerType(
			final long     typeId,
			final Class<?> type
		)
			throws PersistenceExceptionConsistency
		{
			if(this.validateTypeMapping(typeId, type))
			{
				return false;
			}
			this.synchRegisterType(typeId, type);
			
			return true;
		}
		
		@Override
		public final synchronized boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			// validate all type mappings before registering anything
			if(this.validateTypeMappings(types))
			{
				return false;
			}
			
			// itereate all valid types and register each one
			for(final PersistenceTypeLink type : types)
			{
				// already registered types are ignored, inconsistent types are impossible at this point
				this.synchRegisterType(type.typeId(), type.type());
			}
			
			return true;
		}
		
	}

}
