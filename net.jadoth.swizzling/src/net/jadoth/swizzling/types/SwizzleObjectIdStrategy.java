package net.jadoth.swizzling.types;

import net.jadoth.chars.VarString;

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
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}
		
		public static void assemble(final VarString vs, final SwizzleObjectIdStrategy.Transient idStrategy)
		{
			vs
			.add(SwizzleObjectIdStrategy.Transient.typeName())
			.add('(').add(idStrategy.startingObjectId()).add(')')
			;
		}
		
		public static SwizzleObjectIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME SwizzleObjectIdStrategy.Transient#parse()
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
		public String strategyTypeNameObjectId()
		{
			return Transient.typeName();
		}

		@Override
		public final SwizzleObjectIdProvider createObjectIdProvider()
		{
			return SwizzleObjectIdProvider.Transient(this.startingObjectId);
		}
		
	}
	
	@FunctionalInterface
	public interface Assembler<S extends SwizzleObjectIdStrategy>
	{
		public void assembleIdStrategy(VarString vs, S idStrategy);
	}
	
	@FunctionalInterface
	public interface Parser<S extends SwizzleObjectIdStrategy>
	{
		public S parse(String typeIdStrategyContent);
	}
	
}
