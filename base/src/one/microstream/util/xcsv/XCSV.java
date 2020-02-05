package one.microstream.util.xcsv;

import java.util.Arrays;

import one.microstream.X;
import one.microstream.chars.EscapeHandler;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XIterable;

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

	static final char             DEFAULT_LINE_SEPERATOR              = '\n';
	// the most reasonable control character for anyone who actually understands how it really works
	static final char             DEFAULT_SEPERATOR                   = '\t';
	static final char             DEFAULT_DELIMITER                   = '"' ;
	static final char             DEFAULT_ESCAPER                     = '\\';
	static final char             DEFAULT_SEGMENT_STARTER             = '{' ;
	static final char             DEFAULT_SEGMENT_TERMINATOR          = '}' ;
	static final char             DEFAULT_HEADER_STARTER              = '(' ;
	static final char             DEFAULT_HEADER_TERMINATOR           = ')' ;
	static final char             DEFAULT_COMMENT_SIGNAL              = '/' ;
	static final char             DEFAULT_COMMENT_SIMPLE_STARTER      = '/' ;
	static final char             DEFAULT_COMMENT_FULL_STARTER        = '*' ;
	static final String           DEFAULT_COMMENT_FULL_TERMINATOR     = "*/";
	static final char             DEFAULT_TERMINATOR                  = 0   ; // null character by default
	static final int              DEFAULT_SKIP_LINE_COUNT             = 0   ;
	static final int              DEFAULT_SKIP_LINE_COUNT_POST_HEADER = 0   ;
	static final int              DEFAULT_TRAILING_LINE_COUNT         = 0   ;
	static final EscapeHandler    DEFAULT_ESCAPE_HANDLER              = new EscapeHandler.Default();
	static final XCsvConfiguration DEFAULT_CONFIG = new XCsvConfiguration.Builder.Default().createConfiguration();
	
	static final char[] VALID_VALUE_SEPARATORS = X.chars('\t', ';', ',', '|', '-', '~', '#', ':');



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	// intentionally "get" since this is not a trivial accessor but performs considerable logic
	public char[] getValidValueSeparators()
	{
		return VALID_VALUE_SEPARATORS.clone();
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

