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
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}
		
		public static void assemble(final VarString vs, final SwizzleTypeIdStrategy.Transient idStrategy)
		{
			vs
			.add(SwizzleTypeIdStrategy.Transient.typeName())
			.add('(').add(idStrategy.startingTypeId()).add(')')
			;
		}
		
		public static SwizzleTypeIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeIdStrategy.Transient#parse()
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
		
		public final long startingTypeId()
		{
			return this.startingTypeId;
		}
		
		@Override
		public String strategyTypeNameTypeId()
		{
			return Transient.typeName();
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
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "None";
		}
		
		public static void assemble(final VarString vs, final SwizzleTypeIdStrategy.None idStrategy)
		{
			vs
			.add(SwizzleTypeIdStrategy.None.typeName())
			;
		}
		
		public static SwizzleTypeIdStrategy.None parse(final String typeIdStrategyContent)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleTypeIdStrategy.None#parse()
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
			return None.typeName();
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
	
	@FunctionalInterface
	public interface Parser<S extends SwizzleTypeIdStrategy>
	{
		public S parse(String typeIdStrategyContent);
	}
	
}
