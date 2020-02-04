package one.microstream.chars;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.types.XReference;
import one.microstream.functional._charRangeProcedure;
import one.microstream.typing.Stateless;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvConfiguration;
import one.microstream.util.xcsv.XCsvParser;
import one.microstream.util.xcsv.XCsvRecordParserCharArray;
import one.microstream.util.xcsv.XCsvRowCollector;
import one.microstream.util.xcsv.XCsvSegmentsParser;


/**
 * Reference implementation for the XCSV file format based on a {@code char[]} input.
 *
 * @author Thomas Muenz
 */
public final class XCsvParserCharArray implements XCsvParser<_charArrayRange>, Stateless
{
	/* Note on implementation:
	 * This implementation might seem a bit too procedural and cumbersome.
	 * The rationale behind this implementation is that is is several times faster (x8 or more) than a comparable
	 * "neat" object oriented implementation (most static parameters transformed to fields).
	 * The comparison was done on a 1.7.0_25 jvm with server mode, ensured escape analysis, enough heap to
	 * neglect GC pauses (checked), etc.
	 * One should assume that passing around a dozend of parameters everywhere would be slower than
	 * intelligent field accesses before and after loops, but it is obviously the other way around.
	 * Probably due to aggressive inlining and optimization of static methods
	 *
	 * Note on performance (state of 2014-01-02, pure parsing without IO / value collection / collection copying / etc.)
	 * ~4 million records with 7 columns and a total of ~240 MB took around 600 ms to parse on a several year old
	 * private PC. Hence any data ranging below millions can be assumed to be parsed "instantaneously" in productive
	 * use. The rest is up to the layers below (IO) and above (value collection).
	 *
	 * Conclusion:
	 * Admitted it's relatively ugly, but it's also incredibly fast.
	 */



	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int META_INDEX_LITERAL_DELIMITER            =  0;
	private static final int META_INDEX_VALUE_SEPARATOR              =  1;
	private static final int META_INDEX_RECORD_SEPARATOR             =  2;
	private static final int META_INDEX_SEGMENT_STARTER              =  3;
	private static final int META_INDEX_SEGMENT_TERMINATOR           =  4;
	private static final int META_INDEX_COLUMN_DEFINITION_STARTER    =  5;
	private static final int META_INDEX_COLUMN_DEFINITION_TERMINATOR =  6;
	private static final int META_INDEX_COMMENT_SIGNAL               =  7;
	private static final int META_INDEX_SIMPLE_COMMENT_STARTER       =  8;
	private static final int META_INDEX_FULL_COMMENT_STARTER         =  9;
	private static final int META_INDEX_FULL_COMMENT_TERMINATOR      = 10;
	private static final int META_COUNT                              = 11;

	private static final int META_INDEX_COMPLETE_BASIC    =  2; // first 3 symbols read
	private static final int META_INDEX_COMPLETE_ENHANCED =  6; // first 7 symbols read
	private static final int META_INDEX_COMPLETE_FULL     = 10; // all  11 symbols read



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static final void noOp(final char[] data, final int offset, final int length)
	{
		// no-op dummy implementation
	}

	private static int skipLines(
		final char[] input        ,
		final int    iStart       ,
		final int    iBound       ,
		final char   lineSeparator,
		final int    lineCount
	)
	{
		int i = iStart;
		for(int c = 0; c < lineCount; c++)
		{
			while(input[i] != lineSeparator)
			{
				if(++i == iBound)
				{
					return i;
				}
			}
			i++;
		}
		return i;
	}

	private static int skipLinesBackwards(
		final char[] input        ,
		final int    iLowBound    ,
		final int    iHighBound   ,
		final char   lineSeparator,
		final int    lineCount
	)
	{
		int i = iHighBound;
		for(int c = 0; c < lineCount; c++)
		{
			while(input[--i] != lineSeparator)
			{
				if(i == iLowBound)
				{
					return i;
				}
			}
			i--; // skip line separator
		}
		return i;
	}

	private static int skipValueSeparator(
		final char[] input          ,
		final int    iStart         ,
		final int    iBound         ,
		final char   valueSeparator ,
		final char   recordSeparator,
		final char   terminator
	)
	{
		for(int i = iStart; i < iBound; i++)
		{
			if(input[i] == valueSeparator)
			{
				return i + 1; // skip value separator
			}
			if(XChars.isNonWhitespace(input[i]) || input[i] == recordSeparator || input[i] == terminator)
			{
				return i;
			}
		}
		return iBound;
	}

	private static int parseLiteralSimple(
		final char[]              input          ,
		final int                 iStart         ,
		final int                 iBound         ,
		final char                recordSeparator,
		final char                valueSeparator ,
		final char                terminator     ,
		final _charRangeProcedure valueCollector
	)
	{
		int i = iStart;

		// scroll to end of simple literal

		// (28.11.2013 TM)NOTE: improved to recognize enclosed non-control whitespaces. Should make no problems, could it?
		while(i < iBound && input[i] != valueSeparator && input[i] != recordSeparator && input[i] != terminator)
		{
			i++;
		}
		int lastLiteralIndex = i - 1;
		while(XChars.isWhitespace(input[lastLiteralIndex]))
		{
			lastLiteralIndex--;
		}

		// end reached (either end of parsing range or separator or record end or whitespace), so add and report back
		if(valueCollector != null) // cannot check sooner as header parsing cannot be circumvented (see before)
		{
			valueCollector.accept(input, iStart, lastLiteralIndex + 1 - iStart);
		}

		// ensure that this literal's separator is skipped (accounting for white spaces, record ends, index bound)
		return skipValueSeparator(input, i, iBound, valueSeparator, recordSeparator, terminator);
	}

	private static int parseLiteralDelimited(
		final char[]              input          ,
		final int                 iStart         ,
		final int                 iBound         ,
		final char                delimiter      ,
		final char                escaper        ,
		final VarString           literalBuilder ,
		final EscapeHandler       escapeHandler  ,
		final _charRangeProcedure valueCollector
	)
	{
		int i = iStart;
		literalBuilder.clear();

		// scroll to end of delimited literal
		while(++i < iBound) // preincrement to skip the opening delimiter right away
		{
			// handle escaping
			if(input[i] == escaper)
			{
				if(i + 1 == iBound)
				{
					// damn special cases
					break;
				}
				escapeHandler.handleEscapedChar(input[++i], literalBuilder);
				continue;
			}

			// check for (unescaped) literal delimiter (literal end)
			if(input[i] == delimiter)
			{
				// found end of literal. Increment index but do NOT add closing delimiter to the literal
				i++;
				break;
			}

			// normal case: add ordinary character
			literalBuilder.add(input[i]);
		}

		// add completely collected delimited literal without delimiters
		if(valueCollector != null)
		{
			// cannot check sooner as header parsing cannot be circumvented (see before)
			valueCollector.accept(literalBuilder.data, 0, literalBuilder.size);
		}

		return i;
	}

	// inlineable static method with a lot of parameters for performance reasons (tested!)
	static final int parseRecord(
		final char[]              input          ,
		final int                 iStart         ,
		final int                 iBound         ,
		final char                valueSeparator ,
		final char                delimiter      ,
		final char                escaper        ,
		final char                recordSeparator,
		final char                terminator     ,
		final XCsvConfiguration    config         ,
		final VarString           literalBuilder ,
		final EscapeHandler       escapeHandler  ,
		final _charRangeProcedure valueCollector
	)
	{
		int i = iStart;
		while(true)
		{
			if(i == iBound || input[i] == recordSeparator || input[i] == terminator)
			{
				// input-trailing missing value special case
				if(XCsvRecordParserCharArray.Static.isTrailingSeparator(input, iStart, i, valueSeparator))
				{
					// interpret as null
					valueCollector.accept(null, 0, 0);
				}
				return i;
			}
			if(input[i] == valueSeparator)
			{
				// encountered the next separator while searching for a literal, interpret as null value
				valueCollector.accept(null, 0, 0);
				i++; // skip separator
			}
			else if(XChars.isWhitespace(input[i]))
			{
				i++; // skip unimportant whitespace
			}
			else if(input[i] == delimiter)
			{
				// encountered an opening delimiter, parse accordingly
				i = parseLiteralDelimited(input, i, iBound, delimiter, escaper, literalBuilder, escapeHandler, valueCollector);
				i = skipValueSeparator(input, i, iBound, valueSeparator, recordSeparator, terminator);
			}
			else
			{
				// default case: parse encountered non-whitespace non-specials chars as simple literal
				i = parseLiteralSimple(input, i, iBound, recordSeparator, valueSeparator, terminator, valueCollector);
			}
		}
	}

	private static int parseSimpleColumnLine(
		final char[]                input              ,
		final int                   iStart             ,
		final int                   iBound             ,
		final char                  recordSeparator    ,
		final char                  terminator         ,
		final XCsvConfiguration      config             ,
		final VarString             literalBuilder     ,
		final ColumnHeaderCollector columnNameCollector
	)
	{
		/* note: cannot just skip header line here if column name collector is null
		 * as a column name can potentially be a delimited literal containing a line separator.
		 * Instead, header must be parsed anyway and colum names must simply be discarded.
		 */

		// parse line using column name meta characters (delimiter etc.)
		return parseRecord(
			input                    ,
			iStart                   ,
			iBound                   ,
			config.valueSeparator()  ,
			config.literalDelimiter(),
			config.escaper()         ,
			recordSeparator          ,
			terminator               ,
			config                   ,
			literalBuilder           ,
			config.escapeHandler()   ,
			columnNameCollector
		);
	}

	private static int parseMetaCharacter(
		final char[]        input           ,
		final int           iStart          ,
		final int           iBound          ,
		final char          metaSeparator   ,
		final char          metaEscaper     ,
		final String[]      metaCharacters  ,
		final int           currentMetaIndex,
		final EscapeHandler escapeHandler
	)
	{
		int i = iStart;

		if(i == iBound || input[i] != metaSeparator || ++i == iBound)
		{
			return iStart;
		}

		if(input[i] == metaEscaper)
		{
			if(++i == iBound || input[i] == metaSeparator || input[i] == metaEscaper)
			{
				return iStart;
			}
			metaCharacters[currentMetaIndex] = String.valueOf(escapeHandler.unescape(input[i]));
		}
		else
		{
			metaCharacters[currentMetaIndex] = String.valueOf(input[i]);
		}

		return ++i;
	}

	private static int parseFullCommentTerminator(
		final char[]        input           ,
		final int           iStart          ,
		final int           iBound          ,
		final char          metaSeparator   ,
		final String[]      metaCharacters  ,
		final int           currentMetaIndex
	)
	{
		int i = iStart;
		if(i == iBound || input[i] != metaSeparator)
		{
			return iStart;
		}

		while(true)
		{
			if(++i == iBound)
			{
				return iStart;
			}
			if(XChars.isWhitespace(input[i]))
			{
				break;
			}
		}
		metaCharacters[currentMetaIndex] = String.valueOf(input, iStart + 1, i - iStart - 1);

		return i;
	}

	private static void updateConfig(
		final XReference<XCsvConfiguration> config      ,
		final int                          symbolIndex,
		final String[]                     symbols
	)
	{
		final XCsvConfiguration.Builder builder = new XCsvConfiguration.Builder.Default().copyFrom(config.get());

		// check for full meta characters set
		if(symbolIndex >= META_INDEX_COMPLETE_FULL)
		{
			builder
			.setCommentSignal        (symbols[META_INDEX_COMMENT_SIGNAL         ].charAt(0))
			.setCommentSimpleStarter (symbols[META_INDEX_SIMPLE_COMMENT_STARTER ].charAt(0))
			.setCommentFullStarter   (symbols[META_INDEX_FULL_COMMENT_STARTER   ].charAt(0))
			.setCommentFullTerminator(symbols[META_INDEX_FULL_COMMENT_TERMINATOR])
			;
		}

		// check for enhanced meta characters set
		if(symbolIndex >= META_INDEX_COMPLETE_ENHANCED)
		{
			builder
			.setSegmentStarter         (symbols[META_INDEX_SEGMENT_STARTER             ].charAt(0))
			.setSegmentTerminator      (symbols[META_INDEX_SEGMENT_TERMINATOR          ].charAt(0))
			.setHeaderStarter          (symbols[META_INDEX_COLUMN_DEFINITION_STARTER   ].charAt(0))
			.setHeaderTerminator       (symbols[META_INDEX_COLUMN_DEFINITION_TERMINATOR].charAt(0))
			;
		}

		// basic meta characters set is guaranteed due to outside check
		builder
		.setLiteralDelimiter(symbols[META_INDEX_LITERAL_DELIMITER].charAt(0))
		.setValueSeparator  (symbols[META_INDEX_VALUE_SEPARATOR  ].charAt(0))
		.setRecordSeparator (symbols[META_INDEX_RECORD_SEPARATOR ].charAt(0))
		;

		config.set(builder.createConfiguration());
	}

	private static boolean isValidSymbols(final String[] metaChars)
	{
		final int bound;
		boundInitializer:
		{
			for(int i = 0; i <= META_INDEX_COMPLETE_ENHANCED; i++)
			{
				if(metaChars[i] == null)
				{
					bound = i;
					break boundInitializer;
				}
			}
			bound = META_INDEX_COMPLETE_ENHANCED + 1;
		}

		/* all symbols except comment symbols must be unique among each other
		 * (inefficient algorithm but for that small amount still faster than a hashset instance)
		 */
		for(int i = 0; i < bound; i++)
		{
			final String current = metaChars[i];
			for(int j = i + 1; j < bound; j++)
			{
				if(current.equals(metaChars[j]))
				{
					return false; // duplicate found, uniqueness constraint broken
				}
			}
		}

		// uniqueness constraint satisfied
		return true;
	}

	private static int checkMetaCharacters(
		final char[]                       input ,
		final int                          iStart,
		final int                          iBound,
		final XReference<XCsvConfiguration> config
	)
	{
		if(iStart == iBound)
		{
			return iStart;
		}
		int i = iStart;

		// keep assumed meta separator
		final char assumedMetaSeparator = input[i];
		if(++i == iBound)
		{
			return i;
		}

		// keep assumed escape character
		final char assumedEscaper = input[i];
		if(assumedMetaSeparator == assumedEscaper)
		{
			return iStart; // can't be a (valid) meta symbol definition, so return
		}

		int j = ++i, c = 0;
		final EscapeHandler escapeHandler = config.get().escapeHandler();
		final String[] metaChars = new String[META_COUNT];

		while(c < META_INDEX_FULL_COMMENT_TERMINATOR)
		{
			i = parseMetaCharacter(input, i, iBound, assumedMetaSeparator, assumedEscaper, metaChars, c, escapeHandler);
			if(i == j)
			{
				break;
			}
			j = i;
			c++;
		}
		if(c == META_INDEX_FULL_COMMENT_TERMINATOR)
		{
			i = parseFullCommentTerminator(input, i, iBound, assumedMetaSeparator, metaChars, c);
		}
		if(c < META_INDEX_COMPLETE_BASIC)
		{
			return iStart; // if not even basic part has been parsed completely, then discard all
		}
		if(!isValidSymbols(metaChars))
		{
			throw new RuntimeException("Inconsistent meta characters: " + String.valueOf(input, iStart, i - iStart));
		}

		updateConfig(config, c, metaChars);
		return i;
	}

	static final boolean isSegmentStart(
		final char[]           input         ,
		final int              iStart        ,
		final int              iBound        ,
		final VarString        literalBuilder,
		final XCsvConfiguration config
	)
	{
		// Redundant parsing here. However only once per file, so it's acceptable
		return parseSegmentStart(input, iStart, iBound, literalBuilder, config) != iStart;
	}

	static final int parseSegmentStart(
		final char[]           input         ,
		final int              iStart        ,
		final int              iBound        ,
		final VarString        literalBuilder,
		final XCsvConfiguration config
	)
	{
		if(iStart >= iBound)
		{
			return iStart;
		}

		/* segment start is one optionally delimited optional identifier followed by any number of skippables
		 * and a segment starter character.
		 * Meaning two non-skippable non-segment-starter symbols in a row cannot be a segment start
		 */
		if(input[iStart] == config.segmentStarter())
		{
			return XCsvRecordParserCharArray.Static.skipSkippable(
				input, iStart + 1, iBound, config.commentSignal(), config
			);
		}
		int i = iStart;
		if(input[i] == config.literalDelimiter())
		{
			i = parseLiteralDelimited(
				input                    ,
				i                        ,
				iBound                   ,
				config.literalDelimiter(),
				config.escaper()         ,
				literalBuilder           ,
				config.escapeHandler()   ,
				XCsvParserCharArray::noOp
			);
			i = XCsvRecordParserCharArray.Static.skipSkippable(input, i, iBound, config.commentSignal(), config);
			if(input[i] != config.segmentStarter())
			{
				literalBuilder.clear();
			}
		}
		else
		{
			literalBuilder.clear();
			final char segmentStarter = config.segmentStarter();
			while(i < iBound && XChars.isNonWhitespace(input[i]) && input[i] != segmentStarter)
			{
				i++;
			}
			final int simpleLiteralEnd = i;
			i = XCsvRecordParserCharArray.Static.skipSkippable(input, i, iBound, config.commentSignal(), config);

			if(i == iBound)
			{
				// end of input reached before a segment starter can be found (e.g. file with header but no rows)
				return iStart;
			}

			if(input[i] == segmentStarter)
			{
				literalBuilder.add(input, iStart, simpleLiteralEnd - iStart);
			}
		}
		return input[i] == config.segmentStarter()
			? XCsvRecordParserCharArray.Static.skipSkippable(input, i + 1, iBound, config.commentSignal(), config)
			: iStart
		;
	}

	public static final void parseSegments(
		final char[]                            input               ,
		final int                               iStart              ,
		final int                               iBound              ,
		final VarString                         literalBuilder      ,
		final XCsvConfiguration                  config              ,
		final XCsvRowCollector                   rowAggregator       ,
		final XCsvRecordParserCharArray.Provider recordParserProvider
	)
	{
		if(isSegmentStart(input, iStart, iBound, literalBuilder, config))
		{
			parseMultipleSegments(input, iStart, iBound, literalBuilder, config, rowAggregator, recordParserProvider);
		}
		else
		{
			parseSingletonSegment(input, iStart, iBound, literalBuilder, config, rowAggregator, recordParserProvider);
		}
	}

	static final void parseMultipleSegments(
		final char[]                            input               ,
		final int                               iStart              ,
		final int                               iBound              ,
		final VarString                         literalBuilder      ,
		final XCsvConfiguration                  config              ,
		final XCsvRowCollector                   rowAggregator       ,
		final XCsvRecordParserCharArray.Provider recordParserProvider
	)
	{
		final ColumnHeaderCollector columnNames       = new ColumnHeaderCollector(new BulkList<>());
		final ColumnHeaderCollector columnTypes       = new ColumnHeaderCollector(new BulkList<>());
		final char                  segmentTerminator = config.segmentTerminator()                 ;
		final char                  recordSeparator   = config.recordSeparator()                   ;
		final char                  valueSeparator    = config.valueSeparator()                    ;
		final char                  commentSignal     = config.commentSignal()                     ;
		final char                  literalDelimiter  = config.literalDelimiter()                  ;
		final char                  escaper           = config.escaper()                           ;
		final EscapeHandler         escapeHandler     = config.escapeHandler()                     ;

		for(int i = iStart; i < iBound;)
		{
			final int i1 = parseSegmentStart(input, i, iBound, literalBuilder, config);
			if(i1 == i)
			{
				throw new RuntimeException("invalid segment start at index " + i); // (17.11.2014 TM)EXCP: proper exception
			}
			i = parseSegment(
				input                    ,
				i1                       ,
				iBound                   ,
				recordSeparator          ,
				valueSeparator           ,
				commentSignal            ,
				literalDelimiter         ,
				escaper                  ,
				escapeHandler            ,
				segmentTerminator        ,
				rowAggregator            ,
				literalBuilder           ,
				literalBuilder.toString(),
				columnNames              ,
				columnTypes              ,
				config                   ,
				recordParserProvider
			);
			if(i >= iBound || input[i] != segmentTerminator)
			{
				throw new RuntimeException("unclosed segment at index " + i); // (17.11.2014 TM)EXCP: proper exception
			}
			i = XCsvRecordParserCharArray.Static.skipSkippable(input, i + 1, iBound, config.commentSignal(), config);
		}
	}

	static final void parseSingletonSegment(
		final char[]                            input               ,
		final int                               iStart              ,
		final int                               iBound              ,
		final VarString                         literalBuilder      ,
		final XCsvConfiguration                  config              ,
		final XCsvRowCollector                   rowAggregator       ,
		final XCsvRecordParserCharArray.Provider recordParserProvider
	)
	{
		parseSegment(
			input                                      ,
			iStart                                     ,
			iBound                                     ,
			config.recordSeparator()                   ,
			config.valueSeparator()                    ,
			config.commentSignal()                     ,
			config.literalDelimiter()                  ,
			config.escaper()                           ,
			config.escapeHandler()                     ,
			config.segmentTerminator()                 ,
			rowAggregator                              ,
			literalBuilder                             ,
			null                                       , // simple singleton segment never defines a name
			new ColumnHeaderCollector(new BulkList<>()),
			new ColumnHeaderCollector(new BulkList<>()),
			config                                     ,
			recordParserProvider
		);
	}

	private static int parseSegmentHeader(
		final char[]                input         ,
		final int                   iStart        ,
		final int                   iBound        ,
		final VarString             literalBuilder,
		final ColumnHeaderCollector columnNames   ,
		final ColumnHeaderCollector columnTypes   ,
		final XCsvConfiguration      config
	)
	{
		int i = iStart;

		// ensure helper collections are resetted / empty
		columnNames.clear();
		columnTypes.clear();

		final char terminator      = config.terminator()     ;
		final char valueSeparator  = config.valueSeparator() ;
		final char recordSeparator = config.recordSeparator();
		final char commentSignal   = config.commentSignal()  ;

		i = parseSimpleColumnLine(input, i, iBound, recordSeparator, terminator, config, literalBuilder, columnNames);

		if(i >= iBound || input[i] == terminator)
		{
			return i;
		}
		if(input[i] == recordSeparator)
		{
			i++; // skip record separator
		}

		// skip any comments
		i = XCsvRecordParserCharArray.Static.skipDataComments(
			input, i, iBound, terminator, valueSeparator, recordSeparator, commentSignal, config
		);

		if(i < iBound && input[i] == config.headerStarter())
		{
			// i + 1 to skip the header starter/terminator characters
			i = parseSimpleColumnLine(
				input, i + 1, iBound, config.headerTerminator(), terminator, config, literalBuilder, columnTypes
			);

			if(i >= iBound || input[i] == terminator)
			{
				// (22.11.2014 TM)EXCP: proper exception
				throw new RuntimeException("Unclosed header type line at index " + i);
			}

			// skip header terminator
			i++;

			// skip all discardable whitespaces after the column type line
			while(i < iBound && input[i] != valueSeparator && XChars.isWhitespace(input[i]))
			{
				i++;
			}
			// skip header terminator and any following comments
			i = XCsvRecordParserCharArray.Static.skipDataComments(
				input, i, iBound, terminator, valueSeparator, recordSeparator, commentSignal, config
			);
		}

		// note that line skipping cares only for pure line separators, no delimited symbols or such
		i = skipLines(input, i, iBound, config.recordSeparator(), config.postColumnHeaderSkipLineCount());
		i = XCsvRecordParserCharArray.Static.skipDataComments(
			input, i, iBound, terminator, valueSeparator, recordSeparator, commentSignal, config
		);

		return i;
	}

	private static int parseSegment(
		final char[]                            input               ,
		final int                               iStart              ,
		final int                               iBound              ,
		final char                              recordSeparator     ,
		final char                              valueSeparator      ,
		final char                              commentSignal       ,
		final char                              literalDelimiter    ,
		final char                              escaper             ,
		final EscapeHandler                     escapeHandler       ,
		final char                              terminator          ,
		final XCsvRowCollector                   rowCollector        ,
		final VarString                         literalBuilder      ,
		final String                            segmentName         ,
		final ColumnHeaderCollector             columnNames         ,
		final ColumnHeaderCollector             columnTypes         ,
		final XCsvConfiguration                  config              ,
		final XCsvRecordParserCharArray.Provider recordParserProvider
	)
	{
		if(iStart == iBound)
		{
			return iStart; // reached end of input, don't begin another table
		}

		int i = iStart;

		// parse header, including post column name line skipping
		i = parseSegmentHeader(input, i, iBound, literalBuilder, columnNames, columnTypes, config);

		// note that even if end of input is reached here, a read column header means another (dataless) segment
		rowCollector.beginTable(segmentName, columnNames.values, columnTypes.values);

		// get record parser not before here as the table header might influence/configure the parser
		final XCsvRecordParserCharArray recordParser = recordParserProvider.provideRecordParser();

		// parse rows until end of file
		while(i < iBound)
		{
			i = recordParser.parseRecord(
				input           ,
				i               ,
				iBound          ,
				valueSeparator  ,
				literalDelimiter,
				escaper         ,
				recordSeparator ,
				terminator      ,
				config          ,
				literalBuilder  ,
				escapeHandler   ,
				rowCollector
			);
			rowCollector.completeRow();

			if(i >= iBound || input[i] == terminator)
			{
				break;
			}
			if(input[i] == recordSeparator)
			{
				i = XCsvRecordParserCharArray.Static.skipDataComments(
					input          ,
					i + 1          ,
					iBound         ,
					terminator     ,
					valueSeparator ,
					recordSeparator,
					commentSignal  ,
					config
				);
			}
		}
		rowCollector.completeTable();

		return i;
	}

	public static final XCsvParserCharArray New()
	{
		return new XCsvParserCharArray();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private XCsvParserCharArray()
	{
		super();
	}


	static XCsvSegmentsParser<_charArrayRange> provideSegmentsParser(
		final XCsvConfiguration config,
		final XCsvRowCollector  rowAggregator
	)
	{
		// crazy sh*t indirection nesting
		return input -> parseSegments(
			input.array(),
			input.start(),
			input.bound(),
			VarString.New(),
			config,
			rowAggregator,
			() -> XCsvParserCharArray::parseRecord
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XCsvConfiguration parseCsvData(
		final XCsvConfiguration                            config        ,
		final _charArrayRange                             input         ,
		final XCsvSegmentsParser.Provider<_charArrayRange> parserProvider,
		final XCsvRowCollector                             rowAggregator
	)
	{
		// defaulting logic
		XCsvConfiguration cfg = config != null
			? config
			: XCSV.configurationDefault()
		;
		final XCsvSegmentsParser.Provider<_charArrayRange> pp = parserProvider != null
			? parserProvider
			: XCsvParserCharArray::provideSegmentsParser
		;

		final int    startIndex = input.start();
		final char[] data       = input.array();

		// cut off trailing lines if required
		final int boundIndex = skipLinesBackwards(
			data         ,
			startIndex   ,
			input.bound(),
			cfg.recordSeparator(),
			cfg.trailingLineCount()
		);

		// note that line skipping intentionally cares only for pure line separators, no delimited symbols or such
		int i = skipLines(data, startIndex, boundIndex, cfg.recordSeparator(), cfg.skipLineCount());

		// skip all skippable (whitespaces and comments by passed/default config) at the beginning.
		i = XCsvRecordParserCharArray.Static.skipSkippable(data, i, boundIndex, cfg.commentSignal(), config);

		// check meta characters and replace config if necessary
		final XReference<XCsvConfiguration> refConfig = X.Reference(config);
		i = checkMetaCharacters(data, i, boundIndex, refConfig);
		cfg = refConfig.get();

		// skip all skippable (whitespaces and comments by effective config) until the first non-skippable.
		i = XCsvRecordParserCharArray.Static.skipSkippable(data, i, boundIndex, cfg.commentSignal(), cfg);

		// there is no problem in IT that cannot be solved with one more level of indirection :-D.
		final XCsvSegmentsParser<_charArrayRange> parser = pp.provideSegmentsParser(cfg, rowAggregator);

		// finally to the actual parsing after all the initialization has been done.
		parser.parseSegments(_charArrayRange.New(data, i, boundIndex));

		return cfg;
	}



	static final class ColumnHeaderCollector implements _charRangeProcedure
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final BulkList<String> values;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ColumnHeaderCollector(final BulkList<String> values)
		{
			super();
			this.values = values;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final void clear()
		{
			this.values.clear();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void accept(final char[] data, final int offset, final int length)
		{
			this.values.add(data == null ? null : new String(data, offset, length));
		}

	}

}
