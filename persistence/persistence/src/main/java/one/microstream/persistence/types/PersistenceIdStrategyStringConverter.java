package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import static one.microstream.X.notNull;

import one.microstream.X;
import one.microstream.chars.ObjectStringConverter;
import one.microstream.chars.VarString;
import one.microstream.chars.XParsing;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.HashTable;
import one.microstream.collections.types.XImmutableMap;
import one.microstream.collections.types.XReference;
import one.microstream.exceptions.ParsingException;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.typing.KeyValue;

public interface PersistenceIdStrategyStringConverter extends ObjectStringConverter<PersistenceIdStrategy>
{
	@Override
	public VarString assemble(VarString vs, PersistenceIdStrategy subject);
	
	@Override
	public default VarString provideAssemblyBuffer()
	{
		return ObjectStringConverter.super.provideAssemblyBuffer();
	}
	
	@Override
	public default String assemble(final PersistenceIdStrategy subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}
	
	@Override
	public PersistenceIdStrategy parse(_charArrayRange input);

	@Override
	public default PersistenceIdStrategy parse(final String input)
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
		
		throw new ParsingException(
			"Invalid id strategy content for type name \"" + idStrategyName + "\""
			+ " of type " + idStrategyType.getName()
			+ ": " + idStrategyContent
		);
	}
	
	
	
	public static PersistenceIdStrategyStringConverter.Creator Creator()
	{
		return new PersistenceIdStrategyStringConverter.Creator.Default();
	}
	
	public static interface Creator
	{
		public <S extends PersistenceObjectIdStrategy> Creator register(
			Class<S>                             objectIdStrategyType,
			PersistenceObjectIdStrategy.Assembler<S> assembler
		);
		
		public <S extends PersistenceTypeIdStrategy> Creator register(
			Class<S>                           typeIdStrategyType,
			PersistenceTypeIdStrategy.Assembler<S> assembler
		);
		
		public <S extends PersistenceObjectIdStrategy> Creator register(
			String                            strategyTypeName,
			PersistenceObjectIdStrategy.Parser<S> parser
		);
		
		public <S extends PersistenceTypeIdStrategy> Creator register(
			String                          strategyTypeName,
			PersistenceTypeIdStrategy.Parser<S> parser
		);
		
		public PersistenceIdStrategyStringConverter create();
		
		
		
		public final class Default implements PersistenceIdStrategyStringConverter.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final HashTable<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidAssemblers = HashTable.New();
			private final HashTable<Class<?>, PersistenceTypeIdStrategy.Assembler<?>>   tidAssemblers = HashTable.New();
			
			private final EqHashTable<String, PersistenceObjectIdStrategy.Parser<?>> oidParsers = EqHashTable.New();
			private final EqHashTable<String, PersistenceTypeIdStrategy.Parser<?>>   tidParsers = EqHashTable.New();
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default()
			{
				super();
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public synchronized <S extends PersistenceObjectIdStrategy> Creator.Default register(
				final Class<S>                             objectIdStrategyType,
				final PersistenceObjectIdStrategy.Assembler<S> assembler
			)
			{
				this.oidAssemblers.put(
					notNull(objectIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceTypeIdStrategy>  Creator.Default register(
				final Class<S>                           typeIdStrategyType,
				final PersistenceTypeIdStrategy.Assembler<S> assembler
			)
			{
				this.tidAssemblers.put(
					notNull(typeIdStrategyType),
					notNull(assembler)
				);

				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceObjectIdStrategy> Creator register(
				final String                            strategyTypeName,
				final PersistenceObjectIdStrategy.Parser<S> parser
			)
			{
				this.oidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public synchronized <S extends PersistenceTypeIdStrategy> Creator register(
				final String                          strategyTypeName,
				final PersistenceTypeIdStrategy.Parser<S> parser
			)
			{
				this.tidParsers.put(
					notNull(strategyTypeName),
					notNull(parser)
				);
				
				return this;
			}
			
			@Override
			public final synchronized PersistenceIdStrategyStringConverter create()
			{
				return new PersistenceIdStrategyStringConverter.Default(
					this.oidAssemblers.immure(),
					this.tidAssemblers.immure(),
					this.oidParsers.immure()   ,
					this.tidParsers.immure()
				);
			}
			
		}
		
	}
	
	public static PersistenceIdStrategyStringConverter New()
	{
		// generics magic! 8-)
		return Creator()
			.register(PersistenceObjectIdStrategy.Transient.class     , PersistenceObjectIdStrategy.Transient::assemble)
			.register(PersistenceObjectIdStrategy.Transient.typeName(), PersistenceObjectIdStrategy.Transient::parse)
			
			.register(PersistenceObjectIdStrategy.None.class     , PersistenceObjectIdStrategy.None::assemble)
			.register(PersistenceObjectIdStrategy.None.typeName(), PersistenceObjectIdStrategy.None::parse)
			
			.register(PersistenceTypeIdStrategy.Transient.class     , PersistenceTypeIdStrategy.Transient::assemble)
			.register(PersistenceTypeIdStrategy.Transient.typeName(), PersistenceTypeIdStrategy.Transient::parse)
			
			.register(PersistenceTypeIdStrategy.None.class     , PersistenceTypeIdStrategy.None::assemble)
			.register(PersistenceTypeIdStrategy.None.typeName(), PersistenceTypeIdStrategy.None::parse)
			.create()
		;
	}
	
	public final class Default implements PersistenceIdStrategyStringConverter
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
		
		final XImmutableMap<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidsAssemblers;
		final XImmutableMap<Class<?>, PersistenceTypeIdStrategy.Assembler<?>  > tidsAssemblers;
		final XImmutableMap<String, PersistenceObjectIdStrategy.Parser<?>>      oidsParsers   ;
		final XImmutableMap<String, PersistenceTypeIdStrategy.Parser<?>>        tidsParsers   ;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(
			final XImmutableMap<Class<?>, PersistenceObjectIdStrategy.Assembler<?>> oidsAssemblers,
			final XImmutableMap<Class<?>, PersistenceTypeIdStrategy.Assembler<?>  > tidsAssemblers,
			final XImmutableMap<String, PersistenceObjectIdStrategy.Parser<?>>      oidsParsers   ,
			final XImmutableMap<String, PersistenceTypeIdStrategy.Parser<?>>        tidsParsers
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
		
		private <S extends PersistenceObjectIdStrategy> PersistenceObjectIdStrategy.Assembler<S> lookupObjectIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final PersistenceObjectIdStrategy.Assembler<S> assembler =
				(PersistenceObjectIdStrategy.Assembler<S>)this.oidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends PersistenceTypeIdStrategy> PersistenceTypeIdStrategy.Assembler<S> lookupTypeIdStrategyAssembler(
			final Class<?> type
		)
		{
			@SuppressWarnings("unchecked") // cast safety is guaranteed by the registration logic
			final PersistenceTypeIdStrategy.Assembler<S> assembler =
				(PersistenceTypeIdStrategy.Assembler<S>)this.tidsAssemblers.get(type)
			;
			
			return assembler;
		}
		
		private <S extends PersistenceObjectIdStrategy> void assembleObjectIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final PersistenceObjectIdStrategy.Assembler<S> assembler = this.lookupObjectIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		private <S extends PersistenceTypeIdStrategy> void assembleTypeIdStrategy(
			final VarString vs        ,
			final S         idStrategy
		)
		{
			final PersistenceTypeIdStrategy.Assembler<S> assembler = this.lookupTypeIdStrategyAssembler(idStrategy.getClass());
			assembler.assembleIdStrategy(vs, idStrategy);
		}
		
		@Override
		public VarString assemble(final VarString vs, final PersistenceIdStrategy idStrategy)
		{
			vs
			.add(labelType()).add(typeAssigner()).add(quote()).apply(v ->
				this.assembleTypeIdStrategy(v, idStrategy.typeIdStragegy())
			).add(quote()).add(separator()).blank()
			.add(labelObject()).add(typeAssigner()).add(quote()).apply(v ->
				this.assembleObjectIdStrategy(v, idStrategy.objectIdStragegy())
			).add(quote());
			
			return vs;
		}

		@Override
		public PersistenceIdStrategy parse(final _charArrayRange input)
		{
			final XReference<String> tidsContent = X.Reference(null);
			final XReference<String> oidsContent = X.Reference(null);
			
			parseContent(input, tidsContent, oidsContent);
			
			final PersistenceTypeIdStrategy.Parser<?>   tidsParser = this.lookupTypeIdStrategyParser(tidsContent.get());
			final PersistenceObjectIdStrategy.Parser<?> oidsParser = this.lookupObjectIdStrategyParser(oidsContent.get());
			
			final PersistenceTypeIdStrategy   tidStrategy = tidsParser.parse(tidsContent.get());
			final PersistenceObjectIdStrategy oidStrategy = oidsParser.parse(oidsContent.get());
			
			return PersistenceIdStrategy.New(oidStrategy, tidStrategy);
		}
		
		private static void parseContent(
			final _charArrayRange    inputRange ,
			final XReference<String> tidsContent,
			final XReference<String> oidsContent
		)
		{
			final char[] input = inputRange.array();
			
			// the effective bounding index is the position of the last non-whitespace plus 1.
			final int iBound = XParsing.skipWhiteSpacesReversed(input, inputRange.start(), inputRange.bound()) + 1;
			
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
			
			throw new PersistenceException("IdStrategy type label \"" + label + "\" not found at index " + i + ".");
		}
		
		private PersistenceTypeIdStrategy.Parser<?> lookupTypeIdStrategyParser(final String content)
		{
			for(final KeyValue<String, PersistenceTypeIdStrategy.Parser<?>> e : this.tidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			throw new PersistenceException("Unknown TypeIdStrategy: \"" + content + "\".");
		}
		
		private PersistenceObjectIdStrategy.Parser<?> lookupObjectIdStrategyParser(final String content)
		{
			for(final KeyValue<String, PersistenceObjectIdStrategy.Parser<?>> e : this.oidsParsers)
			{
				if(content.startsWith(e.key()))
				{
					return e.value();
				}
			}
			
			throw new PersistenceException("Unknown ObjectIdStrategy: \"" + content + "\".");
		}
				
	}
		
}
