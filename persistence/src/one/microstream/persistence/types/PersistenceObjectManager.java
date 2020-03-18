package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.lang.ref.WeakReference;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Swizzling;
import one.microstream.util.Cloneable;

public interface PersistenceObjectManager
extends PersistenceSwizzlingLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager>
{
	public long ensureObjectId(Object object, PersistenceLocalObjectIdRegistry objectIdConsumer);

	public void consolidate();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceObjectManager updateCurrentObjectId(long currentObjectId);
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public default PersistenceObjectManager Clone()
	{
		return Cloneable.super.Clone();
	}
	
	public boolean registerLocalRegistry(PersistenceLocalObjectIdRegistry localRegistry);
	
	public void mergeEntries(PersistenceLocalObjectIdRegistry localRegistry);


	
	
	public static PersistenceObjectManager.Default New(
		final PersistenceObjectRegistry   objectRegistry,
		final PersistenceObjectIdProvider oidProvider
	)
	{
		return new PersistenceObjectManager.Default(
			notNull(objectRegistry),
			notNull(oidProvider)
		);
	}

	public final class Default implements PersistenceObjectManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectRegistry   objectRegistry;
		private final PersistenceObjectIdProvider oidProvider   ;
		
		private WeakReference<PersistenceLocalObjectIdRegistry>[] localRegistries = X.WeakReferences(1);

		

		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final PersistenceObjectRegistry   objectRegistry,
			final PersistenceObjectIdProvider oidProvider
		)
		{
			super();
			this.objectRegistry = objectRegistry;
			this.oidProvider    = oidProvider   ;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectManager.Default Clone()
		{
			/*
			 * This basically turns the globally connected manager instance into a standalone clone.
			 * The oidProvider must support cloning, e.g. be transient instead of persisting into a
			 * single target location.
			 */
			synchronized(this.objectRegistry)
			{
				return new PersistenceObjectManager.Default(
					this.objectRegistry.Clone(),
					this.oidProvider.Clone()
				);
			}
		}
		
		@Override
		public void consolidate()
		{
			synchronized(this.objectRegistry)
			{
				this.objectRegistry.consolidate();
			}
		}

		@Override
		public long lookupObjectId(final Object object)
		{
			synchronized(this.objectRegistry)
			{
				return this.objectRegistry.lookupObjectId(object);
			}
		}

		@Override
		public Object lookupObject(final long objectId)
		{
//			XDebug.debugln(XChars.systemString(this) + " looking up \n" + objectId
//				+ " -> " + XChars.systemString(this.objectRegistry.lookupObject(objectId))
//			);
			synchronized(this.objectRegistry)
			{
				return this.objectRegistry.lookupObject(objectId);
			}
		}
		
		@Override
		public long ensureObjectId(final Object object, final PersistenceLocalObjectIdRegistry localRegistry)
		{
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					return objectId;
				}
				if(Swizzling.isProperId(objectId = this.synchCheckLocalRegistries(localRegistry, object)))
				{
					// must handle the object in this case since the locally keeping storer might fail its write!
					if(localRegistry != null)
					{
						localRegistry.accept(objectId, object);
					}
					
					return objectId;
				}
				
				/* (06.12.2019 TM)NOTE:
				 * The object<->id association may NOT be registered, yet, because the storing (writing) process
				 * afterwards might fail, which would leave an inconsistency (unstored entry that the next storer
				 * would assume to have already been stored) in the registry.
				 * The associations are kept locally in the storers and are merged into the registry in the commit
				 * upon success.
				 * In the exception ases, the objectId is "lost", but that is not a problem since it is no different
				 * from a deleted entity. Unused objectIds can be "recycled" by a objectId condensing util functionality.
				 * And there's the type analysis exception, anyway which stops the whole process.
				 * See PersistenceTypeHandler#guaranteeInstanceViablity.
				 */
				objectId = this.oidProvider.provideNextObjectId();
				if(localRegistry != null)
				{
					localRegistry.accept(objectId, object);
				}
				
				return objectId;
			}
		}
		
		private long synchCheckLocalRegistries(
			final PersistenceLocalObjectIdRegistry requestingConsumer,
			final Object                           instance
		)
		{
			for(final WeakReference<PersistenceLocalObjectIdRegistry> localRegistryEntry : this.localRegistries)
			{
				if(localRegistryEntry == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry localRegistry = localRegistryEntry.get();
				if(localRegistry == null || localRegistry == requestingConsumer)
				{
					continue;
				}
				
				final long objectId;
				if(Swizzling.isProperId(objectId = localRegistry.lookupObjectId(instance, requestingConsumer)))
				{
					return objectId;
				}
			}
			
			return Swizzling.notFoundId();
		}
		
		private void synchInternalMergeEntries(final PersistenceLocalObjectIdRegistry localRegistry)
		{
			localRegistry.iterateMergeableEntries(this.objectRegistry::validate);
			localRegistry.iterateMergeableEntries(this.objectRegistry::registerObject);
		}
		
		@Override
		public boolean registerLocalRegistry(final PersistenceLocalObjectIdRegistry localRegistry)
		{
			if(localRegistry.parentObjectManager() != this)
			{
				// (18.03.2020 TM)EXCP: proper exception
				throw new PersistenceException(
					PersistenceLocalObjectIdRegistry.class.getSimpleName()
					+ " " + XChars.systemString(localRegistry)
					+ " does not belong to this "
					+ PersistenceObjectManager.class.getSimpleName()
					+ " " + XChars.systemString(this)
				);
			}
			
			synchronized(this.objectRegistry)
			{
				final WeakReference<PersistenceLocalObjectIdRegistry>[] localRegistries = this.localRegistries;
				if(isAlreadyRegistered(localRegistry, localRegistries))
				{
					return false;
				}

				for(int i = 0; i < localRegistries.length; i++)
				{
					if(localRegistries[i] == null || localRegistries[i].get() == null)
					{
						localRegistries[i] = X.WeakReference(localRegistry);
						return true;
					}
				}
				
				// very conservative enlargement since there should never be many registered localRegistries at once.
				this.localRegistries = XArrays.enlarge(localRegistries, localRegistries.length + 1);
				this.localRegistries[localRegistries.length] = X.WeakReference(localRegistry);
				
				return true;
			}
		}
		
		private static boolean isAlreadyRegistered(
			final PersistenceLocalObjectIdRegistry                  localRegistry ,
			final WeakReference<PersistenceLocalObjectIdRegistry>[] localRegistries
		)
		{
			// no hash set required since there should never be a lot of entries, anyway.
			for(int i = 0; i < localRegistries.length; i++)
			{
				if(localRegistries[i] == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry registeredLocalRegistry = localRegistries[i].get();
				if(registeredLocalRegistry == null)
				{
					// some cleanup along the way
					localRegistries[i] = null;
					continue;
				}
				
				if(registeredLocalRegistry == localRegistry)
				{
					return true;
				}
			}
			
			return false;
		}

		@Override
		public void mergeEntries(final PersistenceLocalObjectIdRegistry localRegistry)
		{
			synchronized(this.objectRegistry)
			{
				int emptySlotCount = 0;
				for(int i = 0; i < this.localRegistries.length; i++)
				{
					if(this.localRegistries[i] == null)
					{
						emptySlotCount++;
						continue;
					}
					
					final PersistenceLocalObjectIdRegistry registeredLocalRegistry = this.localRegistries[i].get();
					if(registeredLocalRegistry == null)
					{
						// some cleanup along the way
						this.localRegistries[i] = null;
						emptySlotCount++;
						continue;
					}
					
					if(registeredLocalRegistry == localRegistry)
					{
						synchInternalMergeEntries(localRegistry);
						
						if(emptySlotCount > 2)
						{
							this.localRegistries = X.consolidateWeakReferences(this.localRegistries);
						}
						
						// local registry cannot be removed here as it might be reused. Must be weakly-managed.
						return;
					}
				}
			}
			
			// (17.03.2020 TM)EXCP: proper exception
			throw new PersistenceException(
				PersistenceLocalObjectIdRegistry.class.getSimpleName()
				+ " " + XChars.systemString(localRegistry)
				+ " not registered at this "
				+ PersistenceObjectManager.class.getSimpleName()
				+ " " + XChars.systemString(this)
			);
		}
		
		@Override
		public final long currentObjectId()
		{
			synchronized(this.objectRegistry)
			{
				return this.oidProvider.currentObjectId();
			}
		}

		@Override
		public PersistenceObjectManager updateCurrentObjectId(final long currentObjectId)
		{
			synchronized(this.objectRegistry)
			{
				if(this.oidProvider.currentObjectId() >= currentObjectId)
				{
					return this;
				}
				this.oidProvider.updateCurrentObjectId(currentObjectId);
			}
			
			return this;
		}

	}

}
