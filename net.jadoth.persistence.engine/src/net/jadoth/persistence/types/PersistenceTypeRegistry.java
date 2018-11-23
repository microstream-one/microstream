package net.jadoth.persistence.types;

import net.jadoth.collections.HashMapIdObject;
import net.jadoth.collections.HashMapObjectId;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistency;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyWrongType;
import net.jadoth.persistence.exceptions.PersistenceExceptionConsistencyWrongTypeId;
import net.jadoth.util.Flag;

public interface PersistenceTypeRegistry extends PersistenceTypeLookup
{
	public boolean registerType(long typeId, Class<?> type) throws PersistenceExceptionConsistency;
	
	public default boolean registerTypes(
		final Iterable<? extends PersistenceTypeLink> types
	)
		throws PersistenceExceptionConsistency
	{
		synchronized(this)
		{
			// validate all type mappings before registering anything
			this.validatePossibleTypeMappings(types);
			
			final Flag hasChanged = Flag.New();
			
			// register type identities (typeId<->type) first to make all types available for type handler creation
			types.forEach(e ->
			{
				if(this.registerType(e.typeId(), e.type()) && hasChanged.isOff())
				{
					hasChanged.on();
				}
			});
			
			return hasChanged.isOn();
		}
		
	}
	
	
	
	public static PersistenceTypeRegistry.Implementation New()
	{
		return new PersistenceTypeRegistry.Implementation();
	}
	
	public final class Implementation implements PersistenceTypeRegistry
	{
		// (21.11.2018 TM)FIXME: JET-48: implement SwizzleTypeRegistry
		
		// (21.11.2018 TM)NOTE: moved from ObjectRegistry
//		private final Consumer<SwizzleTypeLink> typeExistsValidator = e ->
//			this.validateExistingMapping(e.type(), e.typeId())
//		;
//
//		private final Consumer<SwizzleTypeLink> typePossibleValidator = e ->
//			this.validatePossibleMapping(e.type(), e.typeId())
//		;
//
//		void validateExistingMapping(final Class<?> type, final long typeId)
//		{
//			// don't know if this method's synchronization pattern is worth much performance, but it's funny to use it
//			final Entry[][] slotsPerOid, slotsPerRef;
//			final int modulo;
//			synchronized(this)
//			{
//				slotsPerOid = this.slotsPerOid;
//				slotsPerRef = this.slotsPerRef;
//				modulo      = this.modulo;
//			}
//
//			// don't lock out other threads while doing mere non-writing validation work
//			validateExistingTypeForTypeId(slotsPerOid[(int)(typeId & modulo)         ], typeId, type);
//			validateExistingTypeIdForType(slotsPerRef[identityHashCode(type) & modulo], typeId, type);
//		}
//
//		void validatePossibleMapping(final Class<?> type, final long typeId)
//		{
//			// don't know if this method's synchronization pattern is worth much performance, but it's funny to use it
//			final Entry[][] slotsPerOid, slotsPerRef;
//			final int modulo;
//			synchronized(this)
//			{
//				slotsPerOid = this.slotsPerOid;
//				slotsPerRef = this.slotsPerRef;
//				modulo      = this.modulo;
//			}
//
//			// don't lock out other threads while doing mere non-writing validation work
//			// only use for consistency check. Wether type is registered or unknown is irrelevant here
//			isConsistentRegisteredTypeForTypeId(slotsPerOid[(int)(typeId & modulo)         ], typeId, type);
//			isConsistentRegisteredTypeIdForType(slotsPerRef[identityHashCode(type) & modulo], typeId, type);
//		}
//
//		private static void validateExistingTypeForTypeId(final Entry[] bucketsI, final long tid, final Class<?> type)
//		{
//			if(isConsistentRegisteredTypeForTypeId(bucketsI, tid, type))
//			{
//				return;
//			}
//			throw new SwizzleExceptionConsistencyUnknownMapping(tid, type);
//		}
//
//		private static void validateExistingTypeIdForType(final Entry[] bucketsR, final long tid, final Class<?> type)
//		{
//			if(isConsistentRegisteredTypeIdForType(bucketsR, tid, type))
//			{
//				return;
//			}
//			throw new SwizzleExceptionConsistencyUnknownMapping(tid, type);
//		}
//
//		private static boolean isConsistentRegisteredTypeForTypeId(
//			final Entry[]  bucketsI,
//			final long     tid     ,
//			final Class<?> type
//		)
//		{
//			if(bucketsI != null)
//			{
//				for(int i = 0; i < bucketsI.length; i++)
//				{
//					if(bucketsI[i] != null && bucketsI[i].oid == tid)
//					{
//						// tid == oid for types
//						if(bucketsI[i].ref.get() == type)
//						{
//							return true;
//						}
//						throw new SwizzleExceptionConsistencyWrongType(tid, (Class<?>)bucketsI[i].ref.get(), type);
//					}
//				}
//			}
//			return false;
//		}
//
//		private static boolean isConsistentRegisteredTypeIdForType(
//			final Entry[]  bucketsR,
//			final long     tid     ,
//			final Class<?> type
//		)
//		{
//			if(bucketsR != null)
//			{
//				for(int i = 0; i < bucketsR.length; i++)
//				{
//					if(bucketsR[i] != null && bucketsR[i].oid == tid)
//					{
//						// tid == oid for types
//						if(bucketsR[i].oid == tid)
//						{
//							// getting here actually means the registry is inconsistent. Maybe throw exception etc.
//							return true;
//						}
//						throw new SwizzleExceptionConsistencyWrongTypeId(type, bucketsR[i].oid, tid);
//					}
//				}
//			}
//			return false;
//		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final HashMapIdObject<Class<?>> typesPerIds = HashMapIdObject.New();
		private final HashMapObjectId<Class<?>> idsPerTypes = HashMapObjectId.New();
		
		
		
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
		public final synchronized long lookupTypeId(final Class<?> type)
		{
			return this.idsPerTypes.get(type);
		}

		@SuppressWarnings("unchecked") // cast safety ensured by registration logic
		@Override
		public final synchronized <T> Class<T> lookupType(final long typeId)
		{
			return (Class<T>)this.typesPerIds.get(typeId);
		}

		@Override
		public void validateExistingTypeMapping(final long typeId, final Class<?> type)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeLookup#validateExistingTypeMapping()
		}

		@Override
		public void validateExistingTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeLookup#validateExistingTypeMappings()
		}

		@Override
		public void validatePossibleTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeLookup#validatePossibleTypeMappings()
		}

		@Override
		public final synchronized boolean registerType(
			final long     typeId,
			final Class<?> type
		)
			throws PersistenceExceptionConsistency
		{
			final Class<?> registeredType   = this.typesPerIds.get(typeId);
			final long     registeredTypeId = this.idsPerTypes.get(type);
			
			if(registeredType == null)
			{
				if(registeredTypeId == Persistence.nullId())
				{
					this.typesPerIds.add(typeId, type);
					this.idsPerTypes.add(type, typeId);
					return true;
				}
				
				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}
			
			if(registeredType == type)
			{
				if(registeredTypeId == typeId)
				{
					return false;
				}

				throw new PersistenceExceptionConsistencyWrongTypeId(type, registeredTypeId, typeId);
			}

			throw new PersistenceExceptionConsistencyWrongType(typeId, registeredType, type);
		}
		
	}

}
