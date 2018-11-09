package net.jadoth.swizzling.types;

public interface SwizzleTypeIdProvider extends SwizzleTypeIdHolder
{
	public long provideNextTypeId();

	public SwizzleTypeIdProvider initializeTypeId();

	public SwizzleTypeIdProvider updateCurrentTypeId(long currentTypeId);
	
	
	
	public static SwizzleTypeIdProvider Transient()
	{
		return new Transient(Swizzle.defaultStartTypeId());
	}
	
	public static SwizzleTypeIdProvider Transient(final long startingTypeId)
	{
		return new Transient(Swizzle.validateTypeId(startingTypeId));
	}
	
	public final class Transient implements SwizzleTypeIdProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private long currentTypeId;



		///////////////////////////////////////////////////////////////////////////
		// constructors     //
		/////////////////////

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
		public final synchronized SwizzleTypeIdProvider updateCurrentTypeId(final long currentTypeId)
		{
			this.currentTypeId = currentTypeId;
			return this;
		}

	}
	
	public static SwizzleTypeIdProvider.Failing Failing()
	{
		return new SwizzleTypeIdProvider.Failing();
	}
	
	public final class Failing implements SwizzleTypeIdProvider
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
		public SwizzleTypeIdProvider.Failing initializeTypeId()
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
		public SwizzleTypeIdProvider.Failing updateCurrentTypeId(final long currentTypeId)
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
