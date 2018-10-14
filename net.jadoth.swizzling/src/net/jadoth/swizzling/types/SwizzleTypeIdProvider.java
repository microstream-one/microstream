package net.jadoth.swizzling.types;


public interface SwizzleTypeIdProvider extends SwizzleTypeIdHolder
{
	public long provideNextTypeId();

	public SwizzleTypeIdProvider initializeTypeId();

	public SwizzleTypeIdProvider updateCurrentTypeId(long currentTypeId);
	
	
	
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
		public SwizzleTypeIdProvider initializeTypeId()
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
		public SwizzleTypeIdProvider updateCurrentTypeId(final long currentTypeId)
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
