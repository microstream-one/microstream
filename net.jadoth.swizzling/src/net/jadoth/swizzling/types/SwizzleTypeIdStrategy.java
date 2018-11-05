package net.jadoth.swizzling.types;

import net.jadoth.chars.VarString;

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
		
		public static void assemble(final VarString vs, final SwizzleTypeIdStrategy.Transient idStrategy)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeIdStrategy.Transient#assemble()
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
		public String strategyTypeNameTypeId()
		{
			return Transient.strategyTypeName();
		}

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
		
		public static void assemble(final VarString vs, final SwizzleTypeIdStrategy.None idStrategy)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeIdStrategy.None#assemble()
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
		public String strategyTypeNameTypeId()
		{
			return None.strategyTypeName();
		}

		@Override
		public final SwizzleTypeIdProvider createTypeIdProvider()
		{
			return SwizzleTypeIdProvider.Failing();
		}
		
	}
	
	@FunctionalInterface
	public interface Assembler<S extends SwizzleTypeIdStrategy>
	{
		public void assembleIdStrategy(VarString vs, S idStrategy);
	}
	
}
