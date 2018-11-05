package net.jadoth.swizzling.types;

public interface SwizzleObjectIdStrategy
{
	public SwizzleObjectIdProvider createObjectIdProvider();
	
	public String strategyTypeNameObjectId();
	
	
	
	public static SwizzleObjectIdStrategy.Transient Transient()
	{
		return new SwizzleObjectIdStrategy.Transient(Swizzle.defaultStartObjectId());
	}
	
	public static SwizzleObjectIdStrategy.Transient Transient(final long startingObjectId)
	{
		return new SwizzleObjectIdStrategy.Transient(Swizzle.validateObjectId(startingObjectId));
	}
	
	public final class Transient implements SwizzleObjectIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String strategyTypeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}
		
		@Override
		public String strategyTypeNameObjectId()
		{
			return Transient.strategyTypeName();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingObjectId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingObjectId)
		{
			super();
			this.startingObjectId = startingObjectId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final long startingObjectId()
		{
			return this.startingObjectId;
		}

		@Override
		public final SwizzleObjectIdProvider createObjectIdProvider()
		{
			return SwizzleObjectIdProvider.Transient(this.startingObjectId);
		}
		
	}
	
}
