package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.reference.Swizzling;
import one.microstream.util.Cloneable;

public interface PersistenceObjectManager
extends PersistenceSwizzlingLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager>
{
	public default long ensureObjectId(final Object object)
	{
		return this.ensureObjectId(object, null);
	}
	
	public long ensureObjectId(Object object, PersistenceAcceptor newObjectIdCallback);

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
		public long ensureObjectId(final Object object)
		{
			return this.ensureObjectId(object, null);
		}
		
		@Override
		public long ensureObjectId(final Object object, final PersistenceAcceptor newObjectIdCallback)
		{
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isNotFoundId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					/* (19.07.2019 TM)NOTE:
					 * The objectId is provided prior to ensuring the TypeHandler (which happens via the callback),
					 * meaning even instances of types that throw an exception upon type analysis will get
					 * cause an objectId to be reserved/allocated.
					 * However, if the type analysis throws an exception, the instance is not registered at the
					 * object registry, hence not causing any inconsistent state.
					 * The objectId is "lost", but that is not a problem since it is no different from a
					 * deleted entity. And there's the type analysis exception, anyway which stops the whole process.
					 * See PersistenceTypeHandler#guaranteeInstanceViablity.
					 */
					objectId = this.oidProvider.provideNextObjectId();
					if(newObjectIdCallback != null)
					{
						newObjectIdCallback.accept(objectId, object);
					}
					this.objectRegistry.registerObject(objectId, object);
				}
				
				return objectId;
			}
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
