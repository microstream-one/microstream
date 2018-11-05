package net.jadoth.swizzling.types;

public interface SwizzleTypeIdStrategy
{
	public SwizzleTypeIdProvider createTypeIdProvider();

	public String strategyTypeNameTypeId();
	
	
	public static SwizzleTypeIdStrategy.Transient Transient()
	{
		return new SwizzleTypeIdStrategy.Transient(Swizzle.defaultStartTypeId());
	}
	
	public static SwizzleTypeIdStrategy.Transient Transient(final long startingTypeId)
	{
		return new SwizzleTypeIdStrategy.Transient(Swizzle.validateTypeId(startingTypeId));
	}
	
	public final class Transient implements SwizzleTypeIdStrategy
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
		public String strategyTypeNameTypeId()
		{
			return Transient.strategyTypeName();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final long startingTypeId;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Transient(final long startingTypeId)
		{
			super();
			this.startingTypeId = startingTypeId;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final SwizzleTypeIdProvider createTypeIdProvider()
		{
			return SwizzleTypeIdProvider.Transient(this.startingTypeId);
		}
		
	}
	
	
	
	public static SwizzleTypeIdStrategy.None None()
	{
		return new SwizzleTypeIdStrategy.None();
	}
	
	public final class None implements SwizzleTypeIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String strategyTypeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "None";
		}
		
		@Override
		public String strategyTypeNameTypeId()
		{
			return None.strategyTypeName();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		None()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final SwizzleTypeIdProvider createTypeIdProvider()
		{
			return SwizzleTypeIdProvider.Failing();
		}
		
	}
	
}
