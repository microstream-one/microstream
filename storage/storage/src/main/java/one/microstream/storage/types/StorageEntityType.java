package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.function.Predicate;

import one.microstream.functional.ThrowingProcedure;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.storage.exceptions.StorageException;


public interface StorageEntityType<E extends StorageEntity>
{
	public StorageEntityTypeHandler typeHandler();

	public long entityCount();
	
	public default boolean isEmpty()
	{
		return this.entityCount() == 0;
	}

	public <T extends Throwable, P extends ThrowingProcedure<? super E, T>> P iterateEntities(P procedure) throws T;

	public boolean hasReferences();

	public long simpleReferenceDataCount();

	public void iterateEntityReferenceIds(E entity, PersistenceObjectIdAcceptor iterator);

	public StorageIdAnalysis validateEntities();



	public final class Default implements StorageEntityType<StorageEntity.Default>
	{
		public interface EntityDeleter extends Predicate<StorageEntity.Default>
		{
			@Override
			public boolean test(StorageEntity.Default entity);

			public void delete(
				StorageEntity.Default     entity        ,
				StorageEntityType.Default type          ,
				StorageEntity.Default     previousInType
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		        final int                      channelIndex            ;
		        final long                     typeId                  ;
		private final StorageEntityTypeHandler typeHandler             ;
		private final boolean                  hasReferences           ;
		private final long                     simpleReferenceDataCount;
		
		private       long                     entityCount             ;
		StorageEntityType.Default              hashNext                ;
		StorageEntityType.Default              next                    ;
		        final TypeInFile               dummy                    = new TypeInFile(this, null, null);

		StorageEntity.Default head = StorageEntity.Default.createDummy(this.dummy);
		StorageEntity.Default tail = this.head;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final int channelIndex)
		{
			super();
			this.channelIndex             = channelIndex;
			this.typeId                   =           -1;
			this.typeHandler              =         null;
			this.hasReferences            =        false;
			this.simpleReferenceDataCount =            0;
			this.next                     =         this;
		}

		Default(
			final int                              channelIndex,
			final StorageEntityTypeHandler         typeHandler ,
			final StorageEntityType.Default hashNext    ,
			final StorageEntityType.Default next
		)
		{
			super();
			this.channelIndex             = channelIndex                        ;
			this.hasReferences            = typeHandler.hasPersistedReferences();
			this.simpleReferenceDataCount = typeHandler.simpleReferenceCount()  ;
			this.typeId                   = typeHandler.typeId()                ;
			this.typeHandler              = typeHandler                         ;
			this.hashNext                 = hashNext                            ;
			this.next                     = next                                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void add(final StorageEntity.Default entry)
		{
			// last item next null strategy to increase adding and iteration performance
//			(entry.typePrev = this.head.typePrev).typeNext = this.head.typePrev = entry;
			this.tail = this.tail.typeNext = entry;
			this.entityCount++;
		}

		final void remove(final StorageEntity.Default entry, final StorageEntity.Default previousInType)
		{
			// tail reference requires special handling logic
			if(entry == this.tail)
			{
				(this.tail = previousInType).typeNext = null;
			}
			else
			{
				previousInType.typeNext = entry.typeNext;
			}

			// decrement entity count (strictly only once per remove as guaranteed by check above)
			this.entityCount--;
		}

		@Override
		public <T extends Throwable, P extends ThrowingProcedure<? super StorageEntity.Default, T>>
		P iterateEntities(final P procedure) throws T
		{
			for(StorageEntity.Default entity = this.head; (entity = entity.typeNext) != null;)
			{
				procedure.accept(entity);
			}
			return procedure;
		}

		public <P extends EntityDeleter> P removeAll(final P deleter)
		{
			for(StorageEntity.Default last, entity = this.head; (entity = (last = entity).typeNext) != null;)
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

		@Override
		public final StorageEntityTypeHandler typeHandler()
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
		public final void iterateEntityReferenceIds(
			final StorageEntity.Default entity  ,
			final PersistenceObjectIdAcceptor  iterator
		)
		{
			this.typeHandler.iterateReferences(entity.cacheAddress(), iterator);
		}

		@Override
		public StorageIdAnalysis validateEntities()
		{
			final StorageEntityTypeHandler typeHandler = this.typeHandler;

			long maxObjectId = 0, maxConstantId = 0;
			final long maxTypeId = 0;
			for(StorageEntity.Default entity = this.head; (entity = entity.typeNext) != null;)
			{
				final long entityLength   = entity.length;
				final long entityObjectId = entity.objectId();

				typeHandler.validateEntityGuaranteedType(entityLength, entityObjectId);

				final long objectId = entity.objectId();
				if(Persistence.IdType.OID.isInRange(objectId))
				{
					if(objectId >= maxObjectId)
					{
						maxObjectId = objectId;
					}
				}
				else if(Persistence.IdType.CID.isInRange(objectId))
				{
					if(objectId >= maxConstantId)
					{
						maxConstantId = objectId;
					}
				}
				else
				{
					throw new StorageException("Invalid OID: " + objectId);
				}
			}

			return StorageIdAnalysis.New(maxTypeId, maxObjectId, maxConstantId);
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
