package net.jadoth.storage.types;

import java.util.function.Predicate;

import net.jadoth.functional.ThrowingProcedure;
import net.jadoth.functional._longProcedure;
import net.jadoth.storage.exceptions.StorageException;
import net.jadoth.swizzling.types.Swizzle;


public interface StorageEntityType<I extends StorageEntityCacheItem<I>>
{
	public StorageEntityTypeHandler<?> typeHandler();

	public long entityCount();
	
	public default boolean isEmpty()
	{
		return this.entityCount() == 0;
	}

	public <T extends Throwable, P extends ThrowingProcedure<? super I, T>> P iterateEntities(P procedure) throws T;

	public boolean hasReferences();

	public long simpleReferenceDataCount();

	public void iterateEntityReferenceIds(I entity, _longProcedure procedure);

	@Deprecated // (28.05.2018 TM)FIXME: remove with OGS-3
	public default StorageIdAnalysis validateEntities()
	{
		return this.validateEntities(null);
	}

	@Deprecated // (28.05.2018 TM)FIXME: remove with OGS-3
	public StorageIdAnalysis validateEntities(StorageTypeDictionary oldTypes);

	// (28.05.2018 TM)FIXME: rename with OGS-3
	public StorageIdAnalysis validateEntities_NEWOGS3();



	public final class Implementation implements StorageEntityType<StorageEntity.Implementation>
	{
		public interface EntityDeleter extends Predicate<StorageEntity.Implementation>
		{
			@Override
			public boolean test(StorageEntity.Implementation entity);

			public void delete(
				StorageEntity.Implementation     entity        ,
				StorageEntityType.Implementation type          ,
				StorageEntity.Implementation     previousInType
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		        final int                         channelIndex            ;
		        final long                        typeId                  ;
		private final StorageEntityTypeHandler<?> typeHandler             ;
		private final boolean                     hasReferences           ;
		private final long                        simpleReferenceDataCount;

		private       long                        entityCount             ;
		StorageEntityType.Implementation          hashNext                ;
		StorageEntityType.Implementation          next                    ;
		        final TypeInFile                  dummy                    = new TypeInFile(this, null, null);

		StorageEntity.Implementation head = StorageEntity.Implementation.createDummy(this.dummy);
		StorageEntity.Implementation tail = this.head;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

		Implementation(final int channelIndex)
		{
			super();
			this.channelIndex             = channelIndex;
			this.typeId                   =           -1;
			this.typeHandler              =         null;
			this.hasReferences            =        false;
			this.simpleReferenceDataCount =            0;
			this.next                     =         this;
		}

		Implementation(
			final int                              channelIndex,
			final StorageEntityTypeHandler<?>      typeHandler ,
			final StorageEntityType.Implementation hashNext    ,
			final StorageEntityType.Implementation next
		)
		{
			super();
			this.channelIndex             = channelIndex                      ;
			this.hasReferences            = typeHandler.hasPersistedReferences()       ;
			this.simpleReferenceDataCount = typeHandler.simpleReferenceCount();
			this.typeId                   = typeHandler.typeId()              ;
			this.typeHandler              = typeHandler                       ;
			this.hashNext                 = hashNext                          ;
			this.next                     = next                              ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void add(final StorageEntity.Implementation entry)
		{
			// last item next null strategy to increase adding and iteration performance
//			(entry.typePrev = this.head.typePrev).typeNext = this.head.typePrev = entry;
			this.tail = this.tail.typeNext = entry;
			this.entityCount++;
		}

		final void remove(final StorageEntity.Implementation entry, final StorageEntity.Implementation previousInType)
		{
			// better check if already removed, should never happen in correct code, but you never know and it's cheap.
//			if(entry.typePrev == null)
//			{
//				return;
//			}

			// tail reference requires special handling logic
			if(entry == this.tail)
			{
				(this.tail = previousInType).typeNext = null;
			}
			else
			{
				previousInType.typeNext = entry.typeNext;
			}

			// mark as removed
//			entry.typePrev = null;

			// decrement entity count (strictly only once per remove as guaranteed by check above)
			this.entityCount--;
		}

//		final void reset(final StorageEntityType.Implementation typeHead)
//		{
//			this.entityCount = 0;
//			(this.tail = this.head).typeNext = null;
//			this.hashNext = null;
//			this.next = typeHead;
//		}

		@Override
		public <T extends Throwable, P extends ThrowingProcedure<? super StorageEntity.Implementation, T>>
		P iterateEntities(final P procedure) throws T
		{
			for(StorageEntity.Implementation entity = this.head; (entity = entity.typeNext) != null;)
			{
				procedure.accept(entity);
			}
			return procedure;
		}

		public <P extends EntityDeleter> P removeAll(final P deleter)
		{
			for(StorageEntity.Implementation last, entity = this.head; (entity = (last = entity).typeNext) != null;)
			{
				if(deleter.test(entity))
				{
					deleter.delete(entity, this, last);
					// must back-set entity variable to last in order for last to remain itself in the loop's next step.
					entity = last;
				}
			}
			return deleter;
		}

//		@Override
//		public void processEntities(final Predicate<? super StorageEntity> predicate)
//		{
//			for(StorageEntity last = this.head, entity; (entity = last.typeNext) != null; )
//			{
//				if(predicate.test(entity))
//				{
//					// devour entity, last remains the same
//					(last.typeNext = entity.typeNext).typePrev = last;
//				}
//				else
//				{
//					// advance last by one
//					last = entity;
//				}
//			}
//		}

		@Override
		public final StorageEntityTypeHandler<?> typeHandler()
		{
			return this.typeHandler;
		}

		@Override
		public final long entityCount()
		{
			return this.entityCount;
		}

		@Override
		public final boolean hasReferences()
		{
			return this.hasReferences;
		}

		@Override
		public final long simpleReferenceDataCount()
		{
			return this.simpleReferenceDataCount;
		}

		@Override
		public final void iterateEntityReferenceIds(final StorageEntity.Implementation entity, final _longProcedure procedure)
		{
			this.typeHandler.iterateReferences(entity.cacheAddress(), procedure);
		}

		@Override
		public final StorageIdAnalysis validateEntities(final StorageTypeDictionary oldTypes)
		{
			final StorageEntityTypeHandler<?> typeHandler = this.typeHandler;

			final StorageEntityTypeHandler<?> oldTypeHandler = oldTypes == null
				? null
				: oldTypes.lookupTypeHandler(this.typeId)
			;

			long maxOid = 0, maxCid = 0, maxTid = 0;
			final Swizzle.IdType typeCid = Swizzle.IdType.CID;
			final Swizzle.IdType typeOid = Swizzle.IdType.OID;
			final Swizzle.IdType typeTid = Swizzle.IdType.TID;

			for(StorageEntity.Implementation entity = this.head; (entity = entity.typeNext) != null;)
			{
				final long entityLength   = entity.length;
				final long entityObjectId = entity.objectId();

				if(!typeHandler.isValidEntityGuaranteedType(entityLength, entityObjectId))
				{
					if(oldTypeHandler != null)
					{
//						DEBUGStorage.println("Invalid for current type, trying old type for " + typeHandler.typeName());

						// try again with old type handler
						if(!oldTypeHandler.isValidEntityGuaranteedType(entityLength, entityObjectId))
						{
							// if still not valid, throw exception based on old type definition
							oldTypeHandler.validateEntityGuaranteedType(entityLength, entityObjectId);
						}
						// valid by old type at this point, go on without exception
					}
					else
					{
						// if not valid and no old handler, throw exception based on new type definition
						typeHandler.validateEntityGuaranteedType(entityLength, entityObjectId);
					}
				}
				// valid by one way or the other

				final long oid = entity.objectId();
				if(typeOid.isInRange(oid))
				{
					if(oid >= maxOid)
					{
						maxOid = oid;
					}
				}
				else if(typeCid.isInRange(oid))
				{
					if(oid >= maxCid)
					{
						maxCid = oid;
					}
				}
				else if(typeTid.isInRange(oid))
				{
					/* note that a (storage) type describing a (Java) type (e.g. Class) has TIDs
					 * as the entities' identifying object ID. Hence encountering a TID here is valid.
					 */
					if(oid >= maxTid)
					{
						maxTid = oid;
					}
				}
				else
				{
					throw new StorageException("Invalid OID: " + oid);
				}
			}

			return StorageIdAnalysis.New(maxTid, maxOid, maxCid);
		}
		

		@Override
		public StorageIdAnalysis validateEntities_NEWOGS3()
		{
			final StorageEntityTypeHandler<?> typeHandler = this.typeHandler;

			long maxOid = 0, maxCid = 0, maxTid = 0;
			for(StorageEntity.Implementation entity = this.head; (entity = entity.typeNext) != null;)
			{
				final long entityLength   = entity.length;
				final long entityObjectId = entity.objectId();

				typeHandler.validateEntityGuaranteedType(entityLength, entityObjectId);

				final long oid = entity.objectId();
				if(Swizzle.IdType.OID.isInRange(oid))
				{
					if(oid >= maxOid)
					{
						maxOid = oid;
					}
				}
				else if(Swizzle.IdType.CID.isInRange(oid))
				{
					if(oid >= maxCid)
					{
						maxCid = oid;
					}
				}
				else if(Swizzle.IdType.TID.isInRange(oid))
				{
					/* note that a (storage) type describing a (Java) type (e.g. Class) has TIDs
					 * as the entities' identifying object ID. Hence encountering a TID here is valid.
					 */
					if(oid >= maxTid)
					{
						maxTid = oid;
					}
				}
				else
				{
					throw new StorageException("Invalid OID: " + oid);
				}
			}

			return StorageIdAnalysis.New(maxTid, maxOid, maxCid);
		}

		@Override
		public String toString()
		{
			return "Ch#" + this.channelIndex + "_"
				+ (this.typeHandler == null ? "<Dummy Type>"  : this.typeHandler.toString())
			;
		}

	}

}
