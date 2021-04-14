package one.microstream.persistence.types;

import one.microstream.util.Cloneable;

public interface PersistenceObjectIdProvider
extends PersistenceObjectIdHolder, Cloneable<PersistenceObjectIdProvider>
{
	public long provideNextObjectId();

	public PersistenceObjectIdProvider initializeObjectId();
	
	@Override
	public long currentObjectId();

	@Override
	public PersistenceObjectIdProvider updateCurrentObjectId(long currentObjectId);
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public default PersistenceObjectIdProvider Clone()
	{
		return Cloneable.super.Clone();
	}
	
	
	public static PersistenceObjectIdProvider Transient()
	{
		return new Transient(Persistence.defaultStartObjectId());
	}
	
	public static PersistenceObjectIdProvider Transient(final long startingObjectId)
	{
		return new Transient(Persistence.validateObjectId(startingObjectId));
	}
	
	public final class Transient implements PersistenceObjectIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long currentObjectId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.currentObjectId = startingObjectId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////
		
		@Override
		public final synchronized PersistenceObjectIdProvider.Transient Clone()
		{
			return new PersistenceObjectIdProvider.Transient(this.currentObjectId);
		}

		@Override
		public final synchronized long provideNextObjectId()
		{
			return ++this.currentObjectId;
		}

		@Override
		public final synchronized long currentObjectId()
		{
			return this.currentObjectId;
		}

		@Override
		public final Transient initializeObjectId()
		{
			return this;
		}

		@Override
		public final synchronized PersistenceObjectIdProvider updateCurrentObjectId(final long currentObjectId)
		{
			this.currentObjectId = currentObjectId;
			return this;
		}

	}
	
	public static PersistenceObjectIdProvider.Failing Failing()
	{
		return new PersistenceObjectIdProvider.Failing();
	}
	
	public final class Failing implements PersistenceObjectIdProvider
	{

		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long currentObjectId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Failing()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceObjectIdProvider.Failing Clone()
		{
			return new PersistenceObjectIdProvider.Failing();
		}

		@Override
		public PersistenceObjectIdProvider.Failing initializeObjectId()
		{
			// no-op, nothing to initialize
			return this;
		}

		@Override
		public long currentObjectId()
		{
			return this.currentObjectId;
		}

		@Override
		public PersistenceObjectIdProvider.Failing updateCurrentObjectId(final long currentObjectId)
		{
			this.currentObjectId = currentObjectId;
			return this;
		}

		@Override
		public long provideNextObjectId()
		{
			throw new UnsupportedOperationException();
		}
	}
	
}
