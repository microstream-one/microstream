package net.jadoth.storage.types;

import static net.jadoth.Jadoth.notNull;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

import net.jadoth.Jadoth;
import net.jadoth.X;
import net.jadoth.collections.BulkList;
import net.jadoth.collections.EqConstHashTable;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.csv.CsvConfiguration;
import net.jadoth.csv.CsvRowCollector;
import net.jadoth.csv.CsvSegmentsParser;
import net.jadoth.functional._charRangeProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoFieldComplex;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.util.VMUtils;
import net.jadoth.util.chars.CsvParserCharArray;
import net.jadoth.util.chars.CsvRecordParserCharArray;
import net.jadoth.util.chars.EscapeHandler;
import net.jadoth.util.chars.JadothChars;
import net.jadoth.util.chars.VarString;
import net.jadoth.util.chars._charArrayRange;
import net.jadoth.util.file.JadothFiles;

public interface StorageDataConverterTypeCsvToBinary<S>
{
	public void convertCsv(S source);



	public interface ValueHandler
	{
		/**
		 * Starts parsing a value from the given data array starting at the given offset, esnures the completely
		 * parsed value will get written, returns the position stopping position as the new current offset.
		 */
		public int handleValue(char[] data, int offset, int bound, char separator, char terminator);
	}



	public static StorageDataConverterTypeCsvToBinary<File> New(
		final StorageDataConverterCsvConfiguration    configuration ,
		final PersistenceTypeDictionary               typeDictionary,
		final StorageEntityTypeConversionFileProvider fileProvider
	)
	{
		return New(configuration, typeDictionary, fileProvider, 0);
	}

	public static StorageDataConverterTypeCsvToBinary<File> New(
		final StorageDataConverterCsvConfiguration    configuration ,
		final PersistenceTypeDictionary               typeDictionary,
		final StorageEntityTypeConversionFileProvider fileProvider  ,
		final int                                     bufferSize
	)
	{
		return new StorageDataConverterTypeCsvToBinary.Implementation(
			notNull(configuration) ,
			notNull(typeDictionary),
			notNull(fileProvider)  ,
			bufferSize
		);
	}


	public final class Implementation
	implements
	StorageDataConverterTypeCsvToBinary<File>,
	CsvSegmentsParser.Provider<_charArrayRange>,
	CsvSegmentsParser<_charArrayRange>,
	CsvRecordParserCharArray.Provider,
	CsvRowCollector,
	CsvRecordParserCharArray
	{
		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		static final int
			BIT_SHIFT_DIVIDE_NONE         = 0, // just for completeness' sake
			BIT_SHIFT_DIVIDE_BY_2         = 1, // required to bit-shift-divide by 2
			BIT_SHIFT_DIVIDE_BY_4         = 2, // required to bit-shift-divide by 4
			BIT_SHIFT_DIVIDE_BY_8         = 3, // required to bit-shift-divide by 8
			                              
			BIT_SHIFT_HEX_HIGH_BYTE       = 4, // required to bit-shift-multiply by 16 (1 hex digit)
			
			SIMPLE_CHAR_SKIP_LENGTH       = 2,
			ESCAPED_CHAR_SKIP_LENGTH      = 3,
			DELIMITED_CHAR_LENGTH         = 3,
			DELIMITED_ESCAPED_CHAR_LENGTH = 4  // e.g.: "\n". Also the maximum length a char can need.
		;


		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final StorageDataConverterCsvConfiguration    configuration                  ;
		final PersistenceTypeDictionary               typeDictionary                 ;
		final StorageEntityTypeConversionFileProvider fileProvider                   ;
		final ByteBuffer                              byteBuffer                     ;
		final long                                    byteBufferStartAddress         ;
		final long                                    byteBufferFlushBoundAddress    ;
		final int                                     bufferSize                     ;
		final EqConstHashTable<String, ValueHandler>  simpleValueWriters             ;
		final EqConstHashTable<String, ValueHandler>  theMappingNeverEnds            ;
		final char[]                                  literalTrue                    ;
		final char[]                                  literalFalse                   ;
		final char                                    literalDelimiter               ;
		final char                                    listStarter                    ;
		final char                                    listSeparator                  ;
		final char                                    listTerminator                 ;
		final char                                    escaper                        ;
		final char                                    terminator                     ;
		final EscapeHandler                           escapeHandler                  ;
		final ByteBuffer                              listHeaderUpdateBuffer         ;
		final long                                    addressListHeaderUpdateBuffer  ;
		final ByteBuffer                              entityLengthUpdateBuffer       ;
		final long                                    addressEntityLengthUpdateBuffer;
		final ValueHandler                            objectIdValueHandler           ;

		      File                                    sourceFile                     ;
		      StorageLockedFile                       targetFile                     ;
		      FileChannel                             targetFileChannel              ;
		      long                                    targetFileActualLength         ;
		      PersistenceTypeDescription<?>           currentType                    ;
		      long                                    currentTypeEntityInitLength    ;
		      ValueHandler[]                          valueHandler                   ;

		/*
		 * current buffer address and value setting has to be done manually because the morons
		 * do unnecessary gigantic endian byte shuffling overhead on every damn put.
		 */
		long currentBufferAddress ;
		
		// might be replaced by file-inherent config
		CsvConfiguration actualCsvConfiguation;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final StorageDataConverterCsvConfiguration    configuration ,
			final PersistenceTypeDictionary               typeDictionary,
			final StorageEntityTypeConversionFileProvider fileProvider  ,
			final int                                     bufferSize
		)
		{
			super();
			this.configuration                   = configuration                                                  ;
			this.typeDictionary                  = typeDictionary                                                 ;
			this.fileProvider                    = fileProvider                                                   ;
			// the * 2 is important for simplifying the flush check
			this.bufferSize                      = Math.max(bufferSize, 2 * Memory.defaultBufferSize())           ;
			this.byteBuffer                      = ByteBuffer.allocateDirect(this.bufferSize)                     ;
			this.byteBufferStartAddress          = Memory.directByteBufferAddress(this.byteBuffer)                ;
			this.byteBufferFlushBoundAddress     = this.byteBufferStartAddress + Memory.defaultBufferSize()       ;
			this.simpleValueWriters              = this.deriveSimpleValueWriters(configuration)                   ;
			this.theMappingNeverEnds             = this.derivePrimitiveToArrayWriters(this.simpleValueWriters)    ;
			this.literalTrue                     = configuration.literalBooleanTrue().toCharArray()               ;
			this.literalFalse                    = configuration.literalBooleanFalse().toCharArray()              ;
			this.literalDelimiter                = configuration.csvConfiguration().literalDelimiter()            ;
			this.listStarter                     = configuration.literalListStarter()                             ;
			this.listSeparator                   = configuration.literalListSeparator()                           ;
			this.listTerminator                  = configuration.literalListTerminator()                          ;
			this.terminator                      = configuration.csvConfiguration().terminator()                  ;
			this.escaper                         = configuration.csvConfiguration().escaper()                     ;
			this.escapeHandler                   = configuration.csvConfiguration().escapeHandler()               ;
			this.listHeaderUpdateBuffer          = ByteBuffer.allocateDirect(BinaryPersistence.lengthListHeader());
			this.addressListHeaderUpdateBuffer   = Memory.directByteBufferAddress(this.listHeaderUpdateBuffer)    ;
			this.entityLengthUpdateBuffer        = ByteBuffer.allocateDirect(BinaryPersistence.lengthLength())    ;
			this.addressEntityLengthUpdateBuffer = Memory.directByteBufferAddress(this.entityLengthUpdateBuffer)  ;
			this.objectIdValueHandler            = this.simpleValueWriters.get(long.class.getName())              ;
			this.currentBufferAddress            = this.byteBufferStartAddress                                    ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		final EqConstHashTable<String, ValueHandler>  derivePrimitiveToArrayWriters(
			final EqConstHashTable<String, ValueHandler> valueWriters
		)
		{
			return EqConstHashTable.New(
				X.keyValue(byte   .class.getName(), valueWriters.get(byte[]   .class.getName())),
				X.keyValue(boolean.class.getName(), valueWriters.get(boolean[].class.getName())),
				X.keyValue(short  .class.getName(), valueWriters.get(short[]  .class.getName())),
				X.keyValue(char   .class.getName(), valueWriters.get(char[]   .class.getName())),
				X.keyValue(int    .class.getName(), valueWriters.get(int[]    .class.getName())),
				X.keyValue(float  .class.getName(), valueWriters.get(float[]  .class.getName())),
				X.keyValue(long   .class.getName(), valueWriters.get(long[]   .class.getName())),
				X.keyValue(double .class.getName(), valueWriters.get(double[] .class.getName()))
			);
		}

		final EqConstHashTable<String, ValueHandler> deriveSimpleValueWriters(
			final StorageDataConverterCsvConfiguration configuration
		)
		{
			return EqConstHashTable.New(
				X.keyValue(byte     .class.getName()                    , this::parse_byte        ),
				X.keyValue(boolean  .class.getName()                    , this::parse_boolean     ),
				X.keyValue(short    .class.getName()                    , this::parse_short       ),
				X.keyValue(char     .class.getName()                    , this::parse_char        ),
				X.keyValue(int      .class.getName()                    , this::parse_int         ),
				X.keyValue(float    .class.getName()                    , this::parse_float       ),
				X.keyValue(long     .class.getName()                    , this::parse_long        ),
				X.keyValue(double   .class.getName()                    , this::parse_double      ),
				X.keyValue(byte[]   .class.getName()                    , this::parseArray_byte   ),
				X.keyValue(boolean[].class.getName()                    , this::parseArray_boolean),
				X.keyValue(short[]  .class.getName()                    , this::parseArray_short  ),
				X.keyValue(char[]   .class.getName()                    , this::parseChars        ),
				X.keyValue(int[]    .class.getName()                    , this::parseArray_int    ),
				X.keyValue(float[]  .class.getName()                    , this::parseArray_float  ),
				X.keyValue(long[]   .class.getName()                    , this::parseArray_long   ),
				X.keyValue(double[] .class.getName()                    , this::parseArray_double ),
				X.keyValue(PersistenceTypeDictionary.Symbols.typeChars(), this::parseChars        ),
				X.keyValue(PersistenceTypeDictionary.Symbols.typeBytes(), this::parseBytes        )
			);
		}

		final int parse_byte(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_byte(JadothChars.parse_byteDecimal(data, offset, j - offset + 1));
			return i;
		}

		final int parse_boolean(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}

			final int literalLength = j - offset + 1;
			if(literalLength == this.literalTrue.length
			&& JadothChars.equals(data, offset, this.literalTrue, 0, this.literalTrue.length)
			)
			{
				this.write_boolean(true);
				return i;
			}
			if(literalLength == this.literalFalse.length
			&& JadothChars.equals(data, offset, this.literalFalse, 0, this.literalFalse.length)
			)
			{
				this.write_boolean(false);
				return i;
			}

			// (12.10.2014)EXCP: proper exception
			throw new RuntimeException("Invalid boolean literal: " + String.valueOf(data, offset, literalLength));
		}

		final int parse_short(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_short(JadothChars.parse_shortDecimal(data, offset, j - offset + 1));
			return i;
		}

		final int parse_char(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// so much logic for parsing a simple character ... however: a lot of different cases, so yeah, it is.
			return bound - offset >= DELIMITED_ESCAPED_CHAR_LENGTH
				? this.parse_charNormal(data, offset)
				: this.parse_charTrailing(data, offset, bound - offset)
			;
		}
		
		
		// easy case where max length character literal (length 4) can never reach the bound
		final int parse_charNormal(final char[] data, final int offset)
		{
			// check for opening delimiter
			if(data[offset] != this.literalDelimiter)
			{
				// (14.10.2014 TM)EXCP: proper exception
				throw new RuntimeException("Invalid character literal at " + offset);
			}

			// check for special case (escaped character)
			if(data[offset + 1] == this.escaper)
			{
				if(data[offset + ESCAPED_CHAR_SKIP_LENGTH] != this.literalDelimiter)
				{
					// (14.10.2014 TM)EXCP: proper exception
					throw new RuntimeException("Unclosed character literal at " + (offset + ESCAPED_CHAR_SKIP_LENGTH));
				}
				this.write_char(this.escapeHandler.unescape(data[offset + SIMPLE_CHAR_SKIP_LENGTH]));
				return offset + DELIMITED_ESCAPED_CHAR_LENGTH;
			}

			// normal case: no escaper
			if(data[offset + SIMPLE_CHAR_SKIP_LENGTH] != this.literalDelimiter)
			{
				// (14.10.2014 TM)EXCP: proper exception
				throw new RuntimeException("Unclosed character literal at " + (offset + SIMPLE_CHAR_SKIP_LENGTH));
			}
			
			this.write_char(data[offset + 1]);
			return offset + DELIMITED_CHAR_LENGTH;
		}

		// special-cased trailing character logic
		final int parse_charTrailing(final char[] data, final int offset, final int length)
		{
			// special case checks
			if(length != DELIMITED_CHAR_LENGTH
			|| data[offset    ] != this.literalDelimiter
			|| data[offset + 1] == this.escaper
			|| data[offset + 2] != this.literalDelimiter
			)
			{
				// (14.10.2014 TM)EXCP: proper exception
				throw new RuntimeException("Invalid character literal at " + offset);
			}

			// simply write the character value
			this.write_char(data[offset + 1]);
			return offset + DELIMITED_CHAR_LENGTH;
		}

		final int parse_int(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_int(JadothChars.parse_intDecimal(data, offset, j - offset + 1));
			return i;
		}

		final int parse_float(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_float(JadothChars.parse_float(data, offset, j - offset + 1));
			return i;
		}

		final int parse_long(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_long(JadothChars.parse_longDecimal(data, offset, j - offset + 1));
			return i;
		}

		final int parse_double(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = offset;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				i++;
			}
			int j = i - 1;
			while(data[j] <= ' ')
			{
				j--;
			}
			this.write_double(JadothChars.parse_double(data, offset, j - offset + 1));
			return i;
		}

		final int parseChars(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			if(data[offset] != this.literalDelimiter)
			{
				// (11.11.2014 TM)EXCP: proper Exception
				throw new RuntimeException("Missing literal delimiter " + this.listStarter + " at offset " + offset);
			}

			final long          currentFileOffset = this.writeListHeader();
			final int           lastCharIndex     = bound - 1             ;
			final char          literalDelimiter  = this.literalDelimiter ;
			final char          escaper           = this.escaper          ;
			final EscapeHandler escapeHandler     = this.escapeHandler    ;

			int i = offset;

			while(true)
			{
				if(++i >= bound)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("incomplete trailing characters at offset " + bound);
				}
				if(data[i] == literalDelimiter)
				{
					break;
				}
				if(data[i] == escaper)
				{
					if(i == lastCharIndex)
					{
						// (11.11.2014 TM)EXCP: proper Exception
						throw new RuntimeException("incomplete characters literal at offset " + bound);
					}
					this.write_char(escapeHandler.unescape(data[++i]));
				}
				else
				{
					this.write_char(data[i]);
				}
			}

			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_2); // 2 byte per value

			// return current position in the source array, skipping the delimiter
			return i + 1;
		}

		final int parseBytes(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			final long currentFileOffset = this.writeListHeader();
			final int  lastCharIndex     = bound - 1;

			int i = offset;

			while(i < bound)
			{
				if(data[i] == separator || data[i] == terminator)
				{
					break;
				}
				// intentionally ignore white spaces in hex string
				if(data[i] <= ' ')
				{
					i++;
					continue;
				}
				if(i >= lastCharIndex || data[i + 1] == separator || data[i + 1] == terminator || data[i + 1] <= ' ')
				{
					throw new RuntimeException("Incomplete hexadecimal string at offset " + i);
				}
				this.write_byte((byte)((toValue(data[i]) << BIT_SHIFT_HEX_HIGH_BYTE) + toValue(data[i + 1])));
				i += 2;
			}

			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_NONE); // 1 byte per value

			return i;
		}

		// 0 is decimal ASCII 48. So 48 has to be subtracted from the digit's ASCII value to get the decimal value.
		private static final int ASCII_OFFSET_HEX_TO_DEC_DIGIT = 48;
		
		// A is 65 but means a decimal value of 10. So (65 - 10) = 55 has to be subtracted from the letter.
		private static final int ASCII_OFFSET_HEX_TO_DEC_CHAR = 55;
		
		static final int toValue(final char hexDigit)
		{
			// 0-9 are more chars than A-f, so check them first
			if(hexDigit < ':' && hexDigit >= '0')
			{
				return hexDigit - ASCII_OFFSET_HEX_TO_DEC_DIGIT;
			}
			
			// check A-F
			if(hexDigit < 'G' && hexDigit >= 'A')
			{
				return hexDigit - ASCII_OFFSET_HEX_TO_DEC_CHAR;
			}
			
			// everything else is an error
			// (12.11.2014)EXCP: proper exception
			throw new RuntimeException("Invalid hexadeximal character: " + hexDigit);
		}

		final int parseArray_byte(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_byte(JadothChars.parse_byteDecimal(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_NONE); // 1 byte per value

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_boolean(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// (16.11.2014)TODO: storage csv to bin conversion: maybe single-char boolean array implementation

			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long   currentFileOffset = this.writeListHeader();
			final char   listSeparator     = this.listSeparator    ;
			final char   listTerminator    = this.listTerminator   ;
			final char[] literalTrue       = this.literalTrue      ;
			final char[] literalFalse      = this.literalFalse     ;
			final int    lengthTrue        = literalTrue .length   ;
			final int    lengthFalse       = literalFalse.length   ;

			// iteration state
			int i = offset + 1;

			long elementCount = 0;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int elementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);

				// parse literal. A little more complex for boolean literals.
				if(i - elementStart == lengthTrue && JadothChars.equals(data, elementStart, literalTrue, 0, lengthTrue))
				{
					this.write_boolean(true);
				}
				else if(i - elementStart == lengthFalse
					&& JadothChars.equals(data, elementStart, literalFalse, 0, lengthFalse)
				)
				{
					this.write_boolean(false);
				}
				else
				{
					// (12.11.2014 TM)EXCP: proper exception
					throw new RuntimeException("Invalid boolean literal at offset " + elementStart);
				}
				elementCount++;

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				BinaryPersistence.calculateBinaryArrayByteLength(elementCount * Memory.byteSize_boolean()),
				elementCount
			);

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_short(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_short(JadothChars.parse_shortDecimal(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_2); // 2 bytes per value

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_int(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_int(JadothChars.parse_intDecimal(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_4); // 4 bytes per value

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_float(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_float(JadothChars.parse_float(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_4); // 4 bytes per value

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_long(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, listSeparator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_long(JadothChars.parse_longDecimal(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_8); // 8 bytes per value

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		final int parseArray_double(
			final char[] data      ,
			final int    offset    ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			// iteration state
			int i = offset + 1;

			// parse list/array literal until terminator character is encountered
			while(data[i = seekSimpleLiteralStart(data, i, bound, separator)] != listTerminator)
			{
				// parse literal
				final int currentElementStart = i;
				i = seekSimpleLiteralEnd(data, i, bound, listSeparator, listTerminator);
				this.write_double(JadothChars.parse_double(data, currentElementStart, i - currentElementStart));

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header
			this.retroUpdateListHeader(currentFileOffset, BIT_SHIFT_DIVIDE_BY_8);

			// return current position in the source array, skipping the terminator character
			return seekValueTerminator(data, i + 1, bound, separator, terminator); // +1 to skip list terminator
		}

		static final void validateListStart(final char[] data, final int offset, final char listStarter)
		{
			if(data[offset] != listStarter)
			{
				// (11.11.2014 TM)EXCP: proper Exception
				throw new RuntimeException("Missing list starter character " + listStarter + " at offset " + offset);
			}
		}

		final void beginEntity()
		{
			this.write_long(this.currentTypeEntityInitLength);
			this.write_long(this.currentType.typeId());
		}

		private long writeListHeader()
		{
			// keep current file offset for later header update
			final long currentFileOffset = this.getTargetFileVirtualLength();

			// write binary list header (length, count)
			
			// must check for flush to guarantee the header is never split between flushes!
			this.checkForFlush();
			// list binary length. Intentionally invalid initial length value.
			Memory.set_long(this.currentBufferAddress                         , 0);
			// list element count. None so far.
			Memory.set_long(this.currentBufferAddress + Memory.byteSize_long(), 0);
			
			this.currentBufferAddress += 2 * Memory.byteSize_long();

			return currentFileOffset;
		}

		private void retroUpdateListHeader(final long currentFileOffset, final int bitDivisor)
		{
			final long binaryLength = this.getTargetFileVirtualLength() - currentFileOffset;

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				binaryLength,
				binaryLength - BinaryPersistence.lengthListHeader() >> bitDivisor
			);
		}

		static final int seekListElementCompletion(
			final char[] data      ,
			final int    index     ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = index;
			while(true)
			{
				if(data[i] == separator)
				{
					// return, skipping the completed element's separator to have the index ready for next element
					return i + 1;
				}
				if(data[i] == terminator)
				{
					// return, NOT skipping the terminator for end of list recognition in outside loop
					return i;
				}
				if(data[i] > ' ')
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("incomplete missing separator at offset " + i);
				}
				if(++i >= bound)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("incomplete trailing list at offset " + bound);
				}
			}
		}

		static final int seekValueTerminator(
			final char[] data      ,
			final int    index     ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = index;
			while(i < bound && data[i] != separator && data[i] != terminator)
			{
				if(data[i] > ' ')
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("missing value separator at offset " + i);
				}
				i++;
			}
			return i;
		}

		static final int seekSimpleLiteralStart(
			final char[] data     ,
			final int    index    ,
			final int    bound    ,
			final char   separator
		)
		{
			int i = index;
			// scroll to actual literal start
			while(true)
			{
				// separator must be checked before white space in case it is a white space itself
				if(data[i] == separator)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("missing list element at offset " + i);
				}
				if(data[i] > ' ')
				{
					break;
				}
				if(++i >= bound)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("incomplete trailing list at offset " + bound);
				}
			}
			return i;
		}

		static final int seekSimpleLiteralEnd(
			final char[] data      ,
			final int    index     ,
			final int    bound     ,
			final char   separator ,
			final char   terminator
		)
		{
			int i = index;
			while(data[i] != separator && data[i] != terminator && data[i] > ' ')
			{
				if(++i >= bound)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("Incomplete trailing list at offset " + bound);
				}
			}
			return i;
		}

		private void setTargetFile()
		{
			final File targetFile = this.fileProvider.provideConversionFile(this.currentType, this.sourceFile);
			final StorageLockedFile currentTargetFile;
			try
			{
				JadothFiles.ensureDirectory(targetFile.getParentFile());
				currentTargetFile = StorageLockedFile.openLockedFile(JadothFiles.ensureWriteableFile(targetFile));
			}
			catch(final Exception e)
			{
				throw new RuntimeException(e); // (01.10.2014 TM)EXCP: proper exception
			}

			this.targetFile        = currentTargetFile;
			this.targetFileChannel = this.targetFile.fileChannel();
			try
			{
				this.targetFileActualLength = this.targetFileChannel.size();
			}
			catch(final IOException e)
			{
				// (07.10.2014 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}

		private void setSourceFile(final File file)
		{
			this.clearCurrentFileState();

			// set instance state at the end to guarantee consistency
			this.sourceFile        = file;
		}

		final void parseCurrentFile()
		{
			final char[]             input  = JadothFiles.readCharsFromFile(
				this.sourceFile,
				Charset.forName("UTF-8"),
				VMUtils::throwUnchecked
			);
			final CsvParserCharArray parser = CsvParserCharArray.New();
			parser.parseCsvData(this.configuration.csvConfiguration(), _charArrayRange.New(input), this, this);
		}

		final void clearCurrentFileState()
		{
			// flush buffer to be sure. Unnecessary case gets checked inside
			this.flushBuffer();

			StorageFile.closeSilent(this.targetFile);
			Jadoth.closeSilent(this.targetFileChannel); // already done by locked file, but it's clearer that way

			this.sourceFile            = null;
			this.targetFile            = null;
			this.targetFileChannel     = null;
			this.actualCsvConfiguation = null;
		}

		final void validateTypeNames(final XGettingList<String> dataColumntypes)
		{
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members = this.currentType.members();

			if(members.size() != dataColumntypes.size())
			{
				// (02.10.2014 TM)EXCP: proper exception
				throw new RuntimeException(
					"Count mismatch between data column count (" + dataColumntypes.size() + ") and field count ("
					+ members.size() + ") of type " + this.currentType.typeName()
				);
			}

			final String referenceTypeName = this.configuration.referenceTypeName();

			final Iterator<? extends PersistenceTypeDescriptionMember> memberIterator = members.iterator();
			for(final String columnTypeName : dataColumntypes)
			{
				final PersistenceTypeDescriptionMember member = memberIterator.next();
				if(member.isReference())
				{
					if(!referenceTypeName.equals(columnTypeName))
					{
						// (02.10.2014 TM)EXCP: proper exception
						throw new RuntimeException(
							"CSV reference column type mismatch: " + columnTypeName + " != " + member.typeName()
						);
					}
				}
				else
				{
					final String fieldTypeName = this.configuration.resolveActualTypeName(columnTypeName);
					if(!fieldTypeName.equals(member.typeName()))
					{
						// (02.10.2014 TM)EXCP: proper exception
						throw new RuntimeException(
							"CSV non-reference column type mismatch: " + fieldTypeName + " != " + member.typeName()
						);
					}
				}
			}
		}

		final void deriveValueHandlers()
		{
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members = this.currentType.members();
			final ValueHandler[] valueHandlers = new ValueHandler[X.checkArrayRange(members.size()) + 1];
			valueHandlers[0] = this.objectIdValueHandler;
			int i = 1;
			boolean hasVariableLength = false;

			long entityBaseLength = BinaryPersistence.entityHeaderLength();

			for(final PersistenceTypeDescriptionMember member : members)
			{
				// add fixed length member length right away, variable member length is dynamically added later
				if(member.isFixedLength())
				{
					entityBaseLength += member.persistentMinimumLength();
				}
				else
				{
					hasVariableLength = true;
				}
				valueHandlers[i++] = this.deriveValueWriter(member);
			}

			this.valueHandler = valueHandlers;

			this.currentTypeEntityInitLength = hasVariableLength ? 0 : entityBaseLength;
		}

		final ValueHandler deriveValueWriter(final PersistenceTypeDescriptionMember member)
		{
			final String typeName = member.typeName();

			// handles [char] and [byte] as well
			final ValueHandler valueWriter = this.simpleValueWriters.get(typeName);
			if(valueWriter != null)
			{
				return valueWriter;
			}
			if(member.isReference())
			{
				return this.objectIdValueHandler;
			}

			if(!(member instanceof PersistenceTypeDescriptionMemberPseudoFieldComplex))
			{
				// (15.11.2014)EXCP: proper exception
				throw new RuntimeException("Unhandled non-complex persistence field type: " + typeName);
			}

			final XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members =
				((PersistenceTypeDescriptionMemberPseudoFieldComplex)member).members()
			;

			if(members.size() == 1)
			{
				final PersistenceTypeDescriptionMemberPseudoField singletonField = members.first();

				// check for reference
				if(singletonField.isReference())
				{
					// references (obejctIds) are simply handled as longs
					return this.simpleValueWriters.get(long[].class.getName());
				}

				// check for arrays
				final ValueHandler primitiveArrayHandler = this.theMappingNeverEnds.get(singletonField.typeName());
				if(primitiveArrayHandler != null)
				{
					return primitiveArrayHandler;
				}

				// case complex with single value (e.g. list of strings, list of bytes)
				return new NestedValueHandler(this.deriveValueWriter(singletonField));
			}

			// complex list with multiple fields per element
			return this.deriveComplexValueHandlerMulti(members);
		}

		final ValueHandler deriveComplexValueHandlerMulti(
			final XGettingSequence<PersistenceTypeDescriptionMemberPseudoField> members
		)
		{
			final ValueHandler[] valueHandlers = new ValueHandler[X.checkArrayRange(members.size())];

			int i = 0;
			for(final PersistenceTypeDescriptionMemberPseudoField member : members)
			{
				valueHandlers[i++] = this.deriveValueWriter(member);
			}

			return new NestedValueHandlers(valueHandlers);
		}

		final int parseComplexListSingleField(
			final char[]        data         ,
			final int           offset       ,
			final int           bound        ,
			final char          separator    ,
			final char          terminator   ,
			final ValueHandler  valueHandler
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			int i = offset + 1; // skip list starter
			long elementCount = 0;

			// loop until all elements are parsed (list terminator is encountered)
			while(true)
			{
				if(i >= bound)
				{
					// (15.11.2014)EXCP: proper exception
					throw new RuntimeException("Incomplete complex list at offset " + bound);
				}
				if(data[i] == listTerminator)
				{
					// all complex list's terminator encountered, break loop
					break;
				}
				else if(data[i] <= ' ')
				{
					// skip whitespaces
					i++;
					continue;
				}

				// handle value
				i = valueHandler.handleValue(data, i, bound, listSeparator, listTerminator);

				// element completed, increment count.
				elementCount++;

				// post-element scrolling and checking
				while(true)
				{
					if(i >= bound)
					{
						// (15.11.2014)EXCP: proper exception
						throw new RuntimeException("Incomplete complex list at offset " + bound);
					}
					else if(data[i] <= ' ')
					{
						// skip whitespaces
						i++;
						continue;
					}
					else if(data[i] == listTerminator)
					{
						// end of list reached, continue main loop for aborting.
						break;
					}
					else if(data[i] == listSeparator)
					{
						// found this element's terminating list separator, skip it and continue main loop
						i++;
						break;
					}
				}
			}

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				this.getTargetFileVirtualLength() - currentFileOffset,
				elementCount
			);

			return i;
		}

		final int parseComplexListMulti(
			final char[]         data         ,
			final int            offset       ,
			final int            bound        ,
			final char           separator    ,
			final char           terminator   ,
			final ValueHandler[] valueHandlers
		)
		{
			// initial consistency validation
			validateListStart(data, offset, this.listStarter);

			// keep current file offset for later header update
			final long currentFileOffset = this.writeListHeader();
			final int  handlerIndexBound = valueHandlers.length  ;
			final char listStarter       = this.listStarter      ;
			final char listSeparator     = this.listSeparator    ;
			final char listTerminator    = this.listTerminator   ;

			int i = offset + 1; // skip list starter
			long elementCount = 0;

			// loop until all elements are parsed (list terminator is encountered)
			while(true)
			{
				if(i >= bound)
				{
					// (15.11.2014)EXCP: proper exception
					throw new RuntimeException("Incomplete complex list at offset " + bound);
				}
				if(data[i] == listTerminator)
				{
					// all complex list's terminator encountered, break loop
					break;
				}
				else if(data[i] <= ' ')
				{
					// skip whitespaces
					i++;
					continue;
				}

				// check for start of next complex list element and parse it
				if(data[i] != listStarter)
				{
					// (11.11.2014 TM)EXCP: proper Exception
					throw new RuntimeException("Missing list starter character " + listStarter + " at offset " + i);
				}

				// skip found list starter
				i++;

				// each element requires the handler array to be applied
				for(int h = 0; h < handlerIndexBound; h++)
				{
					if(i >= bound)
					{
						// (15.11.2014)EXCP: proper exception
						throw new RuntimeException("Incomplete complex list at offset " + bound);
					}
					else if(data[i] == listTerminator)
					{
						// (15.11.2014)EXCP: proper exception
						throw new RuntimeException("Incomplete complex list at offset " + i);
					}
					else if(data[i] == listStarter)
					{
						// (11.11.2014 TM)EXCP: proper Exception
						throw new RuntimeException("Missing complex list element at offset " + i);
					}
					else if(data[i] <= ' ')
					{
						// skip whitespaces
						i++;
					}
					else
					{
						i = valueHandlers[h].handleValue(data, i, bound, listSeparator, listTerminator);
						i++; // skip element terminator
					}
				}

				// element completed, increment count.
				elementCount++;

				// post-element scrolling and checking
				while(true)
				{
					if(i >= bound)
					{
						// (15.11.2014)EXCP: proper exception
						throw new RuntimeException("Incomplete complex list at offset " + bound);
					}
					else if(data[i] <= ' ')
					{
						// skip whitespaces
						i++;
						continue;
					}
					else if(data[i] == listTerminator)
					{
						// end of list reached, continue main loop for aborting.
						break;
					}
					else if(data[i] == listSeparator)
					{
						// found this element's terminating list separator, skip it and continue main loop
						i++;
						break;
					}
				}
			}

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				this.getTargetFileVirtualLength() - currentFileOffset,
				elementCount
			);

			return i;
		}

		final class NestedValueHandler implements ValueHandler
		{
			final ValueHandler valueHandler;

			NestedValueHandler(final ValueHandler valueHandler)
			{
				super();
				this.valueHandler = valueHandler;
			}

			@Override
			public int handleValue(
				final char[] data      ,
				final int    offset    ,
				final int    bound     ,
				final char   separator ,
				final char   terminator
			)
			{
				return Implementation.this.parseComplexListSingleField(data, offset, bound, separator, terminator, this.valueHandler);
			}
		}

		final class NestedValueHandlers implements ValueHandler
		{
			final ValueHandler[] valueHandlers;

			NestedValueHandlers(final ValueHandler[] valueHandlers)
			{
				super();
				this.valueHandlers = valueHandlers;
			}

			@Override
			public int handleValue(
				final char[] data      ,
				final int    offset    ,
				final int    bound     ,
				final char   separator ,
				final char   terminator
			)
			{
				return Implementation.this.parseComplexListMulti(data, offset, bound, separator, terminator, this.valueHandlers);
			}
		}



		final void retroUpdateEntityLength(final long filePosition, final long entityTotalLength)
		{
			if(filePosition - this.targetFileActualLength >= 0)
			{
				// simple case: the position to be updated is still in the buffer, so just set the value in-memory
				Memory.set_long(this.byteBufferStartAddress + filePosition - this.targetFileActualLength, entityTotalLength);
			}
			else
			{
				// not so simple case: target position was already flushed to the file, hence update there
				Memory.set_long(this.addressEntityLengthUpdateBuffer, entityTotalLength);
				this.writeBuffer(this.entityLengthUpdateBuffer, filePosition);
			}
		}

		final void retroUpdateListHeader(final long filePosition, final long length, final long elementCount)
		{
			if(filePosition - this.targetFileActualLength >= 0)
			{
				// simple case: the position to be updated is still in the buffer, so just set the values in-memory
				final long offset = filePosition - this.targetFileActualLength;
				Memory.set_long(this.byteBufferStartAddress + offset, length);
				Memory.set_long(this.byteBufferStartAddress + offset + Memory.byteSize_long(), elementCount);
			}
			else
			{
				// not so simple case: target position was already flushed to the file, hence update there
				Memory.set_long(this.addressListHeaderUpdateBuffer, length);
				Memory.set_long(this.addressListHeaderUpdateBuffer + Memory.byteSize_long(), elementCount);
				this.writeBuffer(this.listHeaderUpdateBuffer, filePosition);
			}
		}

		final void writeBuffer(final ByteBuffer byteBuffer, final long filePosition)
		{
			try
			{
				// no idea if this is necessary, especially on such tiny buffers, but completeness must be guaranteed.
				while(byteBuffer.hasRemaining())
				{
					this.targetFileChannel.write(byteBuffer, filePosition + byteBuffer.position());
				}
			}
			catch(final IOException e)
			{
				Jadoth.closeSilent(this.targetFileChannel);
				throw new RuntimeException(e); // (15.10.2014 TM)EXCP: proper exception
			}
			finally
			{
				// reset for next write, even if the current one fails (better to keep things consistent)
				byteBuffer.clear();
			}
		}

		final void flushBuffer()
		{
			// check for unnecessary call, prevent potentially nasty cases like 0-byte writes.
			if(this.currentBufferAddress == this.byteBufferStartAddress)
			{
				return;
			}

			this.byteBuffer.limit((int)(this.currentBufferAddress - this.byteBufferStartAddress));

			// write byte buffer content, ensure all bytes are written
			try
			{
				/* (07.10.2014 TM)TODO: better csv to binary write handling
				 * Maybe check returned written byte count (however that should be guaranteed, shouldn't it?)
				 * Maybe also modularize, and if it's just to get rid of the ugly exception.
				 */
				while(this.byteBuffer.hasRemaining())
				{
					this.targetFileChannel.write(this.byteBuffer);
				}
			}
			catch(final IOException e)
			{
				throw new RuntimeException(e); // (07.10.2014 TM)EXCP: proper exception
			}

			// exactely 'limit' bytes are guaranteed to have been written.
			this.targetFileActualLength += this.byteBuffer.limit();

			// reset buffer state.
			this.byteBuffer.clear();
			this.currentBufferAddress = this.byteBufferStartAddress;
		}

		final void checkForFlush()
		{
			if(this.currentBufferAddress < this.byteBufferFlushBoundAddress)
			{
				return;
			}
			this.flushBuffer();
		}

		final long getTargetFileVirtualLength()
		{
			return this.targetFileActualLength + this.currentBufferAddress - this.byteBufferStartAddress;
		}

		final void write_byte(final byte value)
		{
			this.checkForFlush();
			Memory.set_byte(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_byte();
		}

		final void write_boolean(final boolean value)
		{
			this.checkForFlush();
			Memory.set_boolean(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_boolean();
		}

		final void write_short(final short value)
		{
			this.checkForFlush();
			Memory.set_short(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_short();
		}

		final void write_char(final char value)
		{
			this.checkForFlush();
			Memory.set_char(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_char();
		}

		final void write_int(final int value)
		{
			this.checkForFlush();
			Memory.set_int(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_int();
		}

		final void write_float(final float value)
		{
			this.checkForFlush();
			Memory.set_float(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_float();
		}

		final void write_long(final long value)
		{
			this.checkForFlush();
			Memory.set_long(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_long();
		}

		final void write_double(final double value)
		{
			this.checkForFlush();
			Memory.set_double(this.currentBufferAddress, value);
			this.currentBufferAddress += Memory.byteSize_double();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void convertCsv(final File file)
		{
			this.setSourceFile(file);
			this.parseCurrentFile();
			this.clearCurrentFileState();
		}

		@Override
		public void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			final String typeName = tableName != null
				? tableName
				: JadothFiles.getSuffixlessFileName(this.sourceFile)
			;
			if((this.currentType = this.typeDictionary.lookupTypeByName(typeName)) == null)
			{
				throw new RuntimeException("Type not found: " + typeName); // (01.10.2014 TM)EXCP: proper exception
			}

			final String firstColumnName = columnNames.first();
			if(!this.configuration.objectIdColumnName().equals(firstColumnName))
			{
				// (01.10.2014 TM)EXCP: proper exception
				throw new RuntimeException(
					"First column (" + firstColumnName + ") is not " + this.configuration.objectIdColumnName()
				);
			}

			this.validateTypeNames(BulkList.New(columnTypes).removeRange(0, 1));
			this.deriveValueHandlers();

			this.setTargetFile();
		}

		@Override
		public void accept(final char[] data, final int offset, final int length)
		{
			// nothing to do here. Record parser writes values with individual value writer on its own.
		}

		@Override
		public final void completeRow()
		{
			// nothing to do here. Buffer flush check is done automatically and at the end
		}

		@Override
		public final void completeTable()
		{
			this.flushBuffer();
		}

		@Override
		public CsvSegmentsParser<_charArrayRange> provideSegmentsParser(
			final CsvConfiguration config       ,
			final CsvRowCollector  rowAggregator
		)
		{
			this.actualCsvConfiguation = config;
			return this; // implements ALL the interfaces!
		}

		@Override
		public void parseSegments(final _charArrayRange input)
		{
			CsvParserCharArray.parseSegments(
				input.array(),
				input.start(),
				input.bound(),
				VarString.New(),
				this.actualCsvConfiguation,
				this,
				this
			);
		}

		@Override
		public CsvRecordParserCharArray provideRecordParser()
		{
			return this;
		}

		@Override
		public int parseRecord(
			final char[]              input          ,
			final int                 iStart         ,
			final int                 iBound         ,
			final char                valueSeparator ,
			final char                delimiter      ,
			final char                escaper        ,
			final char                recordSeparator,
			final char                terminator     ,
			final CsvConfiguration    config         ,
			final VarString           literalBuilder ,
			final EscapeHandler       escapeHandler  ,
			final _charRangeProcedure valueCollector
		)
		{
			final ValueHandler[] valueHandlers      = this.valueHandler    ;
			final int            handlerCount       = valueHandlers.length ;
			final long           entityFilePosition = this.getTargetFileVirtualLength();

			// write initial length and type id. Object id gets written as a normal first value.
			this.beginEntity();

			int i = iStart;
			for(int h = 0; h < handlerCount; h++)
			{
				if(i == iBound)
				{
					// (16.10.2014 TM)EXCP: proper exception
					throw new RuntimeException("Incomplete record at index " + i);
				}
				else if(input[i] == valueSeparator || input[i] == recordSeparator || input[i] == terminator)
				{
					// encountered the next separator while searching for a literal, interpret as error
					/* there are no null values in storage CSV files, as NULL reference is OID literal "0"
					 * and everything else are just primitives anyway.
					 */
					// (16.10.2014 TM)EXCP: proper exception
					throw new RuntimeException("Missing value at index " + i);
				}
				else if(input[i] <= ' ')
				{
					i++; // skip unimportant whitespace
				}
				else
				{
					i = valueHandlers[h].handleValue(input, i, iBound, valueSeparator, recordSeparator);
					if(i >= iBound)
					{
						// check if valid end
						if(h < handlerCount - 1)
						{
							// (16.10.2014 TM)EXCP: proper exception
							throw new RuntimeException("Missing record value at index " + i);
						}
						break;
					}
					i++; // skip valueTerminator
				}
			}
			// end of record reached consistently

			if(this.currentTypeEntityInitLength == 0)
			{
				this.retroUpdateEntityLength(
					entityFilePosition,
					this.getTargetFileVirtualLength() - entityFilePosition
				);
			}

			// skip separator and any number and type of comments
			return i;
		}

	}

}
