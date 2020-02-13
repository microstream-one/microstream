package one.microstream.util.xcsv;

import java.nio.file.Path;
import java.util.Arrays;

import one.microstream.chars.EscapeHandler;
import one.microstream.chars.StringTable;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XIterable;
import one.microstream.io.XIO;

/**
 * An extended CSV format ("XCSV") with the following traits:
 * <ul>
 * <li>Allows an arbitrary separator value (interpreting "CSV" as "character separated values" instead of
 * "comma separated values"), with a TAB ascii character as the default separator since that character
 * has been designed exactely for that purpose and is superior to any other character for that task.</li>
 * <li>Optionally contains a header line defining all control characters</li>
 * <li>Contains an optional second header line defining/hinting the data type of the column</li>
 * <li>Allows single line and multi line comments</li>
 * <li>Allows multiple tables of different structure ("segments") in one file</li>
 * </ul>
 * In short: this is the ultimate textual data format for tabular data regarding efficiency and readability.
 */
public final class XCSV
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final char          DEFAULT_LINE_SEPERATOR              = '\n';
	// the most reasonable control character for anyone who actually understands how it really works
	static final char          DEFAULT_SEPERATOR                   = '\t';
	static final char          DEFAULT_DELIMITER                   = '"' ;
	static final char          DEFAULT_ESCAPER                     = '\\';
	static final char          DEFAULT_SEGMENT_STARTER             = '{' ;
	static final char          DEFAULT_SEGMENT_TERMINATOR          = '}' ;
	static final char          DEFAULT_HEADER_STARTER              = '(' ;
	static final char          DEFAULT_HEADER_TERMINATOR           = ')' ;
	static final char          DEFAULT_COMMENT_SIGNAL              = '/' ;
	static final char          DEFAULT_COMMENT_SIMPLE_STARTER      = '/' ;
	static final char          DEFAULT_COMMENT_FULL_STARTER        = '*' ;
	static final String        DEFAULT_COMMENT_FULL_TERMINATOR     = "*/";
	static final char          DEFAULT_TERMINATOR                  = 0   ; // null character by default
	static final int           DEFAULT_SKIP_LINE_COUNT             = 0   ;
	static final int           DEFAULT_SKIP_LINE_COUNT_POST_HEADER = 0   ;
	static final int           DEFAULT_TRAILING_LINE_COUNT         = 0   ;
	static final EscapeHandler DEFAULT_ESCAPE_HANDLER              = new EscapeHandler.Default();
	static final XCsvConfiguration DEFAULT_CONFIG = new XCsvConfiguration.Builder.Default().createConfiguration();
		
	
	static final char[] VALID_VALUE_SEPARATORS = createValidValueSeparators();
	
	
	public static ValueSeparatorWeight ValueSeparatorWeight(
		final char   valueSeparator,
		final double weight
	)
	{
		return new ValueSeparatorWeight.Default(
			validateValueSeparator(valueSeparator),
			(float)weight
		);
	}
	
	public interface ValueSeparatorWeight
	{
		public char valueSeparator();
		
		public float weight();
		
		
		
		public final class Default implements ValueSeparatorWeight
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final char  valueSeparator;
			private final float weight;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Default(final char valueSeparator, final float weight)
			{
				super();
				this.valueSeparator = valueSeparator;
				this.weight         = weight        ;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
			
			@Override
			public final char valueSeparator()
			{
				return this.valueSeparator;
			}
			
			@Override
			public final float weight()
			{
				return this.weight;
			}
			
		}
		
	}

	public enum DataType
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		XCSV(
			"xcsv",
			map(
				vc('\t', 1.3),
				vc( ';', 1.2),
				vc( ',', 1.1),
				vc( '|', 1.0),
				vc( '~', 0.9),
				vc( ':', 0.9),
				vc( '#', 0.9),
				vc( '*', 0.8),
				vc( '-', 0.8),
				vc( '.', 0.8)
			)
		),
		TSV(
			"tsv",
			map(
				vc('\t', 1.3),
				vc( ';', 1.2),
				vc( ',', 1.1),
				vc( '|', 1.0),
				vc( '~', 0.9),
				vc( ':', 0.9),
				vc( '#', 0.9),
				vc( '*', 0.8),
				vc( '-', 0.8),
				vc( '.', 0.8)
			)
		),
		CSV(
			"csv",
			map(
				vc('\t', 1.1),
				vc( ';', 1.2), // "," ist standard, see https://en.wikipedia.org/wiki/Comma-separated_values
				vc( ',', 1.3), // "," ist standard, see https://en.wikipedia.org/wiki/Comma-separated_values
				vc( '|', 1.0),
				vc( '~', 0.9),
				vc( ':', 0.9),
				vc( '#', 0.9),
				vc( '*', 0.8),
				vc( '-', 0.8),
				vc( '.', 0.8)
			)
		);
		
		
		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////
		
		public static DataType fromIdentifier(final String identifier)
		{
			if(identifier == null)
			{
				return null;
			}
			
			return DataType.valueOf(identifier.toUpperCase());
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final String identifier;
		private final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		private DataType(
			final String                                                 identifier           ,
			final EqConstHashTable<Character, XCSV.ValueSeparatorWeight> valueSeparatorWeights
		)
		{
			this.identifier            = identifier           ;
			this.valueSeparatorWeights = valueSeparatorWeights;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		public final String identifier()
		{
			return this.identifier;
		}
		
		public final EqConstHashTable<Character, ValueSeparatorWeight> valueSeparatorWeights()
		{
			return this.valueSeparatorWeights;
		}
		
		public final boolean isValidValueSeparator(final Character c)
		{
			return this.valueSeparatorWeights.keys().contains(c);
		}
		
		public final boolean isValidValueSeparator(final char c)
		{
			return this.isValidValueSeparator(Character.valueOf(c));
		}
		
		public final XCSV.ValueSeparatorWeight lookupValueSeparator(final Character c)
		{
			return this.valueSeparatorWeights.get(c);
		}
		
		public final XCSV.ValueSeparatorWeight lookupValueSeparator(final char c)
		{
			return this.lookupValueSeparator(Character.valueOf(c));
		}
		
	}

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	final static ValueSeparatorWeight vc(final char valueSeparator, final double weight)
	{
		return new ValueSeparatorWeight.Default(valueSeparator, (float)weight);
	}
	
	final static EqConstHashTable<Character, ValueSeparatorWeight> map(final ValueSeparatorWeight... weights)
	{
		final EqHashTable<Character, ValueSeparatorWeight> table = EqHashTable.New();
		
		for(final ValueSeparatorWeight weight : weights)
		{
			table.add(Character.valueOf(weight.valueSeparator()), weight);
		}
		
		return table.immure();
	}
	
	
	private static char[] createValidValueSeparators()
	{
		final XGettingSequence<ValueSeparatorWeight> weights = XCSV.DataType.XCSV.valueSeparatorWeights().values();
		final char[] vss = new char[weights.intSize()];
		
		int i = 0;
		for(final ValueSeparatorWeight weight : weights)
		{
			vss[i++] = weight.valueSeparator();
		}
		
		return vss;
	}
	
	public static String dataTypeCsv()
	{
		return "csv";
	}
	
	public static String dataTypeXCsv()
	{
		return "xcsv";
	}
	
	public static String dataTypeTsv()
	{
		return "tsv";
	}
	
	// intentionally "get" since this is not a trivial accessor but performs considerable logic
	public static final char[] getValidValueSeparators()
	{
		return VALID_VALUE_SEPARATORS;
	}

	public static final XCsvConfiguration configurationDefault()
	{
		return DEFAULT_CONFIG;
	}

	public static final XCsvConfiguration.Builder ConfigurationBuilder()
	{
		return new XCsvConfiguration.Builder.Default();
	}

	public static final XCsvAssembler.Builder<VarString> AssemblerBuilder()
	{
		return XCsvAssembler.Builder.Default.New();
	}
	
	public static boolean isValidValueSeparator(final char c)
	{
		return XChars.contains(VALID_VALUE_SEPARATORS, c);
	}
	
	public static char validateValueSeparator(final char c)
	{
		if(isValidValueSeparator(c))
		{
			return c;
		}
		
		// (05.02.2020 TM)EXCP: proper exceptions
		throw new RuntimeException(
			"Invalid " + XCSV.class.getSimpleName()
			+ " value separator '" + c + "'. Valid separators are "
			+ Arrays.toString(VALID_VALUE_SEPARATORS)
		);
	}

	public static final <T> void assembleRow(
		final XCsvAssembler          assembler   ,
		final XCsvRowAssembler<T>    rowAssembler,
		final XIterable<? extends T> row
	)
	{
		row.iterate(e ->
			rowAssembler.accept(e, assembler)
		);
		assembler.completeRow();
	}

	public static final <T> void assembleRows(
		final XCsvAssembler          assembler   ,
		final XCsvRowAssembler<T>    rowAssembler,
		final XIterable<? extends T> rows
	)
	{
		rows.iterate(e ->
		{
			rowAssembler.accept(e, assembler);
			assembler.completeRow();
		});
		assembler.completeRows();
	}

	
	public static StringTable readFromFile(final Path file)
	{
		final String        fileSuffix = XIO.getFileSuffix(file);
		final String        normalized = fileSuffix == null ? null : fileSuffix.trim().toLowerCase();
		final XCSV.DataType dataType   = XCSV.DataType.fromIdentifier(normalized);
		
		return readFromFile(file, dataType);
	}
	
	public static StringTable readFromFile(final Path file, final XCSV.DataType dataType)
	{
		// (19.04.2018 TM)EXCP: proper exception
		final String fileContent = XIO.unchecked(() ->
			XIO.readString(file)
		);
		
		final StringTable stringTable = StringTable.Static.parse(fileContent, dataType);
		
		return stringTable;
	}
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private XCSV()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}

