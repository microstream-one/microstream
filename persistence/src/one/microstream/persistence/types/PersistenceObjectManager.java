package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.util.Cloneable;

public interface PersistenceObjectManager
extends PersistenceObjectLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager>
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


	
	
	public static PersistenceObjectManager.Implementation New(
		final PersistenceObjectRegistry   objectRegistry,
		final PersistenceObjectIdProvider oidProvider
	)
	{
		return new PersistenceObjectManager.Implementation(
			notNull(objectRegistry),
			notNull(oidProvider)
		);
	}

	public final class Implementation implements PersistenceObjectManager
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final PersistenceObjectRegistry   objectRegistry;
		private final PersistenceObjectIdProvider oidProvider   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
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
		public PersistenceObjectManager.Implementation Clone()
		{
			/*
			 * This basically turns the globally connected manager instance into a standalone clone.
			 * The oidProvider must support cloning, e.g. be transient instead of persisting into a
			 * single target location.
			 */
			synchronized(this.objectRegistry)
			{
				return new PersistenceObjectManager.Implementation(
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
		public Object lookupObject(final long oid)
		{
//			XDebug.debugln(XChars.systemString(this) + " looking up \n" + oid
//				+ " -> " + XChars.systemString(this.objectRegistry.lookupObject(oid))
//			);
			synchronized(this.objectRegistry)
			{
				return this.objectRegistry.lookupObject(oid);
			}
		}
		
		private void validate(final Object object)
		{
			if(object instanceof Class<?>)
			{
				/* (23.11.2018 TM)FIXME: proper check for invalid types
				 * There are more invalid types than just Classes.
				 * All have to be checked, probably by consolidating this implementation with the already
				 * existing registry for invalid types.
				 * Or the check has to be done in the calling context, documented by a comment here.
				 */
				
				// (23.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Invalid Class metadata instance: " + object.toString());
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
				long oid;
				if((oid = this.objectRegistry.lookupObjectId(object)) == Persistence.nullId())
				{
					this.validate(object);
					oid = this.oidProvider.provideNextObjectId();
					if(newObjectIdCallback != null)
					{
						newObjectIdCallback.accept(oid, object);
					}
					this.objectRegistry.registerObject(oid, object);
				}
				
				return oid;
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
