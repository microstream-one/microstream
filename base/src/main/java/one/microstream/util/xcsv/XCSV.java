package one.microstream.util.xcsv;

/*-
 * #%L
 * microstream-base
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

import java.nio.file.Path;
import java.util.Arrays;

import one.microstream.X;
import one.microstream.chars.EscapeHandler;
import one.microstream.chars.StringTable;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XIterable;
import one.microstream.exceptions.XCsvException;
import one.microstream.io.XIO;

/**
 * An extended CSV format ("XCSV") with the following traits:
 * <ul>
 * <li>Allows an arbitrary separator value (interpreting "CSV" as "character separated values" instead of
 * "comma separated values"), with a TAB ascii character as the default separator since that character
 * has been designed exactly for that purpose and is superior to any other character for that task.</li>
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
	static final Boolean       DEFAULT_HAS_COLUMN_NAMES_HEADER     = null;
	static final Boolean       DEFAULT_HAS_COLUMN_TYPES_HEADER     = null;
	static final Boolean       DEFAULT_HAS_CTRLCHAR_DEF_HEADER     = null;
	
	static final EscapeHandler DEFAULT_ESCAPE_HANDLER = new EscapeHandler.Default();
			
	
	// only the common ones. Crazy special needs must be handled explicitely
	static final char[] VALID_VALUE_SEPARATORS = {'\t', ';', ',', '|', ':'};
	
	
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

	

	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
				
	// intentionally "get" since this is not a trivial accessor but performs considerable logic
	public static final char[] getValidValueSeparators()
	{
		return VALID_VALUE_SEPARATORS.clone();
	}

	public static final XCsvConfiguration configurationDefault()
	{
		return XCsvDataType.XCSV.configuration();
	}

	public static final XCsvConfiguration.Builder ConfigurationBuilder()
	{
		return new XCsvConfiguration.Builder.Default();
	}

	public static final XCsvAssembler.Builder<VarString> AssemblerBuilder()
	{
		return XCsvAssembler.Builder();
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
		
		throw new XCsvException(
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

	
	public static StringTable parse(final String rawData)
	{
		return parse(rawData, null, null);
	}
	
	public static StringTable parse(final String rawData, final char valueSeparator)
	{
		return parse(rawData, XCsvConfiguration.New(valueSeparator), null);
	}
	
	public static StringTable parse(final String rawData, final XCsvDataType dataType)
	{
		return parse(rawData, null, dataType);
	}
	
	public static StringTable parse(final String rawData, final XCsvConfiguration configuration)
	{
		return parse(rawData, configuration, null);
	}
	
	public static StringTable parse(
		final String            rawData      ,
		final XCsvConfiguration configuration,
		final XCsvDataType      dataType
	)
	{
		return parse(_charArrayRange.New(XChars.readChars(rawData)), configuration, dataType);
	}
	
	public static String assembleString(final StringTable stringTable)
	{
		return assembleString(stringTable, null);
	}
	
	public static String assembleString(final StringTable stringTable, final XCsvConfiguration configuration)
	{
		final VarString vs = VarString.New(calculateEstimatedCharCount(stringTable.rows().size()));
		assembleString(vs, stringTable, configuration);
		
		return vs.toString();
	}

	public static StringTable parse(final _charArrayRange rawData)
	{
		return parse(rawData, null, null);
	}
		
	public static StringTable parse(final _charArrayRange rawData, final XCsvDataType dataType)
	{
		return parse(rawData, null, dataType);
	}
	
	public static StringTable parse(final _charArrayRange rawData, final char valueSeparator)
	{
		return parse(rawData, XCsvConfiguration.New(valueSeparator));
	}
	
	public static StringTable parse(
		final _charArrayRange   rawData         ,
		final XCsvConfiguration csvConfiguration
	)
	{
		return parse(rawData, csvConfiguration, null);
	}
			
	public static StringTable parse(
		final _charArrayRange   rawData         ,
		final XCsvConfiguration csvConfiguration,
		final XCsvDataType      dataType
	)
	{
		final XCsvContentBuilderCharArray parser = XCsvContentBuilderCharArray.New(
			csvConfiguration, dataType
		);
		
		final XCsvContent content = parser.build(null, rawData);
		final StringTable data    = content.segments().first().value();

		return data;
	}
	
	// float because float to int conversion is automatically capped at max int.
	public static final int estimatedCharCountPerRow()
	{
		return 100;
	}
	
	public static final int calculateEstimatedCharCount(final long rowCount)
	{
		final long estimate = rowCount * estimatedCharCountPerRow();
		
		return estimate >= Integer.MAX_VALUE
			? Integer.MAX_VALUE
			: (int)estimate
		;
	}

	public static final VarString assembleString(final VarString vs, final StringTable st)
	{
		return assembleString(vs, st, null);
	}
	
	public static final VarString assembleString(
		final VarString         vs              ,
		final StringTable       st              ,
		final XCsvConfiguration csvConfiguration
	)
	{
		if(st.columnNames().isEmpty())
		{
			// column names are mandatory. So no columns means no data, even if there should be rows present.
			return vs;
			
		}
		
		final XCsvConfiguration effConfig      = ensureCsvConfiguration(csvConfiguration);
		final char              valueSeparator = effConfig.valueSeparator();
		final char              lineSeparator  = effConfig.lineSeparator();
		final int               vsLength       = vs.length();
		
		if(X.isTrue(effConfig.hasControlCharacterDefinitionHeader()))
		{
			vs.add(effConfig.buildControlCharactersDefinition(';')).add(lineSeparator);
		}
		
		// assemble column names if not suppressed
		if(X.isNotFalse(effConfig.hasColumnNamesHeader()))
		{
			assemble(vs, valueSeparator, st.columnNames()).add(lineSeparator);
		}
		
		// assemble column types if present (and not suppressed)
		if(X.isNotFalse(effConfig.hasColumnTypesHeader()) && !st.columnTypes().isEmpty())
		{
			vs.add(effConfig.headerStarter());
			assemble(vs, valueSeparator, st.columnTypes());
			vs.add(effConfig.headerTerminator()).add(lineSeparator);
		}

		// assemble data rows if present
		if(!st.rows().isEmpty())
		{
			for(final String[] row : st.rows())
			{
				assemble(vs, valueSeparator, row);
				vs.add(lineSeparator);
			}
		}
		
		// any of the 4 elements adds a trailing lineSeparator at the end which must be deleted
		if(vs.length() != vsLength)
		{
			vs.deleteLast();
		}

		return vs;
	}
	
	private static void assemble(final VarString vs, final char separator, final String[] elements)
	{
		if(elements.length == 0)
		{
			return;
		}
		
		for(final String s : elements)
		{
			vs.add(s).add(separator);
		}
		vs.deleteLast();
	}
	
	private static VarString assemble(
		final VarString                  vs       ,
		final char                       separator,
		final XGettingCollection<String> elements
	)
	{
		if(elements.isEmpty())
		{
			return vs;
		}
		
		for(final String s : elements)
		{
			vs.add(s).add(separator);
		}
		vs.deleteLast();
		
		return vs;
	}
	
	// (08.05.2017 TM)NOTE: centralized method to guarantee parser and assembler behave consistently
	private static XCsvConfiguration ensureCsvConfiguration(final XCsvConfiguration csvConfiguration)
	{
		return csvConfiguration == null
			? XCSV.configurationDefault()
			: csvConfiguration
		;
	}
	
	
	public static StringTable readFromFile(final Path file)
	{
		final String       fileSuffix = XIO.getFileSuffix(file);
		final String       normalized = fileSuffix == null ? null : fileSuffix.trim().toLowerCase();
		final XCsvDataType dataType   = XCsvDataType.fromIdentifier(normalized);
		
		return readFromFile(file, dataType);
	}
	
	public static StringTable readFromFile(final Path file, final XCsvDataType dataType)
	{
		final String fileContent = XIO.unchecked(() ->
			XIO.readString(file)
		);
		
		return parse(fileContent, dataType);
	}
	
	public static StringTable readFromFile(final Path file, final char valueSeparator)
	{
		return readFromFile(file, XCsvConfiguration.New(valueSeparator));
	}
	
	public static StringTable readFromFile(final Path file, final XCsvConfiguration xcsvConfiguration)
	{
		final String fileContent = XIO.unchecked(() ->
			XIO.readString(file)
		);
		
		return parse(fileContent, xcsvConfiguration);
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private XCSV()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}

