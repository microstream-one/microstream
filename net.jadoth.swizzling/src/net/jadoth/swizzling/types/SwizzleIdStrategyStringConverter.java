package net.jadoth.swizzling.types;

import static net.jadoth.X.notNull;

import net.jadoth.X;
import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars.XParsing;
import net.jadoth.chars._charArrayRange;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.types.XImmutableMap;
import net.jadoth.collections.types.XReference;
import net.jadoth.exceptions.ParsingException;
import net.jadoth.typing.KeyValue;

public interface SwizzleIdStrategyStringConverter extends ObjectStringConverter<SwizzleIdStrategy>
{
	@Override
	public VarString assemble(VarString vs, SwizzleIdStrategy subject);
	
	@Override
	public default VarString provideAssemblyBuffer()
	{
		return ObjectStringConverter.super.provideAssemblyBuffer();
	}
	
	@Override
	public default String assemble(final SwizzleIdStrategy subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}
	
	@Override
	public SwizzleIdStrategy parse(_charArrayRange input);

	@Override
	public default SwizzleIdStrategy parse(final String input)
	{
		return ObjectStringConverter.super.parse(input);
	}
	
	
	public static void validateIdStrategyName(
		final Class<?> idStrategyType   ,
		final String   idStrategyName   ,
		final String   idStrategyContent
	)
		throws ParsingException
	{
		if(idStrategyContent.startsWith(idStrategyName))
		{
			return;
		}
		
		// (06.11.2018 TM)EXCP: proper exception
		throw new ParsingException(
			"Invalid id strategy content for type name \"" + idStrategyName + "\""
			+ " of type " + idStrategyType.getName()
			+ ": " + idStrategyContent
		);
	}
	
	
	
	public static SwizzleIdStrategyStringConverter.Creator Creator()
	{
		return new SwizzleIdStrategyStringConverter.Creator.Implementation();
	}
	
	public static interface Creator
	{
		public <S extends SwizzleObjectIdStrategy> Creator register(
			Class<S>                             objectIdStrategyType,
			SwizzleObjectIdStrategy.Assembler<S> assembler
		);
		
		public <S extends SwizzleTypeIdStrategy> Creator register(
			Class<S>                           typeIdStrategyType,
			SwizzleTypeIdStrategy.Assembler<S> assembler
		);
		
		public <S extends SwizzleObjectIdStrategy> Creator register(
			String                            strategyTypeName,
			SwizzleObjectIdStrategy.Parser<S> parser
		);
		
		public <S extends SwizzleTypeIdStrategy> Creator register(
			String                          strategyTypeName,
			SwizzleTypeIdStrategy.Parser<S> parser
		);
		
		public SwizzleIdStrategyStringConverter create();
		
		
		
		public final class Implementation implements SwizzleIdStrategyStringConverter.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final HashTable<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> oidAssemblers = HashTable.New();
			private final HashTable<Class<?>, SwizzleTypeIdStrategy.Assembler<?>>   tidAssemblers = HashTable.New();
			
			private final EqHashTable<String, SwizzleObjectIdStrategy.Parser<?>> oidParsers = EqHashTable.New();
			private final EqHashTable<String, SwizzleTypeIdStrategy.Parser<?>>   tidParsers = EqHashTable.New();
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public synchronized <S extends SwizzleObjectIdStrategy> Creator.Implementation register(
				final Class<S>                             objectIdStrategyType,
				final SwizzleObjectIdStrategy.Assembler<S> assembler
			)
			{
				this.oidAssemblers.put(
					notNull(objectIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends SwizzleTypeIdStrategy>  Creator.Implementation register(
				final Class<S>                           typeIdStrategyType,
				final SwizzleTypeIdStrategy.Assembler<S> assembler
			)
			{
				this.tidAssemblers.put(
					notNull(typeIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends SwizzleObjectIdStrategy> Creator register(
				final String                            strategyTypeName,
				final SwizzleObjectIdStrategy.Parser<S> parser
			)
			{
				this.oidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public synchronized <S extends SwizzleTypeIdStrategy> Creator register(
				final String                          strategyTypeName,
				final SwizzleTypeIdStrategy.Parser<S> parser
			)
			{
				this.tidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public final synchronized SwizzleIdStrategyStringConverter create()
			{
				return new SwizzleIdStrategyStringConverter.Implementation(
					this.oidAssemblers.immure(),
					this.tidAssemblers.immure(),
					this.oidParsers.immure()   ,
					this.tidParsers.immure()
				);
			}
			
		}
		
	}
	
	public static SwizzleIdStrategyStringConverter New()
	{
		// generics magic! 8-)
		return Creator()
			.register(SwizzleObjectIdStrategy.Transient.class     , SwizzleObjectIdStrategy.Transient::assemble)
			.register(SwizzleObjectIdStrategy.Transient.typeName(), SwizzleObjectIdStrategy.Transient::parse)
			
			.register(SwizzleTypeIdStrategy.Transient.class     , SwizzleTypeIdStrategy.Transient::assemble)
			.register(SwizzleTypeIdStrategy.Transient.typeName(), SwizzleTypeIdStrategy.Transient::parse)
			
			.register(SwizzleTypeIdStrategy.None.class     , SwizzleTypeIdStrategy.None::assemble)
			.register(SwizzleTypeIdStrategy.None.typeName(), SwizzleTypeIdStrategy.None::parse)
			.create()
		;
	}
	
	public final class Implementation implements SwizzleIdStrategyStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static String labelType()
		{
			return "Type";
		}
		
		public static String labelObject()
		{
			return "Object";
		}
		
		public static char typeAssigner()
		{
			return ':';
		}
		
		public static char separator()
		{
			return ',';
		}
		
		public static char quote()
		{
			return '\'';
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final XImmutableMap<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> oidsAssemblers;
		final XImmutableMap<Class<?>, SwizzleTypeIdStrategy.Assembler<?>  > tidsAssemblers;
		final XImmutableMap<String, SwizzleObjectIdStrategy.Parser<?>>      oidsParsers   ;
		final XImmutableMap<String, SwizzleTypeIdStrategy.Parser<?>>        tidsParsers   ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(
			final XImmutableMap<Class<?>, SwizzleObjectIdStrategy.Assembler<?>> oidsAssemblers,
			final XImmutableMap<Class<?>, SwizzleTypeIdStrategy.Assembler<?>  > tidsAssemblers,
			final XImmutableMap<String, SwizzleObjectIdStrategy.Parser<?>>      oidsParsers   ,
			final XImmutableMap<String, SwizzleTypeIdStrategy.Parser<?>>        tidsParsers
		)
		{
			super();
			this.oidsAssemblers = oidsAssemblers;
			this.tidsAssemblers = tidsAssemblers;
			this.oidsParsers    = oidsParsers   ;
			this.tidsParsers    = tidsParsers   ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private <S extends SwizzleObjectIdStrategy> SwizzleObjectIdStrategy.Assembler<S> lookupObjectIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final SwizzleObjectIdStrategy.Assembler<S> assembler =
				(SwizzleObjectIdStrategy.Assembler<S>)this.oidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends SwizzleTypeIdStrategy> SwizzleTypeIdStrategy.Assembler<S> lookupTypeIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final SwizzleTypeIdStrategy.Assembler<S> assembler =
				(SwizzleTypeIdStrategy.Assembler<S>)this.tidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends SwizzleObjectIdStrategy> void assembleObjectIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final SwizzleObjectIdStrategy.Assembler<S> assembler = this.lookupObjectIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		private <S extends SwizzleTypeIdStrategy> void assembleTypeIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final SwizzleTypeIdStrategy.Assembler<S> assembler = this.lookupTypeIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		@Override
		public VarString assemble(final VarString vs, final SwizzleIdStrategy idStrategy)
		{
			vs
			.add(labelType()).add(typeAssigner()).blank().apply(v ->
				this.assembleTypeIdStrategy(v, idStrategy.typeIdStragegy())
			).add(separator())
			.add(labelObject()).add(typeAssigner()).blank().apply(v ->
				this.assembleObjectIdStrategy(v, idStrategy.objectIdStragegy())
			);
			
			return vs;
		}

		@Override
		public SwizzleIdStrategy parse(final _charArrayRange input)
		{
			final XReference<String> tidsContent = X.Reference(null);
			final XReference<String> oidsContent = X.Reference(null);
			
			parseContent(input, tidsContent, oidsContent);
			
			final SwizzleTypeIdStrategy.Parser<?>   tidsParser = this.lookupTypeIdStrategyParser(tidsContent.get());
			final SwizzleObjectIdStrategy.Parser<?> oidsParser = this.lookupObjectIdStrategyParser(oidsContent.get());
			
			final SwizzleTypeIdStrategy   tidStrategy = tidsParser.parse(tidsContent.get());
			final SwizzleObjectIdStrategy oidStrategy = oidsParser.parse(oidsContent.get());
			
			return SwizzleIdStrategy.New(oidStrategy, tidStrategy);
		}
		
		private static void parseContent(
			final _charArrayRange    inputRange ,
			final XReference<String> tidsContent,
			final XReference<String> oidsContent
		)
		{
			final char[] input = inputRange.array();
			final int iBound = XParsing.skipWhiteSpacesReverse(input, inputRange.start(), inputRange.bound());
			
			final int iTypeEnd = parsePart(input, inputRange.start(), iBound, labelType(), tidsContent);
			
			XParsing.checkIncompleteInput(iTypeEnd, iBound);
			XParsing.checkCharacter(input, iTypeEnd, separator(), "IdStrategy");
			
			parsePart(input, iTypeEnd + 1, iBound, labelObject(), oidsContent);
		}
		
		private static int parsePart(
			final char[]             input        ,
			final int                iStart       ,
			final int                iBound       ,
			final String             label        ,
			final XReference<String> contentHolder
		)
		{
			int i = iStart;
			
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			XParsing.checkIncompleteInput(i, iBound);
			i = checkStartsWith          (input, i, iBound, label);
			i = XParsing.skipWhiteSpaces (input, i, iBound);

			XParsing.checkIncompleteInput(i, iBound);
			i = XParsing.checkCharacter  (input, i, typeAssigner(), label);
			i = XParsing.skipWhiteSpaces (input, i, iBound);

			XParsing.checkIncompleteInput(i, iBound);
			XParsing.checkCharacter      (input, i, quote(), label);
			i = XParsing.parseSimpleQuote(input, i, iBound, contentHolder);
			i = XParsing.skipWhiteSpaces (input, i, iBound);
			
			return i;
		}
				
		private static int checkStartsWith(
			final char[] input ,
			final int    i     ,
			final int    iBound,
			final String label
		)
		{
			if(XParsing.startsWith(input, i, iBound, label))
			{
				return i + label.length();
			}
			
			// (06.11.2018 TM)EXCP: proper exception
			throw new RuntimeException("IdStrategy type label \"" + label + "\" not found at index " + i + ".");
		}
		
		private SwizzleTypeIdStrategy.Parser<?> lookupTypeIdStrategyParser(final String content)
		{
			for(final KeyValue<String, SwizzleTypeIdStrategy.Parser<?>> e : this.tidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			// (05.11.2018 TM)EXCP: proper exception
			throw new RuntimeException("Unknown TypeIdStrategy: \"" + content + "\".");
		}
		
		private SwizzleObjectIdStrategy.Parser<?> lookupObjectIdStrategyParser(final String content)
		{
			for(final KeyValue<String, SwizzleObjectIdStrategy.Parser<?>> e : this.oidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			// (05.11.2018 TM)EXCP: proper exception
			throw new RuntimeException("Unknown ObjectIdStrategy: \"" + content + "\".");
		}
				
	}
		
}
