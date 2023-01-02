package one.microstream.storage.types;

/*-
 * #%L
 * microstream-storage
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

import java.nio.ByteBuffer;
import java.util.Iterator;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.afs.types.AWritableFile;
import one.microstream.chars.EscapeHandler;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.chars.XCsvParserCharArray;
import one.microstream.chars._charArrayRange;
import one.microstream.collections.BulkList;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional._charRangeProcedure;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.util.xcsv.XCsvConfiguration;
import one.microstream.util.xcsv.XCsvRecordParserCharArray;
import one.microstream.util.xcsv.XCsvRowCollector;
import one.microstream.util.xcsv.XCsvSegmentsParser;

public interface StorageDataConverterTypeCsvToBinary<S>
{
	public void convertCsv(S source);



	public interface ValueHandler
	{
		/**
		 * Starts parsing a value from the given data array starting at the given offset, esnures the completely
		 * parsed value will get written, returns the position stopping position as the new current offset.
		 * 
		 * @param data the data array
		 * @param offset start offset
		 * @param bound bound offset
		 * @param separator separator char
		 * @param terminator terminator char
		 * @return the position stopping position as the new current offset
		 */
		public int handleValue(char[] data, int offset, int bound, char separator, char terminator);
	}



	public static StorageDataConverterTypeCsvToBinary<AFile> New(
		final StorageDataConverterCsvConfiguration    configuration ,
		final PersistenceTypeDictionary               typeDictionary,
		final StorageEntityTypeConversionFileProvider fileProvider
	)
	{
		return New(configuration, typeDictionary, fileProvider, 0);
	}

	public static StorageDataConverterTypeCsvToBinary<AFile> New(
		final StorageDataConverterCsvConfiguration    configuration ,
		final PersistenceTypeDictionary               typeDictionary,
		final StorageEntityTypeConversionFileProvider fileProvider  ,
		final int                                     bufferSize
	)
	{
		return new StorageDataConverterTypeCsvToBinary.Default(
			notNull(configuration) ,
			notNull(typeDictionary),
			notNull(fileProvider)  ,
			bufferSize
		);
	}


	public final class Default
	implements
	StorageDataConverterTypeCsvToBinary<AFile>,
	XCsvSegmentsParser.Provider<_charArrayRange>,
	XCsvSegmentsParser<_charArrayRange>,
	XCsvRecordParserCharArray.Provider,
	XCsvRowCollector,
	XCsvRecordParserCharArray
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

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

		      AFile                                   sourceFile                     ;
		      AWritableFile                           targetFile                     ;
		      long                                    targetFileActualLength         ;
		      PersistenceTypeDefinition               currentType                    ;
		      long                                    currentTypeEntityInitLength    ;
		      ValueHandler[]                          valueHandler                   ;

		/*
		 * current buffer address and value setting has to be done manually because they
		 * do unnecessary gigantic endian byte shuffling overhead on every damn put.
		 */
		long currentBufferAddress ;
		
		// might be replaced by file-inherent config
		XCsvConfiguration actualCsvConfiguation;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final StorageDataConverterCsvConfiguration    configuration ,
			final PersistenceTypeDictionary               typeDictionary,
			final StorageEntityTypeConversionFileProvider fileProvider  ,
			final int                                     bufferSize
		)
		{
			super();
			this.configuration                   = configuration ;
			this.typeDictionary                  = typeDictionary;
			this.fileProvider                    = fileProvider  ;
			
			// the * 2 is important for simplifying the flush check
			this.bufferSize                      = Math.max(bufferSize, 2 * XMemory.defaultBufferSize())      ;
			this.byteBuffer                      = createBuffer(this.bufferSize)                              ;
			this.byteBufferStartAddress          = address(this.byteBuffer)                                   ;
			this.byteBufferFlushBoundAddress     = this.byteBufferStartAddress + XMemory.defaultBufferSize()  ;
			this.simpleValueWriters              = this.deriveSimpleValueWriters(configuration)               ;
			this.theMappingNeverEnds             = this.derivePrimitiveToArrayWriters(this.simpleValueWriters);
			this.literalTrue                     = XChars.readChars(configuration.literalBooleanTrue())       ;
			this.literalFalse                    = XChars.readChars(configuration.literalBooleanFalse())      ;
			this.literalDelimiter                = configuration.csvConfiguration().literalDelimiter()        ;
			this.listStarter                     = configuration.literalListStarter()                         ;
			this.listSeparator                   = configuration.literalListSeparator()                       ;
			this.listTerminator                  = configuration.literalListTerminator()                      ;
			this.terminator                      = configuration.csvConfiguration().terminator()              ;
			this.escaper                         = configuration.csvConfiguration().escaper()                 ;
			this.escapeHandler                   = configuration.csvConfiguration().escapeHandler()           ;
			this.listHeaderUpdateBuffer          = createBuffer((int)Binary.binaryListMinimumLength())        ;
			this.addressListHeaderUpdateBuffer   = address(this.listHeaderUpdateBuffer)                       ;
			this.entityLengthUpdateBuffer        = createBuffer(Binary.lengthLength())                        ;
			this.addressEntityLengthUpdateBuffer = address(this.entityLengthUpdateBuffer)                     ;
			this.objectIdValueHandler            = this.simpleValueWriters.get(long.class.getName())          ;
			this.currentBufferAddress            = this.byteBufferStartAddress                                ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////
		
		private static ByteBuffer createBuffer(final int capacity)
		{
			return XMemory.allocateDirectNative(capacity);
		}
		
		private static long address(final ByteBuffer dbb)
		{
			return XMemory.getDirectByteBufferAddress(dbb);
		}

		final EqConstHashTable<String, ValueHandler>  derivePrimitiveToArrayWriters(
			final EqConstHashTable<String, ValueHandler> valueWriters
		)
		{
			return EqConstHashTable.New(
				X.KeyValue(byte   .class.getName(), valueWriters.get(byte[]   .class.getName())),
				X.KeyValue(boolean.class.getName(), valueWriters.get(boolean[].class.getName())),
				X.KeyValue(short  .class.getName(), valueWriters.get(short[]  .class.getName())),
				X.KeyValue(char   .class.getName(), valueWriters.get(char[]   .class.getName())),
				X.KeyValue(int    .class.getName(), valueWriters.get(int[]    .class.getName())),
				X.KeyValue(float  .class.getName(), valueWriters.get(float[]  .class.getName())),
				X.KeyValue(long   .class.getName(), valueWriters.get(long[]   .class.getName())),
				X.KeyValue(double .class.getName(), valueWriters.get(double[] .class.getName()))
			);
		}

		final EqConstHashTable<String, ValueHandler> deriveSimpleValueWriters(
			final StorageDataConverterCsvConfiguration configuration
		)
		{
			return EqConstHashTable.New(
				X.KeyValue(byte     .class.getName()                    , this::parse_byte        ),
				X.KeyValue(boolean  .class.getName()                    , this::parse_boolean     ),
				X.KeyValue(short    .class.getName()                    , this::parse_short       ),
				X.KeyValue(char     .class.getName()                    , this::parse_char        ),
				X.KeyValue(int      .class.getName()                    , this::parse_int         ),
				X.KeyValue(float    .class.getName()                    , this::parse_float       ),
				X.KeyValue(long     .class.getName()                    , this::parse_long        ),
				X.KeyValue(double   .class.getName()                    , this::parse_double      ),
				X.KeyValue(byte[]   .class.getName()                    , this::parseArray_byte   ),
				X.KeyValue(boolean[].class.getName()                    , this::parseArray_boolean),
				X.KeyValue(short[]  .class.getName()                    , this::parseArray_short  ),
				X.KeyValue(char[]   .class.getName()                    , this::parseChars        ),
				X.KeyValue(int[]    .class.getName()                    , this::parseArray_int    ),
				X.KeyValue(float[]  .class.getName()                    , this::parseArray_float  ),
				X.KeyValue(long[]   .class.getName()                    , this::parseArray_long   ),
				X.KeyValue(double[] .class.getName()                    , this::parseArray_double ),
				X.KeyValue(PersistenceTypeDictionary.Symbols.typeChars(), this::parseChars        ),
				X.KeyValue(PersistenceTypeDictionary.Symbols.typeBytes(), this::parseBytes        )
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
			this.write_byte(XChars.parse_byteDecimal(data, offset, j - offset + 1));
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
			&& XChars.equals(data, offset, this.literalTrue, 0, this.literalTrue.length)
			)
			{
				this.write_boolean(true);
				return i;
			}
			if(literalLength == this.literalFalse.length
			&& XChars.equals(data, offset, this.literalFalse, 0, this.literalFalse.length)
			)
			{
				this.write_boolean(false);
				return i;
			}

			throw new StorageException("Invalid boolean literal: " + String.valueOf(data, offset, literalLength));
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
			this.write_short(XChars.parse_shortDecimal(data, offset, j - offset + 1));
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
				throw new StorageException("Invalid character literal at " + offset);
			}

			// check for special case (escaped character)
			if(data[offset + 1] == this.escaper)
			{
				if(data[offset + ESCAPED_CHAR_SKIP_LENGTH] != this.literalDelimiter)
				{
					throw new StorageException("Unclosed character literal at " + (offset + ESCAPED_CHAR_SKIP_LENGTH));
				}
				this.write_char(this.escapeHandler.unescape(data[offset + SIMPLE_CHAR_SKIP_LENGTH]));
				return offset + DELIMITED_ESCAPED_CHAR_LENGTH;
			}

			// normal case: no escaper
			if(data[offset + SIMPLE_CHAR_SKIP_LENGTH] != this.literalDelimiter)
			{
				throw new StorageException("Unclosed character literal at " + (offset + SIMPLE_CHAR_SKIP_LENGTH));
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
				throw new StorageException("Invalid character literal at " + offset);
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
			this.write_int(XChars.parse_intLiteral(data, offset, j - offset + 1));
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
			this.write_float(XChars.parse_float(data, offset, j - offset + 1));
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
			this.write_long(XChars.parse_longDecimal(data, offset, j - offset + 1));
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
			this.write_double(XChars.parse_double(data, offset, j - offset + 1));
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
				throw new StorageException("Missing literal delimiter " + this.listStarter + " at offset " + offset);
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
					throw new StorageException("incomplete trailing characters at offset " + bound);
				}
				if(data[i] == literalDelimiter)
				{
					break;
				}
				if(data[i] == escaper)
				{
					if(i == lastCharIndex)
					{
						throw new StorageException("incomplete characters literal at offset " + bound);
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
					throw new StorageException("Incomplete hexadecimal string at offset " + i);
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
			throw new StorageException("Invalid hexadecimal character: " + hexDigit);
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
				this.write_byte(XChars.parse_byteDecimal(data, currentElementStart, i - currentElementStart));

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
			// (16.11.2014 TM)TODO: storage csv to bin conversion: maybe single-char boolean array implementation

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
				if(i - elementStart == lengthTrue && XChars.equals(data, elementStart, literalTrue, 0, lengthTrue))
				{
					this.write_boolean(true);
				}
				else if(i - elementStart == lengthFalse
					&& XChars.equals(data, elementStart, literalFalse, 0, lengthFalse)
				)
				{
					this.write_boolean(false);
				}
				else
				{
					throw new StorageException("Invalid boolean literal at offset " + elementStart);
				}
				elementCount++;

				// find literal terminating character (not the same as literal end: there might be white spaces)
				i = seekListElementCompletion(data, i, bound, listSeparator, listTerminator);
			}

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				Binary.toBinaryListTotalByteLength(elementCount * XMemory.byteSize_boolean()),
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
				this.write_short(XChars.parse_shortDecimal(data, currentElementStart, i - currentElementStart));

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
				this.write_int(XChars.parse_intLiteral(data, currentElementStart, i - currentElementStart));

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
				this.write_float(XChars.parse_float(data, currentElementStart, i - currentElementStart));

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
				this.write_long(XChars.parse_longDecimal(data, currentElementStart, i - currentElementStart));

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
				this.write_double(XChars.parse_double(data, currentElementStart, i - currentElementStart));

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
				throw new StorageException("Missing list starter character " + listStarter + " at offset " + offset);
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
			XMemory.set_long(this.currentBufferAddress                         , 0);
			// list element count. None so far.
			XMemory.set_long(this.currentBufferAddress + XMemory.byteSize_long(), 0);
			
			this.currentBufferAddress += 2 * XMemory.byteSize_long();

			return currentFileOffset;
		}

		private void retroUpdateListHeader(final long currentFileOffset, final int bitDivisor)
		{
			final long binaryLength = this.getTargetFileVirtualLength() - currentFileOffset;

			// update list header in binary form
			this.retroUpdateListHeader(
				currentFileOffset,
				binaryLength,
				Binary.toBinaryListContentByteLength(binaryLength) >> bitDivisor
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
					throw new StorageException("incomplete missing separator at offset " + i);
				}
				if(++i >= bound)
				{
					throw new StorageException("incomplete trailing list at offset " + bound);
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
					throw new StorageException("missing value separator at offset " + i);
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
					throw new StorageException("missing list element at offset " + i);
				}
				if(data[i] > ' ')
				{
					break;
				}
				if(++i >= bound)
				{
					throw new StorageException("incomplete trailing list at offset " + bound);
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
					throw new StorageException("Incomplete trailing list at offset " + bound);
				}
			}
			return i;
		}

		private void setTargetFile()
		{
			this.targetFile             = this.fileProvider.provideConversionFile(this.currentType, this.sourceFile);
			this.targetFileActualLength = this.targetFile.size();
		}

		private void setSourceFile(final AFile file)
		{
			this.flushCloseClear();

			// set instance state at the end to guarantee consistency
			this.sourceFile = file;
		}

		final void parseCurrentFile()
		{
			/* (18.09.2018 TM)TODO: unchecked exception really necessary?
			 * Copied from StorageRequestTaskImportData#internalProcessBy:
			 * if it is a normal problem, there should be a proper wrapping exception for it.
			 */
			final char[] input = AFS.readString(this.sourceFile, XChars.utf8()).toCharArray();
						
			final XCsvParserCharArray parser = XCsvParserCharArray.New();
			parser.parseCsvData(this.configuration.csvConfiguration(), _charArrayRange.New(input), this, this);
		}
				
		final void flushCloseClear()
		{
			/* (02.12.2019 TM)NOTE:
			 * Doesn't "really" handling all error cases robustly mean that EVERY call has to be executed
			 * in its own cascade level of try-finally?
			 * If this is done really properly, virtually all code becomes unreadable...
			 */
			
			Throwable suppressed = null;
			try
			{
				// flush buffer to be sure. Unnecessary case gets checked inside
				this.flushBuffer();
			}
			catch(final Throwable t)
			{
				suppressed = t;
				throw t;
			}
			finally
			{
				this.closeAndClear(suppressed);
			}
		}
		
		final void closeAndClear(final Throwable suppressed)
		{
			try
			{
				AFS.close(this.targetFile, suppressed);
			}
			finally
			{
				this.sourceFile            = null;
				this.targetFile            = null;
				this.actualCsvConfiguation = null;
			}
		}

		final void validateTypeNames(final XGettingList<String> dataColumntypes)
		{
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members = this.currentType.instanceMembers();

			if(members.size() != dataColumntypes.size())
			{
				throw new StorageException(
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
						throw new StorageException(
							"CSV reference column type mismatch: " + columnTypeName + " != " + member.typeName()
						);
					}
				}
				else
				{
					final String fieldTypeName = this.configuration.resolveActualTypeName(columnTypeName);
					if(!fieldTypeName.equals(member.typeName()))
					{
						throw new StorageException(
							"CSV non-reference column type mismatch: " + fieldTypeName + " != " + member.typeName()
						);
					}
				}
			}
		}

		final void deriveValueHandlers()
		{
			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members = this.currentType.instanceMembers();
			final ValueHandler[] valueHandlers = new ValueHandler[X.checkArrayRange(members.size()) + 1];
			valueHandlers[0] = this.objectIdValueHandler;
			int i = 1;
			boolean hasVariableLength = false;

			long entityBaseLength = Binary.entityHeaderLength();

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

			if(!(member instanceof PersistenceTypeDescriptionMemberFieldGenericComplex))
			{
				throw new StorageException("Unhandled non-complex persistence field type: " + typeName);
			}

			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members =
				((PersistenceTypeDescriptionMemberFieldGenericComplex)member).members()
			;

			if(members.size() == 1)
			{
				final PersistenceTypeDescriptionMemberFieldGeneric singletonField = members.first();

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
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members
		)
		{
			final ValueHandler[] valueHandlers = new ValueHandler[X.checkArrayRange(members.size())];

			int i = 0;
			for(final PersistenceTypeDescriptionMemberFieldGeneric member : members)
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
					throw new StorageException("Incomplete complex list at offset " + bound);
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
						throw new StorageException("Incomplete complex list at offset " + bound);
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
					throw new StorageException("Incomplete complex list at offset " + bound);
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
					throw new StorageException("Missing list starter character " + listStarter + " at offset " + i);
				}

				// skip found list starter
				i++;

				// each element requires the handler array to be applied
				for(int h = 0; h < handlerIndexBound; h++)
				{
					if(i >= bound)
					{
						throw new StorageException("Incomplete complex list at offset " + bound);
					}
					else if(data[i] == listTerminator)
					{
						throw new StorageException("Incomplete complex list at offset " + i);
					}
					else if(data[i] == listStarter)
					{
						throw new StorageException("Missing complex list element at offset " + i);
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
						throw new StorageException("Incomplete complex list at offset " + bound);
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
				return Default.this.parseComplexListSingleField(data, offset, bound, separator, terminator, this.valueHandler);
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
				return Default.this.parseComplexListMulti(data, offset, bound, separator, terminator, this.valueHandlers);
			}
		}



		final void retroUpdateEntityLength(final long filePosition, final long entityTotalLength)
		{
			if(filePosition - this.targetFileActualLength >= 0)
			{
				// simple case: the position to be updated is still in the buffer, so just set the value in-memory
				XMemory.set_long(this.byteBufferStartAddress + filePosition - this.targetFileActualLength, entityTotalLength);
			}
			else
			{
				// not so simple case: target position was already flushed to the file, hence update there
				XMemory.set_long(this.addressEntityLengthUpdateBuffer, entityTotalLength);
				this.writeBuffer(this.entityLengthUpdateBuffer, filePosition);
			}
		}

		final void retroUpdateListHeader(final long filePosition, final long length, final long elementCount)
		{
			if(filePosition - this.targetFileActualLength >= 0)
			{
				// simple case: the position to be updated is still in the buffer, so just set the values in-memory
				final long offset = filePosition - this.targetFileActualLength;
				XMemory.set_long(this.byteBufferStartAddress + offset, length);
				XMemory.set_long(this.byteBufferStartAddress + offset + XMemory.byteSize_long(), elementCount);
			}
			else
			{
				// not so simple case: target position was already flushed to the file, hence update there
				XMemory.set_long(this.addressListHeaderUpdateBuffer, length);
				XMemory.set_long(this.addressListHeaderUpdateBuffer + XMemory.byteSize_long(), elementCount);
				this.writeBuffer(this.listHeaderUpdateBuffer, filePosition);
			}
		}

		final void writeBuffer(final ByteBuffer byteBuffer, final long filePosition)
		{
			try
			{
				this.targetFile.writeBytes(byteBuffer);
			}
			catch(final Exception e)
			{
				AFS.close(this.targetFile, e);
				throw new StorageException(e);
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
				this.targetFile.writeBytes(this.byteBuffer);
			}
			catch(final Exception e)
			{
				throw new StorageException(e);
			}
			finally
			{
				// exactly 'limit' bytes are guaranteed to have been written.
				this.targetFileActualLength += this.byteBuffer.limit();

				// reset buffer state.
				this.byteBuffer.clear();
				this.currentBufferAddress = this.byteBufferStartAddress;
			}
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
			XMemory.set_byte(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_byte();
		}

		final void write_boolean(final boolean value)
		{
			this.checkForFlush();
			XMemory.set_boolean(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_boolean();
		}

		final void write_short(final short value)
		{
			this.checkForFlush();
			XMemory.set_short(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_short();
		}

		final void write_char(final char value)
		{
			this.checkForFlush();
			XMemory.set_char(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_char();
		}

		final void write_int(final int value)
		{
			this.checkForFlush();
			XMemory.set_int(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_int();
		}

		final void write_float(final float value)
		{
			this.checkForFlush();
			XMemory.set_float(this.currentBufferAddress, value);
			this.currentBufferAddress += Float.BYTES;
		}

		final void write_long(final long value)
		{
			this.checkForFlush();
			XMemory.set_long(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_long();
		}

		final void write_double(final double value)
		{
			this.checkForFlush();
			XMemory.set_double(this.currentBufferAddress, value);
			this.currentBufferAddress += XMemory.byteSize_double();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void convertCsv(final AFile file)
		{
			this.setSourceFile(file);
			this.parseCurrentFile();
			this.flushCloseClear();
		}
		
		static final long getTypeIdFromFileName(final AFile file)
		{
			final String fileName = file.name();
			final long   typeId   = StorageEntityTypeExportFileProvider.getTypeIdFromUniqueTypeFileName(fileName);
			
			return typeId;
		}
		
		@Override
		public void beginTable(
			final String                   tableName  ,
			final XGettingSequence<String> columnNames,
			final XGettingList<String>     columnTypes
		)
		{
			// (20.02.2020 TM)NOTE: with the typeId being added to the file name, it must be parsed away, now
			final long typeId = getTypeIdFromFileName(this.sourceFile);

			
			// (20.02.2020 TM)NOTE: lookup by typeId instead of by type name.
			if((this.currentType = this.typeDictionary.lookupTypeById(typeId)) == null)
			{
				throw new StorageException("Type not found: " + this.sourceFile.identifier());
			}

			final String firstColumnName = columnNames.first();
			if(!this.configuration.objectIdColumnName().equals(firstColumnName))
			{
				throw new StorageException(
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
		public XCsvSegmentsParser<_charArrayRange> provideSegmentsParser(
			final XCsvConfiguration config       ,
			final XCsvRowCollector  rowAggregator
		)
		{
			this.actualCsvConfiguation = config;
			return this; // implements ALL the interfaces!
		}

		@Override
		public void parseSegments(final _charArrayRange input)
		{
			XCsvParserCharArray.parseSegments(
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
		public XCsvRecordParserCharArray provideRecordParser()
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
			final char                lineSeparator,
			final char                terminator     ,
			final XCsvConfiguration   config         ,
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
					throw new StorageException("Incomplete record at index " + i);
				}
				else if(input[i] == valueSeparator || input[i] == lineSeparator || input[i] == terminator)
				{
					// encountered the next separator while searching for a literal, interpret as error
					/* there are no null values in storage CSV files, as NULL reference is OID literal "0"
					 * and everything else are just primitives anyway.
					 */
					throw new StorageException("Missing value at index " + i);
				}
				else if(input[i] <= ' ')
				{
					i++; // skip unimportant whitespace
				}
				else
				{
					i = valueHandlers[h].handleValue(input, i, iBound, valueSeparator, lineSeparator);
					if(i >= iBound)
					{
						// check if valid end
						if(h < handlerCount - 1)
						{
							throw new StorageException("Missing record value at index " + i);
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
