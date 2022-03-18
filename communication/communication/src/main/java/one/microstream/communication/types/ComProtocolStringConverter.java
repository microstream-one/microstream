package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
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

import static one.microstream.X.KeyValue;
import static one.microstream.X.notNull;

import java.nio.ByteOrder;

import one.microstream.chars.ObjectStringConverter;
import one.microstream.chars.VarString;
import one.microstream.chars.XParsing;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingTable;
import one.microstream.com.ComException;
import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceIdStrategyStringConverter;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryAssembler;
import one.microstream.persistence.types.PersistenceTypeDictionaryCompiler;
import one.microstream.typing.KeyValue;


/**
 * A "StringConverter" is hereby defined as a logic instance that handles both conversion to and from a String-form.
 */
public interface ComProtocolStringConverter extends ObjectStringConverter<ComProtocol>
{
	/* (15.11.2018 TM)TODO: ComProtocolStringConverter probably needs to become more flexible
	 * Currently, the assembler and parser handle the string structure in a strict format.
	 * No custom entries are allowed, not even a change in the entries' order.
	 * If customized parts need to transport information in the handshake, they are currently locked out.
	 * E.g.: a public key for encryption.
	 * As a conclusion, the protocol string must become a general purpose key-value content
	 * with optionally delimited keys and values, without trailing entry.
	 */
	
	@Override
	public VarString assemble(VarString vs, ComProtocol subject);
	
	@Override
	public default VarString provideAssemblyBuffer()
	{
		// including the whole type dictionary makes the string rather large.
		return VarString.New(10_000);
	}
	
	@Override
	public default String assemble(final ComProtocol subject)
	{
		return ObjectStringConverter.super.assemble(subject);
	}
	
	@Override
	public ComProtocol parse(_charArrayRange input);

	@Override
	public default ComProtocol parse(final String input)
	{
		return ObjectStringConverter.super.parse(input);
	}
	
	public static String defaultLabelVersion()
	{
		return "Version";
	}
	
	public static String defaultLabelByteOrder()
	{
		return "ByteOrder";
	}
	
	public static String defaultLabelIdStrategy()
	{
		return "IdStrategy";
	}
	
	public static String defaultLableInactivityTimeout()
	{
		return "InactivityTimeout";
	}
	
	public static PersistenceIdStrategyStringConverter defaultIdStrategyStringConverter()
	{
		// light-weight and easy collectable one-shot instance instead of permanent constant instance.
		return PersistenceIdStrategyStringConverter.New();
	}
	
	public static PersistenceTypeDictionaryAssembler defaultTypeDictionaryAssembler()
	{
		// light-weight and easy collectable one-shot instance instead of permanent constant instance.
		return PersistenceTypeDictionaryAssembler.New();
	}
	
	public static String defaultLabelTypeDictionary()
	{
		return "TypeDictionary";
	}
	
	public static char defaultProtocolItemSeparator()
	{
		return ';';
	}
	
	public static char defaultProtocolItemAssigner()
	{
		return ':';
	}
	
	public static char defaultProtocolItemDelimiter()
	{
		return '"';
	}
	
	
	public default String labelProtocolVersion()
	{
		return defaultLabelVersion();
	}
	
	public default String labelByteOrder()
	{
		return defaultLabelByteOrder();
	}
	
	public default String labelIdStrategy()
	{
		return defaultLabelIdStrategy();
	}
	
	public default String labelInactivityTimeout()
	{
		return defaultLableInactivityTimeout();
	}
	
	public default PersistenceIdStrategyStringConverter idStrategyStringConverter()
	{
		return defaultIdStrategyStringConverter();
	}
	
	public default String labelTypeDictionary()
	{
		return defaultLabelTypeDictionary();
	}
	
	public default PersistenceTypeDictionaryAssembler typeDictionaryAssembler()
	{
		return defaultTypeDictionaryAssembler();
	}
	
	public default char protocolItemSeparator()
	{
		return defaultProtocolItemSeparator();
	}
	
	public default char protocolItemAssigner()
	{
		return defaultProtocolItemAssigner();
	}
	
	public default char protocolItemDelimiter()
	{
		return defaultProtocolItemDelimiter();
	}
	
	
	
	public static ComProtocolStringConverter New(
		final PersistenceTypeDictionaryCompiler typeDictionaryCompiler
	)
	{
		return new ComProtocolStringConverter.Default(
			notNull(typeDictionaryCompiler)
		);
	}
	
	public final class Default implements ComProtocolStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		
		private final PersistenceTypeDictionaryCompiler typeDictionaryCompiler;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceTypeDictionaryCompiler typeDictionaryCompiler)
		{
			super();
			this.typeDictionaryCompiler = typeDictionaryCompiler;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vs, final ComProtocol protocol)
		{
			final char separator = this.protocolItemSeparator();
			
			this.assembleName             (vs, protocol).add(separator).lf();
			this.assembleVersion          (vs, protocol).add(separator).lf();
			this.assembleByteOrder        (vs, protocol).add(separator).lf();
			this.assembleInactivityTimeout(vs, protocol).add(separator).lf();
			this.assembleIdStrategy       (vs, protocol).add(separator).lf();
			this.assembleTypeDictionary   (vs, protocol);
			
			return vs;
		}
		
		// just a short-cut method for the lengthy proper one.
		private char assigner()
		{
			return this.protocolItemAssigner();
		}
		
		private char delimiter()
		{
			return this.protocolItemDelimiter();
		}
		
		private VarString assembleName(final VarString vs, final ComProtocol p)
		{
			return vs.add(p.name());
		}
		
		private VarString assembleVersion(final VarString vs, final ComProtocol p)
		{
			return vs
				.add(this.labelProtocolVersion())
				.add(this.assigner()).blank()
				.add(this.delimiter())
				.add(p.version())
				.add(this.delimiter())
			;
		}
		
		private VarString assembleByteOrder(final VarString vs, final ComProtocol p)
		{
			return vs
				.add(this.labelByteOrder())
				.add(this.assigner()).blank()
				.add(this.delimiter())
				.add(p.byteOrder())
				.add(this.delimiter())
			;
		}
		
		private VarString assembleInactivityTimeout(final VarString vs, final ComProtocol p)
		{
			return vs
					.add(this.labelInactivityTimeout())
					.add(this.assigner()).blank()
					.add(this.delimiter())
					.add(p.inactivityTimeout())
					.add(this.delimiter())
				;
		}
		
		private VarString assembleIdStrategy(final VarString vs, final ComProtocol p)
		{
			final PersistenceIdStrategyStringConverter idsc = this.idStrategyStringConverter();
			
			return vs
				.add(this.labelIdStrategy())
				.add(this.assigner()).blank()
				.add(this.delimiter())
				.apply(v ->
					idsc.assemble(v, p.idStrategy())
				)
				.add(this.delimiter())
			;
		}
		
		private VarString assembleTypeDictionary(final VarString vs, final ComProtocol protocol)
		{
			final PersistenceTypeDictionaryAssembler ptda = this.typeDictionaryAssembler();
			
			// Neither no delimeters nor separator, as the type dictionary string is intentionally trailing.
			return vs
				.add(this.labelTypeDictionary())
				.add(this.assigner()).lf()
				.apply(v ->
					ptda.assemble(v, protocol.typeDictionary())
				)
			;
		}
		
		
		
		@Override
		public ComProtocol parse(final _charArrayRange input)
		{
			final EqHashTable<String, String> contentTable = this.initializeContentTable();
			
			final String protocolName = ComProtocol.protocolName();
			parseContent(
				protocolName                ,
				contentTable                ,
				this.labelTypeDictionary()  ,
				this.protocolItemSeparator(),
				this.protocolItemAssigner() ,
				this.protocolItemDelimiter(),
				input
			);
			
			return this.createProtocol(protocolName, contentTable);
		}
		
		private ComProtocol createProtocol(
			final String                        protocolName,
			final XGettingTable<String, String> content
		)
		{
			final String            version           = content.get(this.labelProtocolVersion());
			final ByteOrder         byteOrder         = this.parseByteOrder(content.get(this.labelByteOrder()));
			final int               inactivityTimeout = this.parseInteger(content.get(this.labelInactivityTimeout()));
			final PersistenceIdStrategy idStrategy    = this.parseIdStrategy(content.get(this.labelIdStrategy()));
			final PersistenceTypeDictionary typeDict  = this.parseTypeDictionary(content.get(this.labelTypeDictionary()));
						
			return ComProtocol.New(protocolName, version, byteOrder, inactivityTimeout, idStrategy, typeDict.view());
		}
		
		private ByteOrder parseByteOrder(final String input)
		{
			return XMemory.parseByteOrder(input);
		}
		
		private int parseInteger(final String input)
		{
			return Integer.valueOf(input);
		}
		
		private PersistenceIdStrategy parseIdStrategy(final String input)
		{
			final PersistenceIdStrategyStringConverter idsc = this.idStrategyStringConverter();
			
			return idsc.parse(input);
		}
		
		private PersistenceTypeDictionary parseTypeDictionary(final String input)
		{
			final PersistenceTypeDictionary td = this.typeDictionaryCompiler.compileTypeDictionary(input);

			return td;
		}
		
		private EqHashTable<String, String> initializeContentTable()
		{
			return EqHashTable.New(
				KeyValue(this.labelProtocolVersion()   , null),
				KeyValue(this.labelByteOrder()         , null),
				KeyValue(this.labelInactivityTimeout() , null),
				KeyValue(this.labelIdStrategy()        , null)
			);
		}

		private static void parseContent(
			final String                      protocolName      ,
			final EqHashTable<String, String> content           ,
			final String                      trailingEntryLabel,
			final char                        separator         ,
			final char                        assigner          ,
			final char                        delimiter         ,
			final _charArrayRange             inputRange
		)
		{
			final char[] input  = inputRange.array();
			final int    iStart = inputRange.start();
			final int    iBound = XParsing.skipWhiteSpacesReversed(input, iStart, inputRange.bound()) + 1;

			int i = iStart;
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			i = XParsing.checkStartsWith(input, i, iBound, protocolName, "Protocol name");
			i = parseContentEntries(content, separator, assigner, delimiter, input, i, iBound);
			
			parseTrailingEntry(trailingEntryLabel, content, assigner, input, i, iBound);
		}
		
		private static int parseContentEntries(
			final EqHashTable<String, String> entries  ,
			final char                        separator,
			final char                        assigner ,
			final char                        delimiter,
			final char[]                      input    ,
			final int                         iStart   ,
			final int                         iBound
		)
		{
			int i = iStart;
			for(final KeyValue<String, String> entry : entries)
			{
				final String key = entry.key();
				
				try
				{
					i = skipToEntryValue(key, separator, assigner, input, i, iBound);
					
					XParsing.checkIncompleteInput(i, iBound, key);
					i = XParsing.checkCharacter(input, i, delimiter, key);
					final int valueEndBound = XParsing.skipToSimpleTerminator(input, i, iBound, delimiter);
					entries.set(key, new String(input, i, valueEndBound - i - 1));
					i = valueEndBound;
				}
				catch(final RuntimeException e)
				{
					throw new ComException("Invalid entry '" + entry.key() + "' at index " + i, e);
				}
			}
			
			// skip last entry's separator
			i = skipControlCharacter(input, i, iBound, separator);
			
			return i;
		}
		
		private static void parseTrailingEntry(
			final String                      label   ,
			final EqHashTable<String, String> content ,
			final char                        assigner,
			final char[]                      input   ,
			final int                         iStart  ,
			final int                         iBound
		)
		{
			
			int i = iStart;
			i = XParsing.checkStartsWith(input, i, iBound, label);
			
			i = skipControlCharacter(input, i, iBound, assigner);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			addTrailingValue(label, content, input, i, iBound);
		}
		
		private static void addTrailingValue(
			final String                      label  ,
			final EqHashTable<String, String> content,
			final char[]                      input  ,
			final int                         iStart ,
			final int                         iBound
		)
		{
			XParsing.checkIncompleteInput(iStart, iBound);
			final String trailingValue = new String(input, iStart, iBound - iStart);
			if(!content.add(label, trailingValue))
			{
				throw new ComException("Duplicate entry '" + label + "'.");
			}
		}
		
		private static int skipToEntryValue(
			final String entryLabel,
			final char   separator ,
			final char   assigner  ,
			final char[] input     ,
			final int    iStart    ,
			final int    iBound
		)
		{
			int i = iStart;
			
			i = skipControlCharacter(input, i, iBound, separator);
			
			XParsing.checkIncompleteInput(i, iBound);
			i = XParsing.checkStartsWith (input, i, iBound, entryLabel);
			
			i = skipControlCharacter(input, i, iBound, assigner);
			
			return i;
		}
		
		private static int skipControlCharacter(
			final char[] input ,
			final int    iStart,
			final int    iBound,
			final char   c
		)
		{
			int i = iStart;

			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			XParsing.checkIncompleteInput(i, iBound);
			i = XParsing.checkCharacter (input, i, c);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			return i;
		}
		
	}
	
}
