package net.jadoth.swizzling.types;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.chars.XParsing;
import net.jadoth.collections.types.XReference;
import net.jadoth.exceptions.ParsingException;

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
		
		public static char openingCharacter()
		{
			return '(';
		}
		
		public static char closingCharacter()
		{
			return ')';
		}
		
		public static void assemble(final VarString vs, final SwizzleObjectIdStrategy.Transient idStrategy)
		{
			vs
			.add(SwizzleObjectIdStrategy.Transient.typeName())
			.add(openingCharacter()).add(idStrategy.startingObjectId()).add(closingCharacter())
			;
		}
		
		public static SwizzleObjectIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			SwizzleIdStrategyStringConverter.validateIdStrategyName(
				SwizzleObjectIdStrategy.Transient.class,
				typeName(),
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
				? SwizzleObjectIdStrategy.Transient()
				: SwizzleObjectIdStrategy.Transient(Long.parseLong(valueString.get()))
			;
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
