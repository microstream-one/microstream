package net.jadoth.persistence.types;

import net.jadoth.X;
import net.jadoth.chars.VarString;
import net.jadoth.chars.XParsing;
import net.jadoth.collections.types.XReference;
import net.jadoth.exceptions.ParsingException;

public interface PersistenceTypeIdStrategy
{
	public PersistenceTypeIdProvider createTypeIdProvider();

	public String strategyTypeNameTypeId();
	
	
	public static PersistenceTypeIdStrategy.Transient Transient()
	{
		return new PersistenceTypeIdStrategy.Transient(Persistence.defaultStartTypeId());
	}
	
	public static PersistenceTypeIdStrategy.Transient Transient(final long startingTypeId)
	{
		return new PersistenceTypeIdStrategy.Transient(Persistence.validateTypeId(startingTypeId));
	}
	
	public final class Transient implements PersistenceTypeIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "Transient";
		}
		
		public static void assemble(final VarString vs, final PersistenceTypeIdStrategy.Transient idStrategy)
		{
			vs
			.add(PersistenceTypeIdStrategy.Transient.typeName())
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
		
		public static PersistenceTypeIdStrategy.Transient parse(final String typeIdStrategyContent)
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceTypeIdStrategy.Transient.class,
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
				? PersistenceTypeIdStrategy.Transient()
				: PersistenceTypeIdStrategy.Transient(Long.parseLong(valueString.get()))
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
		public final PersistenceTypeIdProvider createTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Transient(this.startingTypeId);
		}
		
	}
	
	
	
	public static PersistenceTypeIdStrategy.None None()
	{
		return new PersistenceTypeIdStrategy.None();
	}
	
	public final class None implements PersistenceTypeIdStrategy
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String typeName()
		{
			// intentionally not the class name since it must stay the same, even if the class should get renamed.
			return "None";
		}
		
		public static void assemble(final VarString vs, final PersistenceTypeIdStrategy.None idStrategy)
		{
			vs
			.add(PersistenceTypeIdStrategy.None.typeName())
			;
		}
		
		public static PersistenceTypeIdStrategy.None parse(final String typeIdStrategyContent) throws ParsingException
		{
			PersistenceIdStrategyStringConverter.validateIdStrategyName(
				PersistenceTypeIdStrategy.None.class,
				typeName()                      ,
				typeIdStrategyContent
			);
			
			// the rest of the string is ignored intentionally.
			return PersistenceTypeIdStrategy.None();
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
		public final PersistenceTypeIdProvider createTypeIdProvider()
		{
			return PersistenceTypeIdProvider.Failing();
		}
		
	}
	
	@FunctionalInterface
	public interface Assembler<S extends PersistenceTypeIdStrategy>
	{
		public void assembleIdStrategy(VarString vs, S idStrategy);
	}
	
	@FunctionalInterface
	public interface Parser<S extends PersistenceTypeIdStrategy>
	{
		public S parse(String typeIdStrategyContent);
	}
	
}
