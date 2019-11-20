package one.microstream.storage.types;

import static one.microstream.X.notNull;
import static one.microstream.chars.MemoryCharConversionUTF8.toSingleByte;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;

import one.microstream.X;
import one.microstream.chars.CharConversion_float;
import one.microstream.chars.EscapeHandler;
import one.microstream.chars.MemoryCharConversionIntegersUTF8;
import one.microstream.chars.MemoryCharConversionUTF8;
import one.microstream.chars.MemoryCharConversion_doubleUTF8;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.LimitList;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.files.FileException;
import one.microstream.files.XFiles;
import one.microstream.io.XIO;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.storage.exceptions.StorageException;
import one.microstream.typing.XTypes;
import one.microstream.util.csv.CsvConfiguration;


public interface StorageDataConverterTypeBinaryToCsv
{
	public void convertDataFile(StorageLockedFile file);



	public interface TypeNameMapper
	{
		public String mapTypeName(PersistenceTypeDescriptionMember columnType);
	}

	public static TypeNameMapper defaultTypeNameMapper(
		final XGettingMap<String, String> typeNameToCsvTypeNameMapping,
		final String                      referenceTypeName
	)
	{
		return new TypeNameMapper()
		{
			@Override
			public String mapTypeName(final PersistenceTypeDescriptionMember columnType)
			{
				if(columnType.isReference())
				{
					return referenceTypeName;
				}
				final String mappedTypeName = typeNameToCsvTypeNameMapping.get(columnType.typeName());
				if(mappedTypeName == null)
				{
					// (27.01.2014 TM)EXCP: proper exception
					throw new RuntimeException("Unmapped type: " + columnType.typeName());
				}
				return mappedTypeName;
			}
		};
	}



	public final class UTF8
	implements
	StorageDataConverterTypeBinaryToCsv,
	StorageDataFileItemIterator.BufferProvider,
	StorageDataFileItemIterator.ItemProcessor
	{
		// CHECKSTYLE.OFF: MagicNumber: very low-level technical implementation. Magic numbers kept for now.

		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////

		// +1 char for value separator, *2 as each character required 2 bytes
		static final transient int
			FLUSH_BUFFER_RANGE     = 100, // 100 should be enough for any single value (max is 25 chars for double)

			STRING_BYTE_SIZE_CHAR         =     XMemory.byteSize_char(), // a char always occupies 2 bytes in memory
			LITERAL_BYTE_SIZE_SINGLE_CHAR = 1 * XMemory.byteSize_byte(), // a UTF-8 single chars occupies only 1 byte
			LITERAL_BYTE_SIZE_HEXDEC_BYTE = 2 * XMemory.byteSize_byte()  // a byte literal 2 single UTF chars, e.g. "FF".
		;

		static final transient short[] BYTE_MAP = new short[0x100];
		static
		{
			/* one of the few places where a "direct endian" strategy cannot be applied but a little/big endian
			 * distinction has to be made. This is due to the optimization to pack two chars into one int value.
			 */
			if(ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
			{
				for(int i = 0; i < BYTE_MAP.length; i++)
				{
					BYTE_MAP[i] = (short)(toHexadecimal((i & 0xF0) >> 4) + (toHexadecimal(i & 0xF) << 8));
				}
			}
			else
			{
				for(int i = 0; i < BYTE_MAP.length; i++)
				{
					BYTE_MAP[i] = (short)((toHexadecimal((i & 0xF0) >> 4) << 8) + toHexadecimal(i & 0xF));
				}
			}
		}

		public static final byte toHexadecimal(final int b) throws IllegalArgumentException
		{
			switch(b)
			{
				case  0: return (byte)'0';
				case  1: return (byte)'1';
				case  2: return (byte)'2';
				case  3: return (byte)'3';
				case  4: return (byte)'4';
				case  5: return (byte)'5';
				case  6: return (byte)'6';
				case  7: return (byte)'7';
				case  8: return (byte)'8';
				case  9: return (byte)'9';
				case 10: return (byte)'A';
				case 11: return (byte)'B';
				case 12: return (byte)'C';
				case 13: return (byte)'D';
				case 14: return (byte)'E';
				case 15: return (byte)'F';
				default: throw new IllegalArgumentException(b + " is no positive hexadecimal digit value");
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final FileChannel createFileChannel(final File file) throws StorageException
		{
			try
			{
				return XFiles.createWritingFileChannel(file);
			}
			catch(FileException | IOException e)
			{
				throw new StorageException(e);
			}
		}



		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		// caching fields for performance reasons (skipping repeated pointer indirection)
		private final String                                  oidColumnName         ;
		private final String                                  oidColumnType         ;
		private final CsvConfiguration                        csvConfiguration      ;
		private final byte                                    literalDelimiter      ;
		private final byte                                    valueSeparator        ;
		private final byte                                    recordSeparator       ;
		private final byte                                    escaper               ;
		private final EscapeHandler                           escapeHandler         ;
		private final char                                    controlCharsSeparator ;
		private final TypeNameMapper                          typeNameMapper        ;
		private final byte                                    listStarter           ;
		private final byte                                    listTerminator        ;
		private final byte                                    listSeparator         ;

		private final byte[]                                  literalTrue           ;
		private final byte[]                                  literalFalse          ;
		private final int                                     literalByteLengthTrue ;
		private final int                                     literalByteLengthFalse;

		// workaround helper buffer. See use site comments for explanation
		private final char[]                                  decimalBuffer = new char[XChars.maxCharCount_double()];

		private final StorageEntityTypeConversionFileProvider fileProvider          ;
		private final PersistenceTypeDictionary               typeDictionary        ;
		private       StorageLockedFile                       currentSourceFile     ;

		private final int                                     readBufferSize        ;
		private final ByteBuffer                              readBufferNormal      ;
		private       ByteBuffer                              readBufferLarge       ;

		private final int                                     writeBufferSize       ;
		private final ByteBuffer                              writeBuffer           ;
		private final long                                    writeStart            ;
		private final long                                    writeBound            ;
		private final long                                    flushBound            ;
		private       long                                    writeAddress          ;

		private final EqConstHashTable<String, ValueWriter>   valueWriterMap  = this.initializeValueWriterMapping();
		private final ValueWriter                             valueWriterRef  = this.createValueWriterReference();

		private       long                                    typeId          =   -1;
		private       PersistenceTypeDefinition           typeDescription;
		private       ValueWriter[]                           valueWriters   ;
		private       FileChannel                             fileChannel    ;

		private EqConstHashTable<String, ValueWriter> initializeValueWriterMapping()
		{
			final EqHashTable<String, ValueWriter> map = EqHashTable.New();
			map.add(byte   .class.getName()                      , this.createValueWriter_byte()   );
			map.add(boolean.class.getName()                      , this.createValueWriter_boolean());
			map.add(short  .class.getName()                      , this.createValueWriter_short()  );
			map.add(char   .class.getName()                      , this.createValueWriter_char()   );
			map.add(int    .class.getName()                      , this.createValueWriter_int()    );
			map.add(float  .class.getName()                      , this.createValueWriter_float()  );
			map.add(long   .class.getName()                      , this.createValueWriter_long()   );
			map.add(double .class.getName()                      , this.createValueWriter_double() );
			map.add(PersistenceTypeDictionary.Symbols.typeChars(), this.createValueWriter_chars()  );
			map.add(PersistenceTypeDictionary.Symbols.typeBytes(), this.createValueWriter_bytes()  );
			// note: [list] cannot be registered here as it is composed dynamically complex
			return map.immure();
		}

		private static int writeBufferSize(final int writeBufferSize)
		{
			return Math.max(
				(writeBufferSize & ~3) == writeBufferSize ? writeBufferSize : (writeBufferSize & ~3) + 4,
				XMemory.defaultBufferSize()
			);
		}


		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public UTF8(
			final StorageDataConverterCsvConfiguration    configuration  ,
			final StorageEntityTypeConversionFileProvider fileProvider   ,
			final PersistenceTypeDictionary               typeDictionary ,
			final TypeNameMapper                          typeNameMapper ,
			final int                                     readBufferSize ,
			final int                                     writeBufferSize
		)
		{
			super();

			final CsvConfiguration csvConfig = configuration.csvConfiguration();

			// (26.01.2014)EXCP: proper exceptions
			if(csvConfig.isControlCharacter(configuration.literalListStarter()))
			{
				throw new IllegalArgumentException("Conflicting list character: " + configuration.literalListStarter());
			}
			if(csvConfig.isControlCharacter(configuration.literalListTerminator()))
			{
				throw new IllegalArgumentException("Conflicting list character: " + configuration.literalListTerminator());
			}
			if(csvConfig.isControlCharacter(configuration.literalListSeparator()))
			{
				throw new IllegalArgumentException("Conflicting list character: " + configuration.literalListSeparator());
			}

			this.fileProvider            = notNull(fileProvider)                              ;
			this.typeDictionary          = notNull(typeDictionary)                            ;
			this.csvConfiguration        = csvConfig                                          ;
			this.literalDelimiter        = toSingleByte(csvConfig.literalDelimiter())         ;
			this.valueSeparator          = toSingleByte(csvConfig.valueSeparator()  )         ;
			this.recordSeparator         = toSingleByte(csvConfig.recordSeparator() )         ;
			this.escaper                 = toSingleByte(csvConfig.escaper()         )         ;
			this.escapeHandler           = csvConfig.escapeHandler()                          ;
			this.oidColumnName           = configuration.objectIdColumnName()                 ;
			this.oidColumnType           = configuration.objectIdColumnTypeName()             ;
			this.controlCharsSeparator   = configuration.controlCharacterSeparator();
			this.typeNameMapper          = typeNameMapper != null
				? typeNameMapper
				: StorageDataConverterTypeBinaryToCsv.defaultTypeNameMapper(
					configuration.typeNameToCsvTypeNameMapping(),
					configuration.referenceTypeName()
				)
			;
			this.literalTrue             = MemoryCharConversionUTF8.toUTF8(XChars.readChars(configuration.literalBooleanTrue())) ;
			this.literalFalse            = MemoryCharConversionUTF8.toUTF8(XChars.readChars(configuration.literalBooleanFalse()));
			this.listStarter             = toSingleByte(configuration.literalListStarter()   );
			this.listTerminator          = toSingleByte(configuration.literalListTerminator());
			this.listSeparator           = toSingleByte(configuration.literalListSeparator() );
			this.literalByteLengthTrue   = this.literalTrue.length                            ;
			this.literalByteLengthFalse  = this.literalFalse.length                           ;

			this.readBufferSize          = Math.max(readBufferSize, XMemory.defaultBufferSize());
			this.readBufferNormal        = XMemory.allocateDirectNative(this.readBufferSize);
			this.readBufferLarge         = XMemory.allocateDirectNative(0); // don't squander memory if normal size is already huge

			this.writeBufferSize         = writeBufferSize(writeBufferSize);
			this.writeBuffer             = XMemory.allocateDirectNative(this.writeBufferSize)   ;
			this.writeStart              = XMemory.getDirectByteBufferAddress(this.writeBuffer);
			this.writeBound              = this.writeAddress + this.writeBuffer.capacity()     ;
			this.flushBound              = this.writeBound - FLUSH_BUFFER_RANGE                ;
			this.writeAddress            = this.writeStart                                     ;
		}



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		private ValueWriter[] createValueWriters(final XGettingSequence<? extends PersistenceTypeDescriptionMember> members)
		{
//			final LimitList<ValueWriter> simpleValueWriters = new LimitList<>(XTypes.to_int(members.size()));
//			final LimitList<ValueWriter> otherValueWriters  = new LimitList<>(XTypes.to_int(members.size()));

			final int memberCount = XTypes.to_int(members.size());

			final ValueWriter[] simpleValueWriters = new ValueWriter[memberCount];
			final ValueWriter[] otherValueWriters = new ValueWriter[memberCount];

			int s = 0;
			int o = 0;

			// must reproduce the simple reference ordering logic
			for(final PersistenceTypeDescriptionMember field : members)
			{
				if(field.isReference())
				{
//					simpleValueWriters.add(this.valueWriterRef);
					simpleValueWriters[s++] = this.valueWriterRef;
				}
				else
				{
//					otherValueWriters.add(this.deriveOtherValueWriter(field));
					otherValueWriters[o++] = this.deriveOtherValueWriter(field);
				}
			}

			// compile value writers in order (referencs are always arranged to come first for more memory-efficient GC)
			final ValueWriter[] valueWriters = new ValueWriter[memberCount];
			System.arraycopy(simpleValueWriters, 0, valueWriters, 0, s);
			System.arraycopy(otherValueWriters , 0, valueWriters, s, o);

//			simpleValueWriters.copyTo(valueWriters, 0);
//			otherValueWriters.copyTo(valueWriters, XTypes.to_int(simpleValueWriters.size()));
			return valueWriters;
		}

		private void openChannel() throws IOException
		{
			final StorageLockedFile file = this.fileProvider.provideConversionFile(this.typeDescription, this.currentSourceFile);
			final File directory = new File(file.qualifier());
			XFiles.ensureDirectory(directory);
			this.fileChannel = file.fileChannel();
		}

		private ValueWriter deriveOtherValueWriter(final PersistenceTypeDescriptionMember field)
		{
			// check for reference field must already happened before

			// naste instanceof, but well
			if(field instanceof PersistenceTypeDescriptionMemberFieldGenericComplex)
			{
				return this.deriveComplexValueWriter((PersistenceTypeDescriptionMemberFieldGenericComplex)field);
			}
			final ValueWriter valueWriter = this.valueWriterMap.get(field.typeName());
			if(valueWriter == null)
			{
				// (14.01.2014)EXCP: proper exception
				throw new RuntimeException("Unrecognized type: " + field.typeName());
			}
			return valueWriter;
		}

		private ValueWriter deriveComplexValueWriter(final PersistenceTypeDescriptionMemberFieldGenericComplex field)
		{
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members = field.members();

			if(XTypes.to_int(members.size()) == 1)
			{
				/* char array gets written as a String because anything else would be unnecessary overhead
				 * in both bytes and CSV parsing logic.
				 */
				if(members.first().typeName().equals(char.class.getName()))
				{
					return this.createValueWriter_chars();
				}

				final ValueWriter valueWriter = members.first().isReference()
					? this.valueWriterRef
					: this.deriveOtherValueWriter(members.first())
				;
				return new ValueWriter()
				{
					@Override
					public long writeValue(final long valueReadAddress) throws IOException
					{
						return UTF8.this.writeComplexSingle(valueWriter, valueReadAddress);
					}
				};
			}

			final ValueWriter[] valueWriters = this.createValueWriters(members);
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					return UTF8.this.writeComplexMultiple(valueWriters, valueReadAddress);
				}
			};
		}

		private void processEntity(final long entityAddress) throws IOException
		{
			this.checkType(Binary.getEntityTypeIdRawValue(entityAddress));

			final byte valueSeparator = this.valueSeparator;

			// this check covers the following three writes
			this.checkForFlush();

			// write record separator not before it is required a by new record (this method call)
			XMemory.set_byte(this.writeAddress, this.recordSeparator);
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_long(
				Binary.getEntityObjectIdRawValue(entityAddress),
				this.writeAddress + 1
			);
			XMemory.set_byte(this.writeAddress, valueSeparator);
			this.writeAddress += LITERAL_BYTE_SIZE_SINGLE_CHAR;

			long address = Binary.toEntityContentOffset(entityAddress);
			for(final ValueWriter writer : this.valueWriters)
			{
				address = writer.writeValue(address);
				this.write(valueSeparator);
			}

			// remove last value separator (either behind last value or behind oid value)
			this.writeAddress -= LITERAL_BYTE_SIZE_SINGLE_CHAR;
		}

		private void checkType(final long typeId) throws IOException
		{
			if(this.typeId < 0)
			{
				if((this.typeDescription = this.typeDictionary.lookupTypeById(typeId)) == null)
				{
					// (12.01.2014)EXCP: proper exception
					throw new RuntimeException("Unknown TypeId: " + typeId);
				}
				this.typeId = typeId;
				this.valueWriters = this.createValueWriters(this.typeDescription.instanceMembers());
				this.openChannel();
				this.writeCsvHeader();
			}
			else if(typeId != this.typeId)
			{
				// (12.01.2014)EXCP: proper exception
				throw new RuntimeException("Inconsistent TypeId: " + typeId + " != " + this.typeId);
			}
		}

		private void writeCsvHeader() throws IOException
		{
			final VarString vs = VarString.New();

			final char valueSeparator = (char)this.valueSeparator; // tiny detour due to byte type.

			// write control characters definition
			vs
			.add(this.csvConfiguration.buildControlCharactersDefinition(this.controlCharsSeparator)).lf()
			.add(this.oidColumnName).add(valueSeparator)
			;

			final XGettingSequence<? extends PersistenceTypeDescriptionMember> members =
				this.typeDescription.instanceMembers()
			;
			final LimitList<String> refColumnNames = new LimitList<>(XTypes.to_int(members.size()));
			final LimitList<String> prmColumnNames = new LimitList<>(XTypes.to_int(members.size()));
			final LimitList<String> refColumnTypes = new LimitList<>(XTypes.to_int(members.size()));
			final LimitList<String> prmColumnTypes = new LimitList<>(XTypes.to_int(members.size()));

			// write column names (including oid column with custom name)
			for(final PersistenceTypeDescriptionMember column : members)
			{
				if(column.isReference())
				{
					refColumnNames.add(column.name());
					refColumnTypes.add(this.typeNameMapper.mapTypeName(column));
				}
				else
				{
					prmColumnNames.add(column.name());
					prmColumnTypes.add(this.typeNameMapper.mapTypeName(column));
				}
			}
			for(final String columnName : refColumnNames)
			{
				vs.add(columnName).add(valueSeparator);
			}
			for(final String columnName : prmColumnNames)
			{
				vs.add(columnName).add(valueSeparator);
			}
			vs.setLast((char)this.recordSeparator);

			// write column types (including oid column with custom type name)
			vs
			.add(this.csvConfiguration.headerStarter())
			.add(this.oidColumnType).add(valueSeparator);

			for(final String columnType : refColumnTypes)
			{
				vs.add(columnType).add(valueSeparator);
			}
			for(final String columnType : prmColumnTypes)
			{
				vs.add(columnType).add(valueSeparator);
			}

			// do not write a record separator here as every record will prepend one when required
			vs.setLast(this.csvConfiguration.headerTerminator());

			// copy header chars to writing memory. Copying redundancy is not an issue for a tiny one-time header.
			if(this.writeAddress + vs.length() * MemoryCharConversionUTF8.maxCharacterLength() > this.writeBound)
			{
				this.flushWriteBuffer();
			}
			this.writeAddress = MemoryCharConversionUTF8.writeUTF8(this.writeAddress, vs);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final void convertDataFile(final StorageLockedFile file)
		{
			if(file.length() == 0)
			{
				return;
			}

			// (13.10.2018 TM)FIXME: replace autoclose by external closing
			try(SeekableByteChannel inputChannel = (this.currentSourceFile = file).fileChannel())
			{
				try
				{
					StorageDataFileItemIterator.Default.processInputFile(inputChannel, this, this, 0, inputChannel.size());
					this.flushWriteBuffer(); // make sure to flush the buffer at the end to write all
				}
				catch(final WriteException e)
				{
					throw new RuntimeException(e.ioException);
				}
				finally
				{
					this.reset();
				}
			}
			catch(final IOException e)
			{
				// well, what to do if closing fails...
				// (12.01.2014)EXCP: proper exception
				throw new RuntimeException(e);
			}
			finally
			{
				this.currentSourceFile = null;
			}
		}

		private void flushWriteBuffer() throws IOException
		{
			this.writeAddress = this.flushWriteBuffer(this.writeAddress);
		}

		private long flushWriteBuffer(final long writeAddress) throws IOException
		{
			// check for no-op to avoid unnecessary IO effort
			if(writeAddress == this.writeStart)
			{
				return this.writeAddress;
			}

			this.writeBuffer.limit(X.checkArrayRange(writeAddress - this.writeStart));
			while(this.writeBuffer.hasRemaining())
			{
				this.fileChannel.write(this.writeBuffer);
			}
			this.writeBuffer.clear();
			return this.writeStart;
		}

		private void reset() throws IOException
		{
			XIO.closeSilent(this.fileChannel);
			this.typeId       =   -1;
			this.typeDescription  = null;
			this.valueWriters = null;
			this.fileChannel  = null;
		}

		private void checkForFlush() throws IOException
		{
			if(this.writeAddress >= this.flushBound)
			{
				this.flushWriteBuffer();
			}
		}

		final void write(final byte value) throws IOException
		{
			this.checkForFlush();
			XMemory.set_byte(this.writeAddress++, value);
		}

		final void write_byte(final byte value) throws IOException
		{
			this.checkForFlush();
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_byte(value, this.writeAddress);
		}

		final void write_boolean(final boolean value) throws IOException
		{
			this.checkForFlush();

			if(value)
			{
				XMemory.copyArrayToAddress(this.literalTrue, this.writeAddress);
				this.writeAddress += this.literalByteLengthTrue;
			}
			else
			{
				XMemory.copyArrayToAddress(this.literalFalse, this.writeAddress);
				this.writeAddress += this.literalByteLengthFalse;
			}
		}

		final void write_short(final short value) throws IOException
		{
			this.checkForFlush();
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_short(value, this.writeAddress);
		}

		final void write_char(final char c) throws IOException
		{
			this.checkForFlush();

			XMemory.set_byte(this.writeAddress, this.literalDelimiter);
			if(c == this.literalDelimiter || c == this.escaper || this.escapeHandler.needsEscaping(c))
			{
				// escaping case: write escaper, advance address, then handle the actual character
				XMemory.set_byte(this.writeAddress + LITERAL_BYTE_SIZE_SINGLE_CHAR, this.escaper);
				this.writeAddress = MemoryCharConversionUTF8.writeUTF8(
					this.writeAddress + 2 * LITERAL_BYTE_SIZE_SINGLE_CHAR,
					this.escapeHandler.transformEscapedChar(c)
				) + LITERAL_BYTE_SIZE_SINGLE_CHAR;
			}
			else
			{
				this.writeAddress = MemoryCharConversionUTF8.writeUTF8(
					this.writeAddress + LITERAL_BYTE_SIZE_SINGLE_CHAR,
					c
				) + LITERAL_BYTE_SIZE_SINGLE_CHAR;
			}
			XMemory.set_byte(this.writeAddress - LITERAL_BYTE_SIZE_SINGLE_CHAR, this.literalDelimiter);
		}

		final void write_int(final int value) throws IOException
		{
			this.checkForFlush();
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_int(value, this.writeAddress);
		}

		final void write_long(final long value) throws IOException
		{
			this.checkForFlush();
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_long(value, this.writeAddress);
		}

		final void writeReference(final long value) throws IOException
		{
			this.checkForFlush();

			if(value == Persistence.nullId())
			{
				// a reference is merely a primitive id long, so null is the numerical literal '0'
				XMemory.set_byte(this.writeAddress++, (byte)'0');
				return;
			}
			this.writeAddress = MemoryCharConversionIntegersUTF8.put_long(value, this.writeAddress);
		}

		final long writeComplexMultiple(final ValueWriter[] valueWriters, final long valueReadAddress)
			throws IOException
		{
			final long elementCount  = XMemory.get_long(Binary.toBinaryListElementCountOffset(valueReadAddress));
			      long address       = Binary.toBinaryListElementsOffset(valueReadAddress);
			final byte listStarter   = this.listStarter;
			final byte listSeparator = this.listSeparator;

			this.write(listStarter);
			for(long a = 0; a < elementCount; a++)
			{
				this.write(listStarter);
				for(final ValueWriter valueWriter : valueWriters)
				{
					address = valueWriter.writeValue(address);
					this.write(listSeparator);
				}
				XMemory.set_byte(this.writeAddress - LITERAL_BYTE_SIZE_SINGLE_CHAR, this.listTerminator);
				this.write(listSeparator);
			}
			this.closeComplexLiteral(elementCount);

			return valueReadAddress + Binary.getEntityLengthRawValue(valueReadAddress);
		}

		final long writeComplexSingle(final ValueWriter valueWriter, final long valueReadAddress)
			throws IOException
		{
			final long elementCount = XMemory.get_long(Binary.toBinaryListElementCountOffset(valueReadAddress));
			      long address      = Binary.toBinaryListElementsOffset(valueReadAddress);
			final byte listSeparator = this.listSeparator;

			this.write(this.listStarter);
			for(long a = 0; a < elementCount; a++)
			{
				address = valueWriter.writeValue(address);
				this.write(listSeparator);
			}
			this.closeComplexLiteral(elementCount);

			return valueReadAddress + Binary.getEntityLengthRawValue(valueReadAddress);
		}

		private void closeComplexLiteral(final long elementCount) throws IOException
		{
			if(elementCount > 0)
			{
				XMemory.set_byte(this.writeAddress - LITERAL_BYTE_SIZE_SINGLE_CHAR, this.listTerminator);
			}
			else
			{
				this.write(this.listTerminator);
			}
		}

		final void write_float(final float value) throws IOException
		{
			/* Lazy redundant copying effort. Could be optimized by tailored implementation similar to double.
			 */
			final int length = CharConversion_float.put(value, this.decimalBuffer, 0);

			this.checkForFlush();
			this.writeAddress = MemoryCharConversionUTF8.writeUTF8(this.writeAddress, this.decimalBuffer, 0, length);
		}

		final void write_double(final double value) throws IOException
		{
			this.checkForFlush();
			this.writeAddress = MemoryCharConversion_doubleUTF8.put(value, this.writeAddress);
		}


		final void write_chars(final long readStart, final long readBound) throws IOException
		{
			// ensure one char size for the closing literal delimiter
			final long          flushBound       = this.flushBound      ;
			final byte          literalDelimiter = this.literalDelimiter;
			final byte          escaper          = this.escaper         ;
			final EscapeHandler escapeHandler    = this.escapeHandler   ;

			this.write(literalDelimiter);
			long address = this.writeAddress;
			for(long readAddress = readStart; readAddress < readBound; readAddress += STRING_BYTE_SIZE_CHAR)
			{
				final char c = XMemory.get_char(readAddress);
				if(c == literalDelimiter || c == escaper || escapeHandler.needsEscaping(c))
				{
					// escaping case: write escaper, advance address, then handle the actual character
					XMemory.set_byte(address, escaper);
					address = MemoryCharConversionUTF8.writeUTF8(
						address + LITERAL_BYTE_SIZE_SINGLE_CHAR,
						escapeHandler.transformEscapedChar(c)
					);
				}
				else
				{
					// normal case: character does not have to be escaped, just write it
					address = MemoryCharConversionUTF8.writeUTF8(address, c);
				}

				// check for flush
				if(address >= flushBound)
				{
					address = this.flushWriteBuffer(address);
				}
			}

			XMemory.set_byte(address, literalDelimiter);
			this.writeAddress = address + LITERAL_BYTE_SIZE_SINGLE_CHAR;
		}

		final void write_bytes(final long readStart, final long readBound) throws IOException
		{
			// ensure one char size for the closing literal delimiter
			final long flushBound = this.flushBound;
			this.checkForFlush();
			long address = this.writeAddress;

			for(long readAddress = readStart; readAddress < readBound; readAddress++)
			{
				final byte value = XMemory.get_byte(readAddress);
				XMemory.set_short(address, BYTE_MAP[value >= 0 ? value : 256 + value]);
				if((address += LITERAL_BYTE_SIZE_HEXDEC_BYTE) >= flushBound)
				{
					address = this.flushWriteBuffer(address);
				}
			}

			// update instance state with final write address
			this.writeAddress = address;
		}

		final ValueWriter createValueWriter_byte()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_byte(XMemory.get_byte(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_byte();
				}
			};
		}

		final ValueWriter createValueWriter_boolean()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_boolean(XMemory.get_boolean(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_boolean();
				}
			};
		}

		final ValueWriter createValueWriter_short()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_short(XMemory.get_short(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_short();
				}
			};
		}

		final ValueWriter createValueWriter_char()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_char(XMemory.get_char(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_char();
				}
			};
		}

		final ValueWriter createValueWriter_int()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_int(XMemory.get_int(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_int();
				}
			};
		}

		final ValueWriter createValueWriter_float()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_float(XMemory.get_float(valueReadAddress));
					return valueReadAddress + Float.BYTES;
				}
			};
		}

		final ValueWriter createValueWriter_long()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_long(XMemory.get_long(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_long();
				}
			};
		}

		final ValueWriter createValueWriter_double()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.write_double(XMemory.get_double(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_double();
				}
			};
		}

		final ValueWriter createValueWriterReference()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					UTF8.this.writeReference(XMemory.get_long(valueReadAddress));
					return valueReadAddress + XMemory.byteSize_long();
				}
			};
		}

		final ValueWriter createValueWriter_chars()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					final long bound = valueReadAddress + XMemory.get_long(Binary.toBinaryListByteLengthOffset(valueReadAddress));
					
					UTF8.this.write_chars(
						Binary.toBinaryListElementsOffset(valueReadAddress),
						bound
					);
					
					return bound;
				}
			};
		}

		final ValueWriter createValueWriter_bytes()
		{
			return new ValueWriter()
			{
				@Override
				public long writeValue(final long valueReadAddress) throws IOException
				{
					final long bound = valueReadAddress + XMemory.get_long(Binary.toBinaryListByteLengthOffset(valueReadAddress));
					
					UTF8.this.write_bytes(
						Binary.toBinaryListElementsOffset(valueReadAddress),
						bound
					);
					
					return bound;
				}
			};
		}

		interface ValueWriter
		{
			public long writeValue(long valueReadAddress) throws IOException;
		}

		@Override
		public boolean accept(final long entityAddress, final long availableEntityLength)
		{
			// check for gap and skip (report success/advance without taking any action)
			if(Binary.getEntityLengthRawValue(entityAddress) < 0)
			{
				return true;
			}

			// check for incomplete entity data and report failure/reload.
			if(availableEntityLength < Binary.getEntityLengthRawValue(entityAddress))
			{
				return false;
			}

			try
			{
				this.processEntity(entityAddress);
				return true;
			}
			catch(final IOException e)
			{
				throw new WriteException(e);
			}
		}


		@Override
		public final ByteBuffer provideInitialBuffer()
		{
			return this.readBufferNormal;
		}

		@Override
		public ByteBuffer provideBuffer(final ByteBuffer byteBuffer, final long nextEntityLength)
		{
			if(nextEntityLength < this.readBufferNormal.capacity())
			{
				// intentionally no limiting to read more than one entity if there is enough space
				this.readBufferNormal.clear();
				// use standard buffer
				return this.readBufferNormal;
			}
			if(nextEntityLength <= this.readBufferLarge.capacity())
			{
				this.readBufferLarge.clear().limit((int)nextEntityLength);
				// large buffer already large enough
				return this.readBufferLarge;
			}

			// large buffer has to be enlarged
			XMemory.deallocateDirectByteBuffer(this.readBufferLarge);
			return this.readBufferLarge = XMemory.allocateDirectNative(nextEntityLength);
		}

		static final class WriteException extends RuntimeException
		{
			final IOException ioException;

			public WriteException(final IOException ioException)
			{
				super();
				this.ioException = ioException;
			}

		}

		// CHECKSTYLE.ON: MagicNumber
	}

	/* (10.12.2014 TM)TODO: Storage DataFile csv converter
	 * A means is required to directly read a data file.
	 * Something like:
---------------------
"channel1_1@1234:one.microstream.collections.BulkList"
{
length  tid    oid    capacity   elements
(long   long   long   long       complex)
...
...
...
}
"channel1_1@2345:java.math.BigInteger"
{
length  tid    oid    value
(long   long   long   binary)
...
}
---------------------
No matter the type overhead for single entities, readability is important.
No position column as that would be redundant editing effort in case of manual adjustments
	 */

}
