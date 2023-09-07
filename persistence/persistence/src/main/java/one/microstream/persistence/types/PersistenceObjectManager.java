package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import static one.microstream.X.notNull;

import java.lang.ref.WeakReference;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.XArrays;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.reference.Swizzling;
import one.microstream.util.Cloneable;

public interface PersistenceObjectManager<D>
extends PersistenceSwizzlingLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager<D>>
{
	public long ensureObjectId(Object object);
	
	public <T> long ensureObjectId(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);
	
	public <T> long ensureObjectIdGuaranteedRegister(
		T                               object           ,
		PersistenceObjectIdRequestor<D> objectIdRequestor,
		PersistenceTypeHandler<D, T>    optionalHandler
	);

	public void consolidate();

	@Override
	public long currentObjectId();

	@Override
	public PersistenceObjectManager<D> updateCurrentObjectId(long currentObjectId);
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public default PersistenceObjectManager<D> Clone()
	{
		return Cloneable.super.Clone();
	}
	
	public boolean registerLocalRegistry(PersistenceLocalObjectIdRegistry<D> localRegistry);
	
	public void mergeEntries(PersistenceLocalObjectIdRegistry<D> localRegistry);


	
	
	public static <D> PersistenceObjectManager.Default<D> New(
		final PersistenceObjectRegistry   objectRegistry,
		final PersistenceObjectIdProvider oidProvider
	)
	{
		return new PersistenceObjectManager.Default<>(
			notNull(objectRegistry),
			notNull(oidProvider)
		);
	}

	public final class Default<D> implements PersistenceObjectManager<D>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectRegistry   objectRegistry;
		private final PersistenceObjectIdProvider oidProvider   ;
		
		private WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries = X.WeakReferences(1);
		
		private final PersistenceObjectIdRequestor<D> noOp = PersistenceObjectIdRequestor.NoOp();

		

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
		public PersistenceObjectManager.Default<D> Clone()
		{
			/*
			 * This basically turns the globally connected manager instance into a standalone clone.
			 * The oidProvider must support cloning, e.g. be transient instead of persisting into a
			 * single target location.
			 */
			synchronized(this.objectRegistry)
			{
				return new PersistenceObjectManager.Default<>(
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
		public final long ensureObjectId(final Object object)
		{
			return this.ensureObjectId(object, this.noOp, null);
		}
		
		@Override
		public <T> long ensureObjectId(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			/*
			 * Three steps to determine an object's objectId which must be executed in exactely that order
			 * and under the protection of a lock on the global registry to enqueue all concurrent storers.
			 * 
			 * 1.) check if already globally known.
			 * 2.) check if already locally known in on of the other storers (= "local registries)"
			 * 3.) otherwise, provide and assign a new ObjectId.
			 */
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isNotProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					if(Swizzling.isNotProperId(objectId = this.synchCheckLocalRegistries(objectIdRequestor, object, optionalHandler)))
					{
						// see below about not globally registering the newly assigned objectId
						objectId = this.oidProvider.provideNextObjectId();
					}

					// lazy logic means only apply if not yet globally known (= something new / "store required").
					objectIdRequestor.registerLazyOptional(objectId, object, optionalHandler);
				}
				
				// eager logic means ALWAYS apply, even if already globally known (= "store full").
				objectIdRequestor.registerEagerOptional(objectId, object, optionalHandler);

				/* (06.12.2019 TM)NOTE:
				 * A new object<->id association may NOT be registered right away, since the storing (writing) logic
				 * afterwards might fail, which would leave an inconsistency (unstored entry that the next storer
				 * would assume to have already been stored) in the registry.
				 * The associations are kept locally in the storers and are merged into the registry in the commit
				 * upon success.
				 * In the exception case, the objectId is "lost", but that is not a problem since it is no different
				 * from a deleted entity. Unused objectIds can be "recycled" by a (future) objectId condensing utility
				 * functionality.
				 * And there's the type analysis exception, anyway which stops the whole process.
				 * See PersistenceTypeHandler#guaranteeInstanceViablity.
				 */
				
				return objectId;
			}
		}
		
		/**
		 * Variant of {@link #ensureObjectId(Object)} with guaranteed registering (effectively override-eager-logic)
		 * 
		 */
		@Override
		public <T> long ensureObjectIdGuaranteedRegister(
			final T                               object           ,
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final PersistenceTypeHandler<D, T>    optionalHandler
			
		)
		{
			// see #ensureObjectId for explaining comments
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isNotProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					if(Swizzling.isNotProperId(objectId = this.synchCheckLocalRegistries(objectIdRequestor, object, optionalHandler)))
					{
						objectId = this.oidProvider.provideNextObjectId();
					}
				}
				
				// overriding "guaranteed registering" logic
				objectIdRequestor.registerGuaranteed(objectId, object, optionalHandler);
				
				return objectId;
			}
		}
		
		private <T> long synchCheckLocalRegistries(
			final PersistenceObjectIdRequestor<D> objectIdRequestor,
			final T                               instance         ,
			final PersistenceTypeHandler<D, T>    optionalHandler
		)
		{
			for(final WeakReference<PersistenceLocalObjectIdRegistry<D>> localRegistryEntry : this.localRegistries)
			{
				if(localRegistryEntry == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry<D> localRegistry = localRegistryEntry.get();
				if(localRegistry == null || localRegistry == objectIdRequestor)
				{
					continue;
				}
				
				final long objectId;
				if(Swizzling.isProperId(objectId = localRegistry.lookupObjectId(instance, objectIdRequestor, optionalHandler)))
				{
					return objectId;
				}
			}
			
			return Swizzling.notFoundId();
		}
		
		private void synchInternalMergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			localRegistry.iterateMergeableEntries(this.objectRegistry::validate);
			localRegistry.iterateMergeableEntries(this.objectRegistry::registerObject);
		}
		
		@Override
		public boolean registerLocalRegistry(final PersistenceLocalObjectIdRegistry<D> localRegistry)
		{
			if(localRegistry.parentObjectManager() != this)
			{
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
				final WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries = this.localRegistries;
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
		
		private static <D> boolean isAlreadyRegistered(
			final PersistenceLocalObjectIdRegistry<D>                  localRegistry ,
			final WeakReference<PersistenceLocalObjectIdRegistry<D>>[] localRegistries
		)
		{
			// no hash set required since there should never be a lot of entries, anyway.
			for(int i = 0; i < localRegistries.length; i++)
			{
				if(localRegistries[i] == null)
				{
					continue;
				}
				
				final PersistenceLocalObjectIdRegistry<D> registeredLocalRegistry = localRegistries[i].get();
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
		public void mergeEntries(final PersistenceLocalObjectIdRegistry<D> localRegistry)
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
					
					final PersistenceLocalObjectIdRegistry<D> registeredLocalRegistry = this.localRegistries[i].get();
					if(registeredLocalRegistry == null)
					{
						// some cleanup along the way
						this.localRegistries[i] = null;
						emptySlotCount++;
						continue;
					}
					
					if(registeredLocalRegistry == localRegistry)
					{
						this.synchInternalMergeEntries(localRegistry);
						
						if(emptySlotCount > 2)
						{
							this.localRegistries = X.consolidateWeakReferences(this.localRegistries);
						}
						
						this.objectRegistry.cleanUp();
						// local registry cannot be removed here as it might be reused. Must be weakly-managed.
						return;
					}
				}
			}
			
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
		public PersistenceObjectManager<D> updateCurrentObjectId(final long currentObjectId)
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
