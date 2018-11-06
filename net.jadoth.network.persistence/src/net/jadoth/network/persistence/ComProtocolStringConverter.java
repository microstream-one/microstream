package net.jadoth.network.persistence;

import static net.jadoth.X.KeyValue;

import java.nio.ByteOrder;

import net.jadoth.chars.ObjectStringConverter;
import net.jadoth.chars.VarString;
import net.jadoth.chars.XParsing;
import net.jadoth.chars._charArrayRange;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.low.XVM;
import net.jadoth.meta.XDebug;
import net.jadoth.persistence.types.PersistenceTypeDictionaryAssembler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.swizzling.types.SwizzleIdStrategy;
import net.jadoth.swizzling.types.SwizzleIdStrategyStringConverter;
import net.jadoth.typing.KeyValue;


/**
 * A "StringConverter" is hereby defined as a logic instance that handles both conversion to and from a String-form.
 * 
 * @author TM
 *
 */
public interface ComProtocolStringConverter extends ObjectStringConverter<ComProtocol>
{
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
	
	public static SwizzleIdStrategyStringConverter defaultIdStrategyStringConverter()
	{
		// light-weight and easy collectable one-shot instance instead of permanent constant instance.
		return SwizzleIdStrategyStringConverter.New();
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
	
	public default SwizzleIdStrategyStringConverter idStrategyStringConverter()
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
	
	
	
	public static ComProtocolStringConverter New()
	{
		return new ComProtocolStringConverter.Implementation();
	}
	
	public final class Implementation implements ComProtocolStringConverter
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public VarString assemble(final VarString vs, final ComProtocol protocol)
		{
			final char separator = this.protocolItemSeparator();
			
			// (06.11.2018 TM)FIXME: JET-43 assemble quoted
			
			this.assembleName          (vs, protocol).add(separator).lf();
			this.assembleVersion       (vs, protocol).add(separator).lf();
			this.assembleByteOrder     (vs, protocol).add(separator).lf();
			this.assembleIdStrategy    (vs, protocol).add(separator).lf();
			this.assembleTypeDictionary(vs, protocol);
			
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
		
		private VarString assembleIdStrategy(final VarString vs, final ComProtocol p)
		{
			final SwizzleIdStrategyStringConverter idsc = this.idStrategyStringConverter();
			
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
				input.array()               ,
				input.start()               ,
				input.bound()
			);
			
			return this.createProtocol(protocolName, contentTable);
		}
		
		private ComProtocol createProtocol(
			final String                        protocolName,
			final XGettingTable<String, String> content
		)
		{
			// (06.11.2018 TM)FIXME: /!\ DEBUG
			content.iterate(e -> XDebug.println(e.key() + " -> " + e.value()));
			
			final String                        version    = content.get(this.labelProtocolVersion());
			final ByteOrder                     byteOrder  = this.parseByteOrder(content.get(this.labelByteOrder()));
			final SwizzleIdStrategy             idStrategy = this.parseIdStrategy(content.get(this.labelIdStrategy()));
			final PersistenceTypeDictionaryView typeDict   = this.parseTypeDictionary(content.get(this.labelTypeDictionary()));
			
			return ComProtocol.New(protocolName, version, byteOrder, idStrategy, typeDict);
		}
		
		private ByteOrder parseByteOrder(final String input)
		{
			return XVM.lookupByteOrder(input);
		}
		
		private SwizzleIdStrategy parseIdStrategy(final String input)
		{
			final SwizzleIdStrategyStringConverter idsc = this.idStrategyStringConverter();
			
			return idsc.parse(input);
		}
		
		private PersistenceTypeDictionaryView parseTypeDictionary(final String input)
		{
			// FIXME ComProtocolStringConverter.Implementation#parseTypeDictionary()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		private EqHashTable<String, String> initializeContentTable()
		{
			return EqHashTable.New(
				KeyValue(this.labelProtocolVersion(), null),
				KeyValue(this.labelByteOrder()      , null),
				KeyValue(this.labelIdStrategy()     , null)
				// type dictionary is a special trailing entry
//				KeyValue(this.labelTypeDictionary() , null)
			);
		}

		private static void parseContent(
			final String                      protocolName      ,
			final EqHashTable<String, String> contentTable      ,
			final String                      trailingEntryLabel,
			final char                        separator         ,
			final char                        assigner          ,
			final char[]                      input             ,
			final int                         iStart            ,
			final int                         iBound
		)
		{
			final int iBoundEffective = XParsing.skipWhiteSpacesReverse(input, iStart, iBound);
			final int iStartEffective = XParsing.skipWhiteSpaces(input, iStart, iBoundEffective);
			
			if(!XParsing.startsWith(input, iStartEffective, iBoundEffective, protocolName))
			{
				// (04.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Protocol name '" + protocolName + "' not found at index " + iStartEffective);
			}
			
			final int trailingEntryIndex = parseContentEntries(
				contentTable                           ,
				separator                              ,
				assigner                               ,
				input                                  ,
				iStartEffective + protocolName.length(),
				iBoundEffective
			);
			
			if(trailingEntryIndex == iBound)
			{
				// (04.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Trailing entry '" + trailingEntryLabel + "' not found.");
			}
			if(!XParsing.startsWith(input, trailingEntryIndex, iBound, trailingEntryLabel))
			{
				// (04.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Trailing entry is not '" + trailingEntryLabel + "'.");
			}
			final int trailingValueIndex = skipControlCharacter(assigner, input, trailingEntryIndex, iBound);
			
			final String trailingValue = new String(input, trailingValueIndex, iBound);
			if(!contentTable.add(trailingEntryLabel, trailingValue))
			{
				// (04.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Duplicate entry '" + trailingEntryLabel + "'.");
			}
		}
		
		private static int parseContentEntries(
			final EqHashTable<String, String> entries  ,
			final char                        separator,
			final char                        assigner ,
			final char[]                      input    ,
			final int                         iStart   ,
			final int                         iBound
		)
		{
			// (05.11.2018 TM)FIXME: JET-43: handle premature end (iBound reached)
			
			int i = iStart;
			for(final KeyValue<String, String> entry : entries)
			{
				try
				{
					i = skipToEntryValue(entry.value(), separator, assigner, input, i, iBound);
					final int valueEndBound = skipValue(separator, input, i, iBound);
					entries.set(entry.key(), new String(input, i, valueEndBound));
					i = valueEndBound;
				}
				catch(final RuntimeException e)
				{
					// (04.11.2018 TM)EXCP: proper exception
					throw new RuntimeException("Invalid entry '" + entry.key() + "' at index " + i, e);
				}
			}
			
			// skip last entry's separator
			i = skipControlCharacter(separator, input, i, iBound);
			
			return i;
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
			i = skipControlCharacter(separator, input, i, iBound);
			if(!XParsing.startsWith(input, i, iBound, entryLabel))
			{
				// (04.11.2018 TM)EXCP: proper exception
				throw new RuntimeException("Entry label '" + entryLabel + "' not found at index " + i);
			}
			i = skipControlCharacter(assigner, input, i, iBound);
			
			return i;
		}
		
		private static int skipControlCharacter(final char separator, final char[] input, final int iStart, final int iBound)
		{
			int i = iStart;

			i = XParsing.skipWhiteSpaces(input, i, iBound);
			i = XParsing.checkCharacter(input, i, separator);
			i = XParsing.skipWhiteSpaces(input, i, iBound);
			
			return i;
		}

		private static int skipValue(final char valueTerminator, final char[] input, final int iStart, final int iBound)
		{
			// (04.11.2018 TM)TODO: JET-43: support quoted values (XChars.skipSimpleQuote)
			int i = iStart;
			while(i < iBound && input[i] > ' ' && input[i] != valueTerminator)
			{
				i++;
			}
			
			return i;
		}
		
	}
	
}
