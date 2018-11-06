package net.jadoth.swizzling.types;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.chars.XParsing;
import net.jadoth.collections.types.XReference;
import net.jadoth.exceptions.ParsingException;

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
			.add(openingCharacter()).add(idStrategy.startingTypeId()).add(closingCharacter())
			;
		}
		
		public static char openingCharacter()
		{
			return '(';
		}
		
		public static char closingCharacter()
		{
			return ')';
		}
		
		public static SwizzleTypeIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			SwizzleIdStrategyStringConverter.validateIdStrategyName(
				SwizzleTypeIdStrategy.Transient.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			final char[] input  = typeIdStrategyContent.toCharArray();
			final int    iBound = input.length;
			
			final XReference<String> valueString = X.Reference(null);
			
			int i = typeName().length();
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			i = XParsing.checkCharacter(input, i, openingCharacter(), typeName());
			i = XParsing.parseToSimpleTerminator(input, i, iBound, closingCharacter(), valueString);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			if(i != iBound)
			{
				// (06.11.2018 TM)EXCP: proper exception
				throw new ParsingException("Invalid trailing content at index " + i);
			}
			
			return valueString.get().isEmpty()
				? SwizzleTypeIdStrategy.Transient()
				: SwizzleTypeIdStrategy.Transient(Long.parseLong(valueString.get()))
			;
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
		
		public static SwizzleTypeIdStrategy.None parse(final String typeIdStrategyContent) throws ParsingException
		{
			SwizzleIdStrategyStringConverter.validateIdStrategyName(
				SwizzleTypeIdStrategy.None.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			// the rest of the string is ignored intentionally.
			return SwizzleTypeIdStrategy.None();
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
