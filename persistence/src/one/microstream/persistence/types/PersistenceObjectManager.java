package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import one.microstream.reference.Swizzling;
import one.microstream.util.Cloneable;

public interface PersistenceObjectManager
extends PersistenceSwizzlingLookup, PersistenceObjectIdHolder, Cloneable<PersistenceObjectManager>
{
	public long ensureObjectId(Object object, PersistenceObjectIdConsumer objectIdConsumer);

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
		
		// (17.03.2020 TM)FIXME: priv#182: register objectIdConsumers
		private final PersistenceObjectIdConsumer[] consumers = new PersistenceObjectIdConsumer[1];



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
		public long ensureObjectId(final Object object, final PersistenceObjectIdConsumer consumer)
		{
			synchronized(this.objectRegistry)
			{
				long objectId;
				if(Swizzling.isProperId(objectId = this.objectRegistry.lookupObjectId(object)))
				{
					return objectId;
				}
				if(Swizzling.isProperId(objectId = this.synchCheckConsumers(consumer, object)))
				{
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
				if(consumer != null)
				{
					consumer.accept(objectId, object);
				}
				
				return objectId;
			}
		}
		
		private long synchCheckConsumers(
			final PersistenceObjectIdConsumer requestingConsumer,
			final Object                              instance
		)
		{
			for(final PersistenceObjectIdConsumer consumer : this.consumers)
			{
				if(consumer == null)
				{
					// reached end of consumers (first null-entry).
					break;
				}
				if(consumer == requestingConsumer)
				{
					continue;
				}
				
				final long objectId;
				if(Swizzling.isProperId(objectId = consumer.lookupObjectId(instance, requestingConsumer)))
				{
					return objectId;
				}
			}
			
			return Swizzling.notFoundId();
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
