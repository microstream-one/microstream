package one.microstream.persistence.types;

public interface PersistenceTypeIdProvider extends PersistenceTypeIdHolder
{
	public long provideNextTypeId();

	public PersistenceTypeIdProvider initializeTypeId();

	public PersistenceTypeIdProvider updateCurrentTypeId(long currentTypeId);
	
	
	
	public static PersistenceTypeIdProvider Transient()
	{
		return new Transient(Persistence.defaultStartTypeId());
	}
	
	public static PersistenceTypeIdProvider Transient(final long startingTypeId)
	{
		return new Transient(Persistence.validateTypeId(startingTypeId));
	}
	
	public final class Transient implements PersistenceTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private long currentTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingTypeId)
		{
			super();
			this.currentTypeId = startingTypeId;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public final synchronized long provideNextTypeId()
		{
			return ++this.currentTypeId;
		}

		@Override
		public final synchronized long currentTypeId()
		{
			return this.currentTypeId;
		}

		@Override
		public final Transient initializeTypeId()
		{
			return this;
		}

		@Override
		public final synchronized PersistenceTypeIdProvider updateCurrentTypeId(final long currentTypeId)
		{
			this.currentTypeId = currentTypeId;
			return this;
		}

	}
	
	public static PersistenceTypeIdProvider.Failing Failing()
	{
		return new PersistenceTypeIdProvider.Failing();
	}
	
	public final class Failing implements PersistenceTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private long currentTypeId;
		
		
		
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
		public PersistenceTypeIdProvider.Failing initializeTypeId()
		{
			// no-op, nothing to initialize
			return this;
		}

		@Override
		public long currentTypeId()
		{
			return this.currentTypeId;
		}

		@Override
		public PersistenceTypeIdProvider.Failing updateCurrentTypeId(final long currentTypeId)
		{
			this.currentTypeId = currentTypeId;
			return this;
		}

		@Override
		public long provideNextTypeId()
		{
			throw new UnsupportedOperationException();
		}
		
	}
	
}
