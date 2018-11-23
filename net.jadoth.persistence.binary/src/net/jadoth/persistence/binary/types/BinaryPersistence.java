package net.jadoth.persistence.binary.types;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.collections.BinaryHandlerBulkList;
import net.jadoth.collections.BinaryHandlerConstHashEnum;
import net.jadoth.collections.BinaryHandlerConstHashTable;
import net.jadoth.collections.BinaryHandlerConstList;
import net.jadoth.collections.BinaryHandlerEqBulkList;
import net.jadoth.collections.BinaryHandlerEqConstHashEnum;
import net.jadoth.collections.BinaryHandlerEqConstHashTable;
import net.jadoth.collections.BinaryHandlerEqHashEnum;
import net.jadoth.collections.BinaryHandlerEqHashTable;
import net.jadoth.collections.BinaryHandlerFixedList;
import net.jadoth.collections.BinaryHandlerHashEnum;
import net.jadoth.collections.BinaryHandlerHashTable;
import net.jadoth.collections.BinaryHandlerLimitList;
import net.jadoth.collections.ConstList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.exceptions.InstantiationRuntimeException;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional.InstanceDispatcherLogic;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import net.jadoth.persistence.binary.internal.BinaryHandlerArrayList;
import net.jadoth.persistence.binary.internal.BinaryHandlerBigDecimal;
import net.jadoth.persistence.binary.internal.BinaryHandlerBigInteger;
import net.jadoth.persistence.binary.internal.BinaryHandlerDate;
import net.jadoth.persistence.binary.internal.BinaryHandlerFile;
import net.jadoth.persistence.binary.internal.BinaryHandlerHashSet;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_boolean;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_byte;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_char;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_double;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_float;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_int;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_long;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeArray_short;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeBoolean;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeByte;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeCharacter;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeDouble;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeFloat;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeInteger;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeLong;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeObject;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeShort;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeString;
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeVoid;
import net.jadoth.persistence.binary.internal.BinaryHandlerPrimitive;
import net.jadoth.persistence.binary.internal.BinaryHandlerStringBuffer;
import net.jadoth.persistence.binary.internal.BinaryHandlerStringBuilder;
import net.jadoth.persistence.internal.PersistenceTypeDictionaryFileHandler;
import net.jadoth.persistence.types.BinaryHandlerLazyReference;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceHandler;
import net.jadoth.persistence.types.PersistenceObjectIdResolving;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.typing.KeyValue;
import net.jadoth.typing.XTypes;
import net.jadoth.util.BinaryHandlerSubstituterImplementation;
//CHECKSTYLE.OFF: IllegalImport: low-level system tools are required for high performance low-level operations
import sun.misc.Unsafe;
//CHECKSTYLE.ON: IllegalImport

public final class BinaryPersistence extends Persistence
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/* (12.09.2018 TM)TODO: test Unsafe consolidation
	 * Test and refactor or comment if this constant can be replaced by XVM calls without losing performance.
	 * The idea is to have exactely one class that has ties to a JVM-vendor specific class and that encapsulates
	 * them. So in theory, it should suffice to replace just that one class to port the framework but still have
	 * low level access.
	 * OR they replace the clumsy "Unsafe" by something standardized, of course.
	 */
	static final Unsafe VM = (Unsafe)XVM.getSystemInstance();

	static final int
		LENGTH_LONG = Long.BYTES                          ,
		LENGTH_LEN  = LENGTH_LONG                         ,
		LENGTH_OID  = LENGTH_LONG                         ,
		LENGTH_TID  = LENGTH_OID                          , // tid IS AN oid, so it must have the same length
		LENGTH_TO   = LENGTH_TID + LENGTH_OID             , // 8
		LENGTH_LTO  = LENGTH_LEN + LENGTH_TID + LENGTH_OID  // 24
	;

	// header (currently) constists of only LEN, TID, OID. Extra constant has sementical reasons.
	private static final int LENGTH_ENTITY_HEADER = LENGTH_LTO;

	// can't include LABO here as traversal loop has to read length, thus checks iteration offset against LABO + size
	private static final long
		OFFSET_LEN = 0L                      ,
		OFFSET_TID = OFFSET_LEN + LENGTH_LONG,
		OFFSET_OID = OFFSET_TID + LENGTH_LONG
//		OFFSET_DAT = OFFSET_OID + LENGTH_LONG // see LENGTH_ENTITY_HEADER
	;

	private static final int BITS_3 = 3;

	private static final int
		BINARY_ARRAY_OFFSET_BYTE_LENGTH   = 0                                              ,
		BINARY_ARRAY_OFFSET_ELEMENT_COUNT = BINARY_ARRAY_OFFSET_BYTE_LENGTH   + LENGTH_LONG,
		BINARY_ARRAY_OFFSET_ELEMENT_DATA  = BINARY_ARRAY_OFFSET_ELEMENT_COUNT + LENGTH_LONG
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static final long binaryArrayMinimumLength()
	{
		return BINARY_ARRAY_OFFSET_ELEMENT_DATA;
	}

	public static final long binaryArrayMaximumLength()
	{
		return Long.MAX_VALUE;
	}

	/**
	 * @return the length in bytes of a peristent item's length field (8 bytes).
	 */
	public static final int lengthLength()
	{
		return LENGTH_LEN;
	}

	public static final int lengthListHeader()
	{
		return BINARY_ARRAY_OFFSET_ELEMENT_DATA;
	}

	public static final boolean isValidGapLength(final long gapLength)
	{
		// gap total length cannot indicate less then its own length (length of the length field, 1 long)
		return gapLength >= LENGTH_LEN;
	}

	public static final boolean isValidEntityLength(final long entityLength)
	{
		return entityLength >= LENGTH_LTO;
	}

	
	static final long binaryArrayByteLengthAddress(final long valueAddress)
	{
		return valueAddress + BINARY_ARRAY_OFFSET_BYTE_LENGTH;
	}

	static final long binaryArrayByteLength(final long valueAddress)
	{
		return XVM.get_long(binaryArrayByteLengthAddress(valueAddress));
	}

	static final long binaryArrayElementCountAddress(final long valueAddress)
	{
		return valueAddress + BINARY_ARRAY_OFFSET_ELEMENT_COUNT;
	}

	static final long binaryArrayElementCount(final long valueAddress)
	{
		return XVM.get_long(binaryArrayElementCountAddress(valueAddress));
	}

	static final long binaryArrayElementDataAddress(final long valueAddress)
	{
		return valueAddress + BINARY_ARRAY_OFFSET_ELEMENT_DATA;
	}


	public static final long binaryArrayByteLengthAddress(final Binary bytes)
	{
		return binaryArrayElementDataAddress(bytes.entityContentAddress);
	}

	public static final long binaryArrayByteLength(final Binary bytes)
	{
		return binaryArrayByteLength(bytes.entityContentAddress);
	}

	public static final long binaryArrayElementCountAddress(final Binary bytes)
	{
		return binaryArrayElementCountAddress(bytes.entityContentAddress);
	}

	public static final long binaryArrayElementCount(final Binary bytes)
	{
		return binaryArrayElementCount(bytes.entityContentAddress);
	}

	public static final long binaryArrayElementDataAddress(final Binary bytes)
	{
		return binaryArrayElementDataAddress(bytes.entityContentAddress);
	}


	public static final long binaryArrayByteLengthAddress(final Binary bytes, final long addressOffset)
	{
		return binaryArrayElementDataAddress(bytes.entityContentAddress + addressOffset);
	}

	public static final long binaryArrayByteLength(final Binary bytes, final long addressOffset)
	{
		return binaryArrayByteLength(bytes.entityContentAddress + addressOffset);
	}

	public static final long binaryArrayElementCountAddress(final Binary bytes, final long addressOffset)
	{
		return binaryArrayElementCountAddress(bytes.entityContentAddress + addressOffset);
	}

	public static final long binaryArrayElementCount(final Binary bytes, final long addressOffset)
	{
		return binaryArrayElementCount(bytes.entityContentAddress + addressOffset);
	}

	public static final long binaryArrayElementDataAddress(final Binary bytes, final long addressOffset)
	{
		return binaryArrayElementDataAddress(bytes.entityContentAddress + addressOffset);
	}


	public static final long calculateBinaryArrayByteLength(final long dataLength)
	{
		return BINARY_ARRAY_OFFSET_ELEMENT_DATA + dataLength; // element data offset equals header length
	}

	// (03.07.2013)TODO: entityHeaderLength() should never be required
	public static final int entityHeaderLength()
	{
		return LENGTH_ENTITY_HEADER;
	}

	public static final long entityTotalLength(final long entityContentLength)
	{
		// the total length is the content length plus the length of the header (containing length, Tid, Oid)
		return entityContentLength + LENGTH_ENTITY_HEADER;
	}
	
	public static final long entityContentLength(final long entityTotalLength)
	{
		// the content length is the total length minus the length of the header (containing length, Tid, Oid)
		return entityTotalLength - LENGTH_ENTITY_HEADER;
	}

	// (23.05.2013 TM)XXX: Consolidate different naming patterns (with/without get~ etc)
		
	public static final long getEntityTypeId(final long entityAddress)
	{
		return VM.getLong(entityAddress + OFFSET_TID);
	}

	public static final long getEntityObjectId(final long entityAddress)
	{
		return VM.getLong(entityAddress + OFFSET_OID);
	}

	public static final long entityContentAddress(final long entityAddress)
	{
		return entityAddress + LENGTH_ENTITY_HEADER;
	}

	public static final long storeEntityHeader(
		final long entityAddress      ,
		final long entityContentLength, // note: entity CONTENT length (without header length!)
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		setEntityHeaderValues(entityAddress, entityTotalLength(entityContentLength), entityTypeId, entityObjectId);
		return entityAddress + entityTotalLength(entityContentLength);
	}

	public static final void setEntityHeaderValues(
		final long address       ,
		final long entityLength  , // note: entity TOTAL length (including header length!)
		final long entityTypeId  ,
		final long entityObjectId
	)
	{
		XVM.set_long(address + OFFSET_LEN, entityLength  );
		XVM.set_long(address + OFFSET_TID, entityTypeId  );
		XVM.set_long(address + OFFSET_OID, entityObjectId);
	}

	public static final long oidLength()
	{
		return LENGTH_OID;
	}

	private static final BinaryValueStorer STORE_1 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceHandler handler
		)
		{
			VM.putByte(address, VM.getByte(src, srcOffset));
			return address + Byte.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_2 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceHandler handler
		)
		{
			VM.putShort(address, VM.getShort(src, srcOffset));
			return address + Short.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_4 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceHandler handler
		)
		{
			VM.putInt(address, VM.getInt(src, srcOffset));
			return address + Integer.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_8 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceHandler handler
		)
		{
			VM.putLong(address, VM.getLong(src, srcOffset));
			return address + Long.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_REFERENCE = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceHandler handler
		)
		{
			VM.putLong(address, handler.apply(VM.getObject(src, srcOffset)));
			return address + LENGTH_OID;
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_EAGER = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         source       ,
			final long           sourceOffset ,
			final long           targetAddress,
			final PersistenceHandler handler
		)
		{
			VM.putLong(targetAddress, handler.applyEager(VM.getObject(source, sourceOffset)));
			return targetAddress + LENGTH_OID;
		}
	};

	private static final BinaryValueSetter SETTER_1 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     sourceAddress,
			final Object                   target       ,
			final long                     targetOffset ,
			final PersistenceObjectIdResolving idResolver
		)
		{
			VM.putByte(target, targetOffset, VM.getByte(sourceAddress));
			return sourceAddress + Byte.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_2 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     sourceAddress,
			final Object                   target       ,
			final long                     targetOffset ,
			final PersistenceObjectIdResolving idResolver
		)
		{
			VM.putShort(target, targetOffset, VM.getShort(sourceAddress));
			return sourceAddress + Short.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_4 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     sourceAddress,
			final Object                   target       ,
			final long                     targetOffset ,
			final PersistenceObjectIdResolving idResolver
		)
		{
			VM.putInt(target, targetOffset, VM.getInt(sourceAddress));
			return sourceAddress + Integer.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_8 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     sourceAddress,
			final Object                   target       ,
			final long                     targetOffset ,
			final PersistenceObjectIdResolving idResolver
		)
		{
			VM.putLong(target, targetOffset, VM.getLong(sourceAddress));
			return sourceAddress + Long.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_REF = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     sourceAddress,
			final Object                   target       ,
			final long                     targetOffset ,
			final PersistenceObjectIdResolving idResolver
		)
		{
			VM.putObject(target, targetOffset, idResolver.lookupObject(VM.getLong(sourceAddress)));
			return sourceAddress + LENGTH_OID;
		}
	};

	private static final BinaryValueEqualator EQUAL_1 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final PersistenceObjectIdResolving oidResolver
		)
		{
			return VM.getByte(src, srcOffset) == VM.getByte(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_2 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final PersistenceObjectIdResolving oidResolver
		)
		{
			return VM.getShort(src, srcOffset) == VM.getShort(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_4 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final PersistenceObjectIdResolving oidResolver
		)
		{
			return VM.getInt(src, srcOffset) == VM.getInt(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_8 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final PersistenceObjectIdResolving oidResolver
		)
		{
			return VM.getLong(src, srcOffset) == VM.getLong(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_REF = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final PersistenceObjectIdResolving oidResolver
		)
		{
			return VM.getObject(src, srcOffset) == oidResolver.lookupObject(VM.getLong(address));
		}
	};

	public static final PersistenceCustomTypeHandlerRegistry<Binary> createDefaultCustomTypeHandlerRegistry()
	{
		final PersistenceCustomTypeHandlerRegistry.Implementation<Binary> defaultCustomTypeHandlerRegistry =
			PersistenceCustomTypeHandlerRegistry.<Binary>New()
			.registerTypeHandlers(nativeHandlers())
			.registerTypeHandlers(defaultCustomHandlers())
		;
		return defaultCustomTypeHandlerRegistry;
	}
	
	
	static final void initializeNativeTypeId(final PersistenceTypeHandler<Binary, ?> typeHandler)
	{
		final Long nativeTypeId = Persistence.getNativeTypeId(typeHandler.type());
		if(nativeTypeId == null)
		{
			// (07.11.2018 TM)EXCP: proper exception
			throw new RuntimeException("No native TypeId found for type " + typeHandler.type());
		}
		
		typeHandler.initializeTypeId(nativeTypeId);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlers()
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlers = ConstList.New(
			new BinaryHandlerPrimitive<>(byte   .class),
			new BinaryHandlerPrimitive<>(boolean.class),
			new BinaryHandlerPrimitive<>(short  .class),
			new BinaryHandlerPrimitive<>(char   .class),
			new BinaryHandlerPrimitive<>(int    .class),
			new BinaryHandlerPrimitive<>(float  .class),
			new BinaryHandlerPrimitive<>(long   .class),
			new BinaryHandlerPrimitive<>(double .class),

//			new BinaryHandlerNativeClass()    , // (18.09.2018 TM)NOTE: see comments in BinaryHandlerNativeClass.
			new BinaryHandlerNativeByte()     ,
			new BinaryHandlerNativeBoolean()  ,
			new BinaryHandlerNativeShort()    ,
			new BinaryHandlerNativeCharacter(),
			new BinaryHandlerNativeInteger()  ,
			new BinaryHandlerNativeFloat()    ,
			new BinaryHandlerNativeLong()     ,
			new BinaryHandlerNativeDouble()   ,
			
			new BinaryHandlerNativeVoid()     ,
			
			new BinaryHandlerNativeObject()   ,
			
			new BinaryHandlerNativeString()   ,
			new BinaryHandlerStringBuffer()   ,
			new BinaryHandlerStringBuilder()  ,

			new BinaryHandlerNativeArray_byte()   ,
			new BinaryHandlerNativeArray_boolean(),
			new BinaryHandlerNativeArray_short()  ,
			new BinaryHandlerNativeArray_char()   ,
			new BinaryHandlerNativeArray_int()    ,
			new BinaryHandlerNativeArray_float()  ,
			new BinaryHandlerNativeArray_long()   ,
			new BinaryHandlerNativeArray_double() ,

			new BinaryHandlerArrayList() ,
			new BinaryHandlerBigInteger(),
			new BinaryHandlerBigDecimal(),
			new BinaryHandlerFile()      ,
			new BinaryHandlerDate()      ,
			new BinaryHandlerHashSet()   ,

			new BinaryHandlerLazyReference()
			// (24.10.2013 TM)TODO: more native handlers (old collections etc.)
		);
		
		nativeHandlers.iterate(BinaryPersistence::initializeNativeTypeId);
		
		return nativeHandlers;
	}

	/* (17.04.2013 TM)FIXME: register NotPersistable Handler instances
	 * instances (not just classes) for unpersistable types (like Thread, WeakReference, etc.)
	 * have to be registered somewhere.
	 * Also unpersistable:
	 * - class loader
	 * - any kind of io stream, channel, etc.
	 */
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> defaultCustomHandlers()
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> defaultHandlers = ConstList.New(
			new BinaryHandlerBulkList()        ,
			new BinaryHandlerLimitList()       ,
			new BinaryHandlerFixedList()       ,
			new BinaryHandlerConstList()       ,
			new BinaryHandlerEqBulkList()      ,
			new BinaryHandlerHashEnum()        ,
			new BinaryHandlerConstHashEnum()   ,
			new BinaryHandlerEqHashEnum()      ,
			new BinaryHandlerEqConstHashEnum() ,
			new BinaryHandlerHashTable()       ,
			new BinaryHandlerConstHashTable()  ,
			new BinaryHandlerEqHashTable()     ,
			new BinaryHandlerEqConstHashTable(),

			new BinaryHandlerSubstituterImplementation()
			/* (29.10.2013 TM)TODO: more framework default custom handlers
			 * - VarString
			 * - VarByte
			 * - _intList etc.
			 */
		);
		
		// default custom handlers have no fixed typeId like native handlers.
		return defaultHandlers;
	}
	

	public static final void storeFixedSize(
		final Binary                   bytes        ,
		final PersistenceHandler           handler      ,
		final long                     contentLength,
		final long                     typeId       ,
		final long                     objectId     ,
		final Object                   instance     ,
		final long[]                   memoryOffsets,
		final BinaryValueStorer[]      storers
	)
	{
		long address = bytes.storeEntityHeader(contentLength, typeId, objectId);
		for(int i = 0; i < memoryOffsets.length; i++)
		{
			address = storers[i].storeValueFromMemory(instance, memoryOffsets[i], address, handler);
		}
	}

	public static final void updateFixedSize(
		final Object                   instance     ,
		final BinaryValueSetter[]      setters      ,
		final long[]                   memoryOffsets,
		final long                     dataAddress  ,
		final PersistenceObjectIdResolving idResolver
	)
	{
		long address = dataAddress;
		for(int i = 0; i < setters.length; i++)
		{
			address = setters[i].setValueToMemory(address, instance, memoryOffsets[i], idResolver);
		}
	}

	public static final boolean isEqualBinaryState(
		final Object                   instance     ,
		final BinaryValueEqualator[]   equalators   ,
		final long[]                   memoryOffsets,
		final long[]                   binaryOffsets,
		final long                     address      ,
		final PersistenceObjectIdResolving oidResolver
	)
	{
		for(int i = 0; i < equalators.length; i++)
		{
			if(!equalators[i].equalValue(instance, address + binaryOffsets[i], memoryOffsets[i], oidResolver))
			{
				return false;
			}
		}
		return true;
	}

	public static final void iterateInstanceReferences(
		final PersistenceFunction iterator,
		final Object instance,
		final long[] referenceOffsets
	)
	{
		for(int i = 0; i < referenceOffsets.length; i++)
		{
			if(referenceOffsets[i] != 0)
			{
				iterator.apply(VM.getObject(instance, referenceOffsets[i]));
			}
		}
	}

	// (28.10.2013 TM)XXX: move all List~ handling methods to BinaryCollectionHandling? Or somewhere else?
	// (28.10.2013 TM)XXX: consolidate "List~" naming with array (see "SizedArray").

	static final void iterateListElementReferencesAtAddress(
		final long           address ,
		final long           count   ,
		final _longProcedure iterator
	)
	{
		final long boundAddress = address + count * LENGTH_OID;
		for(long a = address; a < boundAddress; a += LENGTH_OID)
		{
			iterator.accept(VM.getLong(a));
		}
	}


	public static final void iterateListElementReferencesWithElementOffset(
		final Binary         bytes        ,
		final long           listOffset   ,
		final long           elementOffset,
		final long           elementLength,
		final _longProcedure iterator
	)
	{
		BinaryPersistence.iterateListElementReferencesAtAddressWithElementOffset(
			BinaryPersistence.getListElementsAddress(bytes, listOffset),
			BinaryPersistence.getListElementCount(bytes, listOffset),
			elementOffset,
			elementLength,
			iterator
		);
	}

	static final void iterateListElementReferencesAtAddressWithElementOffset(
		final long           elementsStartAddress,
		final long           count               ,
		final long           elementOffset       ,
		final long           elementLength       ,
		final _longProcedure iterator
	)
	{
		final long boundAddress = elementsStartAddress + count * elementLength;
		for(long a = elementsStartAddress; a < boundAddress; a += elementLength)
		{
			iterator.accept(VM.getLong(a + elementOffset));
		}
	}


	public static final void iterateListElementReferences(
		final Binary         bytes   ,
		final long           listOffset  ,
		final _longProcedure iterator
	)
	{
		BinaryPersistence.iterateListElementReferencesAtAddress(
			BinaryPersistence.getListElementsAddress(bytes, listOffset),
			BinaryPersistence.getListElementCount(bytes, listOffset),
			iterator
		);
	}

	public static final void iterateBinaryReferences(
		final Binary         bytes      ,
		final long           startOffset,
		final long           boundOffset,
		final _longProcedure iterator
	)
	{
		final long boundAddress = bytes.entityContentAddress + boundOffset;
		for(long address = bytes.entityContentAddress + startOffset; address < boundAddress; address += LENGTH_LONG)
		{
			iterator.accept(VM.getLong(address));
		}
	}

	public static final void storeArray_byte(final Binary bytes, final long tid, final long oid, final byte[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength(array.length);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_BYTE_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			array.length
		);
	}

	public static final void storeArray_boolean(
		final Binary    bytes,
		final long      tid  ,
		final long      oid  ,
		final boolean[] array
	)
	{
		final long totalByteLength = calculateBinaryArrayByteLength(array.length);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			array.length
		);
	}

	public static final void storeArray_short(final Binary bytes, final long tid, final long oid, final short[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << 1);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_SHORT_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << 1 // calculating again should be faster than writing a local length variable
		);
	}

	public static final void storeArray_char(final Binary bytes, final long tid, final long oid, final char[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << 1);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_CHAR_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << 1 // calculating again should be faster than writing a local length variable
		);
	}

	public static final void storeArray_int(final Binary bytes, final long tid, final long oid, final int[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << 2);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_INT_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << 2 // calculating again should be faster than writing a local length variable
		);
	}

	public static final void storeArray_float(final Binary bytes, final long tid, final long oid, final float[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << 2);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_FLOAT_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << 2 // calculating again should be faster than writing a local length variable
		);
	}

	public static final void storeArray_long(final Binary bytes, final long tid, final long oid, final long[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << BITS_3);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_LONG_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << BITS_3 // calculating again should be faster than writing a local length variable
		);
	}

	public static final void storeArray_double(final Binary bytes, final long tid, final long oid, final double[] array)
	{
		final long totalByteLength = calculateBinaryArrayByteLength((long)array.length << BITS_3);
		final long storeAddress    = bytes.storeEntityHeader(totalByteLength, tid, oid);

		VM.putLong(binaryArrayByteLengthAddress(storeAddress), totalByteLength);
		VM.putLong(binaryArrayElementCountAddress(storeAddress), array.length);
		VM.copyMemory(
			array,
			Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
			null,
			binaryArrayElementDataAddress(storeAddress),
			(long)array.length << BITS_3 // calculating again should be faster than writing a local length variable
		);
	}

	public static final long store_int(final long address, final int value)
	{
		VM.putInt(address, value);
		return address + Integer.BYTES;
	}

	public static final long store_long(final long address, final long value)
	{
		VM.putLong(address, value);
		return address + Long.BYTES;
	}

	public static final void storeByte(final Binary bytes, final long tid, final long oid, final byte value)
	{
		VM.putByte(bytes.storeEntityHeader(Byte.BYTES, tid, oid), value);
	}

	public static final void storeBoolean(final Binary bytes, final long tid, final long oid, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		// and where is Boolean.BYTES? Does a boolean not have a binary size? JDK pros... .
		VM.putBoolean(null, bytes.storeEntityHeader(Byte.BYTES, tid, oid), value);
	}

	public static final void storeShort(final Binary bytes, final long tid, final long oid, final short value)
	{
		VM.putShort(bytes.storeEntityHeader(Short.BYTES, tid, oid), value);
	}

	public static final void storeCharacter(final Binary bytes, final long tid, final long oid, final char value)
	{
		VM.putChar(bytes.storeEntityHeader(Character.BYTES, tid, oid), value);
	}

	public static final void storeInteger(final Binary bytes, final long tid, final long oid, final int value)
	{
		VM.putInt(bytes.storeEntityHeader(Integer.BYTES, tid, oid), value);
	}

	public static final void storeFloat(final Binary bytes, final long tid, final long oid, final float value)
	{
		VM.putFloat(bytes.storeEntityHeader(Float.BYTES, tid, oid), value);
	}

	public static final void storeLong(final Binary bytes, final long tid, final long oid, final long value)
	{
		VM.putLong(bytes.storeEntityHeader(Long.BYTES, tid, oid), value);
	}

	public static final void storeDouble(final Binary bytes, final long tid, final long oid, final double value)
	{
		VM.putDouble(bytes.storeEntityHeader(Double.BYTES, tid, oid), value);
	}

	public static final void storeStateless(final Binary bytes, final long tid, final long oid)
	{
		bytes.storeEntityHeader(0L, tid, oid); // so funny :D
	}

	public static final void storeStringValue(final Binary bytes, final long tid, final long oid, final String string)
	{
		final char[] chars = XVM.accessChars(string); // thank god they fixed that stupid String storage mess
		storeChars(
			bytes.storeEntityHeader(
				calculateBinaryLengthChars(chars.length),
				tid,
				oid
			),
			chars,
			0,
			chars.length
		);
	}

	public static final long storeArrayContentAsList(
		final Binary          bytes       ,
		final long            typeId      ,
		final long            objectId    ,
		final long            binaryOffset,
		final PersistenceFunction persister   ,
		final Object[]        array       ,
		final int             offset      ,
		final int             length
	)
	{
		final long contentAddress = bytes.storeEntityHeader(
			binaryOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(array.length),
			typeId,
			objectId
		);

		storeArrayContentAsList(contentAddress + binaryOffset, persister, array, offset, length);

		return contentAddress;
	}

	public static final void storeArrayContentAsList(
		final long            storeAddress,
		final PersistenceFunction persister   ,
		final Object[]        array       ,
		final int             offset      ,
		final int             length
	)
	{
		final long elementsDataAddress = storeListHeader(
			storeAddress,
			referenceBinaryLength(length),
			length
		);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			XVM.set_long(elementsDataAddress + referenceBinaryLength(i), persister.apply(array[i]));
		}
	}

	public static final long storeListHeader(
		final long storeAddress        ,
		final long elementsBinaryLength,
		final long elementsCount
	)
	{
		XVM.set_long(storeAddress + LIST_OFFSET_LENGTH, calculateListTotalBinaryLength(elementsBinaryLength));
		XVM.set_long(storeAddress + LIST_OFFSET_COUNT , elementsCount);
		return storeAddress + LIST_OFFSET_ELEMENTS;
	}

	public static final void storeIterableContentAsList(
		final long            storeAddress,
		final PersistenceFunction persister   ,
		final Iterable<?>     elements    ,
		final long            elementCount
	)
	{
		final Iterator<?> iterator = elements.iterator();

		final long referenceLength     = referenceBinaryLength(1);
		final long elementsBinaryRange = elementCount * referenceLength;
		final long elementsDataAddress = storeListHeader(storeAddress, elementsBinaryRange, elementCount);
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;

		long address = elementsDataAddress;

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		while(address < elementsBinaryBound && iterator.hasNext())
		{
			final Object element = iterator.next();
			XVM.set_long(address, persister.apply(element));
			address += referenceLength;
		}

		/* if there are fewer elements than specified, it is an error just the same.
		 * The element count must match exactely, no matter what.
		 */
		if(address != elementsBinaryBound)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element count: specified " + elementCount
				+ " vs. iterated " + elementsBinaryBound / referenceLength
			);
		}
	}

	public static final void storeKeyValuesAsEntries(
		final long                               storeAddress,
		final PersistenceFunction                    persister   ,
		final Iterable<? extends KeyValue<?, ?>> elements    ,
		final long                               elementCount
	)
	{
		final Iterator<? extends KeyValue<?, ?>> iterator = elements.iterator();

		final long referenceLength = referenceBinaryLength(1);

		// two references per entry
		final long entryLength = 2 * referenceLength;

		final long elementsBinaryRange = elementCount * entryLength;
		final long elementsDataAddress = storeListHeader(storeAddress, elementsBinaryRange, elementCount);
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;

		long address = elementsDataAddress;

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		while(address < elementsBinaryBound && iterator.hasNext())
		{
			final KeyValue<?, ?> element = iterator.next();
			XVM.set_long(address                  , persister.apply(element.key())  );
			XVM.set_long(address + referenceLength, persister.apply(element.value()));
			address += entryLength; // advance index for both in one step
		}

		/* if there are fewer elements than specified, it is an error just the same.
		 * The element count must match exactely, no matter what.
		 */
		if(address != elementsBinaryBound)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element count: specified " + elementCount
				+ " vs. iterated " + elementsBinaryBound / entryLength
			);
		}
	}



	public static final void storeStringsAsList(
		final long     storeAddress            ,
		final long     precalculatedContentBinaryLength,
		final String[] strings
	)
	{
		storeStringsAsList(storeAddress, precalculatedContentBinaryLength, strings, 0, strings.length);
	}

	public static final void storeStringsAsList(
		final long     storeAddress                    ,
		final long     precalculatedContentBinaryLength,
		final String[] strings                         ,
		final int      offset                          ,
		final int      length
	)
	{
		long elementsDataAddress = storeListHeader(storeAddress, precalculatedContentBinaryLength, length);

		// (23.02.2015 TM)NOTE: old
//		Memory.set_long(storeAddress + LIST_OFFSET_LENGTH, precalculatedTotalLength);
//		Memory.set_long(storeAddress + LIST_OFFSET_COUNT , length                  );
//		long elementDataAddress = storeAddress + LIST_OFFSET_ELEMENTS;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			elementsDataAddress = storeChars(elementsDataAddress, XVM.accessChars(strings[i]));
		}
	}

	public static final long storeChars(final long storeAddress, final char[] chars)
	{
		return storeChars(storeAddress, chars, 0, chars.length);
	}

	public static final long storeChars(
		final long   storeAddress,
		final char[] chars       ,
		final int    offset      ,
		final int    length
	)
	{
		// total binary length is header length plus content length
		final long elementsBinaryLength = length * XVM.byteSize_char();
		final long elementsDataAddress  = storeListHeader(storeAddress, elementsBinaryLength, length);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			XVM.set_char(elementsDataAddress + (i << 1), chars[i]);
		}

		return elementsDataAddress + elementsBinaryLength;
	}

	public static final long referenceBinaryLength(final long referenceCount)
	{
		return referenceCount << BITS_3; // reference (ID) binary length is 8
	}

	public static final long buildStrings(
		final Binary   bytes ,
		final long     offset,
		final String[] target
	)
	{
		final long listAddress  = bytes.entityContentAddress + offset;
		final long elementCount = BinaryPersistence.binaryArrayElementCount(listAddress);

		if(target.length != elementCount)
		{
			throw new RuntimeException(); // (22.10.2013 TM)EXCP: proper exception
		}

		long elementAddress = BinaryPersistence.binaryArrayElementDataAddress(listAddress); // first element address
		for(int i = 0; i < target.length; i++)
		{
			target[i] = XVM.wrapCharsAsString(BinaryPersistence.buildArray_char(elementAddress)); // build element
			elementAddress += BinaryPersistence.binaryArrayByteLength(elementAddress); // scroll to next element
		}

		// as this is an offset-based public method, it must return an offset, not an absolute address
		return elementAddress - bytes.entityContentAddress;
	}

	public static final Byte buildByte(final Binary bytes)
	{
		return new Byte(VM.getByte(bytes.entityContentAddress));
	}

	public static final Boolean buildBoolean(final Binary bytes)
	{
		return new Boolean(VM.getBoolean(null, bytes.entityContentAddress));
	}

	public static final Short buildShort(final Binary bytes)
	{
		return new Short(VM.getShort(bytes.entityContentAddress));
	}

	public static final Character buildCharacter(final Binary bytes)
	{
		return new Character(VM.getChar(bytes.entityContentAddress));
	}

	public static final Integer buildInteger(final Binary bytes)
	{
		return new Integer(VM.getInt(bytes.entityContentAddress));
	}

	public static final Float buildFloat(final Binary bytes)
	{
		return new Float(VM.getFloat(bytes.entityContentAddress));
	}

	public static final Long buildLong(final Binary bytes)
	{
		return new Long(VM.getLong(bytes.entityContentAddress));
	}

	public static final Double buildDouble(final Binary bytes)
	{
		return new Double(VM.getDouble(bytes.entityContentAddress));
	}

	public static final byte[] buildArray_byte(final Binary bytes)
	{
		final long elementCount = binaryArrayElementCount(bytes.entityContentAddress);
		final byte[] array;
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array = new byte[X.checkArrayRange(elementCount)],
			Unsafe.ARRAY_BYTE_BASE_OFFSET,
			elementCount << 0
		);
		return array;
	}

	public static final char[] buildArray_char(final Binary bytes)
	{
		return buildArray_char(bytes.entityContentAddress);
	}

	public static final char[] buildArray_char(final Binary bytes, final long addressOffset)
	{
		return buildArray_char(bytes.entityContentAddress + addressOffset);
	}

	static final char[] buildArray_char(final long valueAddress)
	{
		final long elementCount = binaryArrayElementCount(valueAddress);
		final char[] array;
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(valueAddress),
			array = new char[X.checkArrayRange(elementCount)],
			Unsafe.ARRAY_CHAR_BASE_OFFSET,
			elementCount << 1
		);
		return array;
	}

	// array restrukturierung marker //

	public static final byte[] createArray_byte(final Binary bytes)
	{
		return new byte[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_byte(final byte[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_BYTE_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 0
		);
	}

	public static final boolean[] createArray_boolean(final Binary bytes)
	{
		return new boolean[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_boolean(final boolean[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_BOOLEAN_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 0
		);
	}

	public static final short[] createArray_short(final Binary bytes)
	{
		return new short[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_short(final short[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_SHORT_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 1
		);
	}

	public static final char[] createArray_char(final Binary bytes)
	{
		return new char[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_char(final char[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_CHAR_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 1
		);
	}

	public static final int[] createArray_int(final Binary bytes)
	{
		return new int[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_int(final int[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_INT_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 2
		);
	}

	public static final float[] createArray_float(final Binary bytes)
	{
		return new float[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_float(final float[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_FLOAT_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << 2
		);
	}

	public static final long[] createArray_long(final Binary bytes)
	{
		return new long[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_long(final long[] array, final Binary bytes)
	{
		XVM.copyRangeToArray(binaryArrayElementDataAddress(bytes.entityContentAddress), array);
	}

	public static final double[] createArray_double(final Binary bytes)
	{
		return new double[X.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_double(final double[] array, final Binary bytes)
	{
		VM.copyMemory(
			null,
			binaryArrayElementDataAddress(bytes.entityContentAddress),
			array,
			Unsafe.ARRAY_DOUBLE_BASE_OFFSET,
			binaryArrayElementCount(bytes.entityContentAddress) << BITS_3
		);
	}

	public static final String buildString(final Binary bytes)
	{
		// perfectly reasonable example use of the wrapping method.
		return XVM.wrapCharsAsString(buildArray_char(bytes));
	}

	public static final long getBuildItemContentLength(final Binary bytes)
	{
		return VM.getLong(bytes.entityContentAddress - LENGTH_LTO) - LENGTH_LTO;
	}

	public static final long getBuildItemTypeId(final Binary bytes)
	{
		return VM.getLong(bytes.entityContentAddress - LENGTH_TO);
	}

	public static final long getBuildItemObjectId(final Binary bytes)
	{
		return VM.getLong(bytes.entityContentAddress - LENGTH_OID);
	}

	public static final long getEntityLength(final long entityAddress)
	{
		// (06.09.2014)TODO: test and comment if " + 0L" gets eliminated by JIT
		return VM.getLong(entityAddress + OFFSET_LEN);
	}

	public static final BinaryValueStorer getStorer_byte()
	{
		return STORE_1;
	}
	
	public static final BinaryValueStorer getStorer_boolean()
	{
		return STORE_1;
	}
	
	public static final BinaryValueStorer getStorer_short()
	{
		return STORE_2;
	}
	
	public static final BinaryValueStorer getStorer_char()
	{
		return STORE_2;
	}
	
	public static final BinaryValueStorer getStorer_int()
	{
		return STORE_4;
	}
	
	public static final BinaryValueStorer getStorer_float()
	{
		return STORE_4;
	}
	
	public static final BinaryValueStorer getStorer_long()
	{
		return STORE_8;
	}
	
	public static final BinaryValueStorer getStorer_double()
	{
		return STORE_8;
	}
	
	public static final BinaryValueStorer getStorerReference()
	{
		return STORE_REFERENCE;
	}
	
	public static final BinaryValueStorer getStorerReferenceForced()
	{
		return STORE_REFERENCE_EAGER;
	}

	public static BinaryValueStorer getObjectValueStorer(
		final Class<?> type    ,
		final boolean  isForced
	)
		throws IllegalArgumentException
	{
		// primitive special cases
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			switch(XVM.byteSizePrimitive(type))
			{
				case Byte.BYTES   : return STORE_1; // byte & boolean
				case Short.BYTES  : return STORE_2; // short & char
				case Integer.BYTES: return STORE_4; // int & float
				case Long.BYTES   : return STORE_8; // long & double
				default: throw new IllegalArgumentException();
			}
		}

		// reference case. Either "forced" or normal.
		return isForced
			? STORE_REFERENCE_EAGER
			: STORE_REFERENCE
		;
	}
	
	// (23.09.2018 TM)TODO: consolidate with BinaryValueSetters for Legacy Type Mapping value translating?

	public static final BinaryValueSetter getSetter_byte()
	{
		return SETTER_1;
	}
	public static final BinaryValueSetter getSetter_boolean()
	{
		return SETTER_1;
	}
	public static final BinaryValueSetter getSetter_short()
	{
		return SETTER_2;
	}
	public static final BinaryValueSetter getSetter_char()
	{
		return SETTER_2;
	}
	public static final BinaryValueSetter getSetter_int()
	{
		return SETTER_4;
	}
	public static final BinaryValueSetter getSetter_float()
	{
		return SETTER_4;
	}
	public static final BinaryValueSetter getSetter_long()
	{
		return SETTER_8;
	}
	public static final BinaryValueSetter getSetter_double()
	{
		return SETTER_8;
	}
	public static final BinaryValueSetter getSetterReference()
	{
		return SETTER_REF;
	}

	public static BinaryValueSetter getObjectValueSetter(final Class<?> type)
	{
		// primitive special cases
		if(type.isPrimitive())
		{
			switch(XVM.byteSizePrimitive(type))
			{
				case Byte.BYTES   : return SETTER_1; // byte & boolean
				case Short.BYTES  : return SETTER_2; // short & char
				case Integer.BYTES: return SETTER_4; // int & float
				case Long.BYTES   : return SETTER_8; // long & double
				default: throw new IllegalArgumentException();
			}
		}

		// normal case of standard reference
		return SETTER_REF;
	}

	public static BinaryValueEqualator getObjectValueEqualator(final Class<?> type)
	{
		if(type.isPrimitive())
		{
			switch(XVM.byteSizePrimitive(type))
			{
				case Byte.BYTES   : return EQUAL_1;
				case Short.BYTES  : return EQUAL_2;
				case Integer.BYTES: return EQUAL_4;
				case Long.BYTES   : return EQUAL_8;
				default: throw new IllegalArgumentException();
			}
		}
		
		return EQUAL_REF;
	}

	static final int binaryValueSize(final Class<?> type)
	{
		return type.isPrimitive() ? XVM.byteSizePrimitive(type) : LENGTH_OID;
	}

	public static int[] calculateBinarySizes(final XGettingSequence<Field> fields)
	{
		final int[] fieldOffsets = new int[XTypes.to_int(fields.size())];
		fields.iterateIndexed(new IndexProcedure<Field>()
		{
			@Override
			public void accept(final Field e, final long index)
			{
				fieldOffsets[(int)index] = binaryValueSize(e.getType());
			}
		});
		return fieldOffsets;
	}

	public static final <T> void updateInstanceReferences(
		final Binary bytes,
		final T instance,
		final int[] referenceOffsets,
		final PersistenceObjectIdResolving oidResolver
	)
	{
		final long referencesBinaryOffset = bytes.entityContentAddress;
		for(int i = 0; i < referenceOffsets.length; i++)
		{
			VM.putObject(
				instance,
				(long)referenceOffsets[i],
				oidResolver.lookupObject(
					VM.getLong(referencesBinaryOffset + referenceBinaryLength(i))
				)
			);
		}
	}


	private static final int LIST_OFFSET_LENGTH   =  0;
	private static final int LIST_OFFSET_COUNT    =  8;
	private static final int LIST_OFFSET_ELEMENTS = 16;
	private static final int LIST_HEADER_LENGTH   = LIST_OFFSET_ELEMENTS;

	public static final long getListBinaryLength(final long address)
	{
		return VM.getLong(address + LIST_OFFSET_LENGTH);
	}

	public static final long getListElementCount(final long address)
	{
		return VM.getLong(address + LIST_OFFSET_COUNT);
	}

	public static final long getListElementsAddress(final long address)
	{
		return address + LIST_OFFSET_ELEMENTS;
	}


	public static final long getListBinaryLength(final Binary bytes)
	{
		return getListBinaryLength(bytes.entityContentAddress);
	}

	public static final long getListElementCount(final Binary bytes)
	{
		return getListElementCount(bytes.entityContentAddress);
	}

	public static final long getListElementsAddress(final Binary bytes)
	{
		return getListElementsAddress(bytes.entityContentAddress);
	}

	public static final long getListBinaryLength(final Binary bytes, final long offset)
	{
		return getListBinaryLength(bytes.entityContentAddress + offset);
	}

	public static final long getListElementCount(final Binary bytes, final long offset)
	{
		return getListElementCount(bytes.entityContentAddress + offset);
	}

	public static final long getListElementsAddress(final Binary bytes, final long offset)
	{
		return getListElementsAddress(bytes.entityContentAddress + offset);
	}

	public static final long calculateListTotalBinaryLength(final long contentBinaryLength)
	{
		return LIST_HEADER_LENGTH + contentBinaryLength; // header plus content length
	}

	public static final long calculateReferenceListTotalBinaryLength(final long count)
	{
		return calculateListTotalBinaryLength(referenceBinaryLength(count)); // 8 bytes per reference
	}

	public static final long calculateStringListContentBinaryLength(final String[] strings)
	{
		// precise size for each string (char list header plus 2 byte per char)
		long listContentBinaryLength = 0;
		for(final String string : strings)
		{
			listContentBinaryLength += calculateBinaryLengthChars(string.length());
		}

		return listContentBinaryLength;
	}

	public static final long calculateBinaryLengthChars(final long count)
	{
		return calculateListTotalBinaryLength(count << 1);  // header plus 2 bytes per char
	}

	public static final void updateArrayObjectReferences(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final PersistenceObjectIdResolving oidResolver ,
		final Object[]                 array       ,
		final int                      offset      ,
		final int                      length
	)
	{
		if(BinaryPersistence.getListElementCount(bytes, binaryOffset) < length)
		{
			throw new BinaryPersistenceExceptionStateArrayLength(
				array,
				X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, binaryOffset))
			);
		}
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			array[offset + i] = oidResolver.lookupObject(
				XVM.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public static final void collectElementsIntoArray(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final PersistenceObjectIdResolving oidResolver ,
		final Object[]                 target
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < target.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			target[i] = oidResolver.lookupObject(
				XVM.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public static final int collectListObjectReferences(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final PersistenceObjectIdResolving oidResolver ,
		final Consumer<Object>        collector
	)
	{
		final int size = X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, binaryOffset));
		BinaryPersistence.collectObjectReferences(
			bytes       ,
			binaryOffset,
			size        ,
			oidResolver ,
			collector
		);
		return size;
	}

	public static final void collectObjectReferences(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final int                      length      ,
		final PersistenceObjectIdResolving oidResolver ,
		final Consumer<Object>         collector
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				oidResolver.lookupObject(
					XVM.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
				)
			);
		}
	}

	public static final int collectKeyValueReferences(
		final Binary                      bytes       ,
		final long                        binaryOffset,
		final int                         length      ,
		final PersistenceObjectIdResolving    oidResolver ,
		final BiConsumer<Object, Object> collector
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				// key (on every nth oid position)
				oidResolver.lookupObject(
					XVM.get_long(binaryElementsStartAddress + referenceBinaryLength(i << 1))
				),
				// value (on every (n + 1)th oid position)
				oidResolver.lookupObject(
					XVM.get_long(binaryElementsStartAddress + referenceBinaryLength(i << 1) + LENGTH_OID)
				)
			);
		}
		return length;
	}



	@SuppressWarnings("unchecked") // safe by method parameter
	public static final <T> T blankMemoryInstantiate(final Class<T> type)
	{
		try
		{
			return (T)VM.allocateInstance(type);
		}
		catch(final InstantiationException e)
		{
			/* sry but checked exceptions and functional programming (or any clean architecture)
			 * just don't get along with each other.
			 */
			throw new InstantiationRuntimeException(e);
		}
	}

	public static final <T> BinaryInstantiator<T> blankMemoryInstantiator(final Class<T> type)
	{
		return new BinaryInstantiator<T>()
		{
			@SuppressWarnings("unchecked") // safe by method parameter
			@Override
			public T newInstance(final long buildItemAddress) throws InstantiationRuntimeException
			{
				try
				{
					return (T)VM.allocateInstance(type);
				}
				catch(final InstantiationException e)
				{
					/* sry but checked exceptions and functional programming (or any clean architecture)
					 * just don't get along with each other.
					 */
					throw new InstantiationRuntimeException(e);
				}
			}
		};
	}

	public static BinaryPersistenceFoundation<?> Foundation()
	{
		return Foundation(null);
	}

	public static BinaryPersistenceFoundation<?> Foundation(final InstanceDispatcherLogic dispatcher)
	{
		final BinaryPersistenceFoundation<?> foundation = BinaryPersistenceFoundation.New()
			.setInstanceDispatcher(dispatcher)
		;
		return foundation;
	}

	public static final short get_short(final Binary bytes, final long offset)
	{
		return XVM.get_short(bytes.entityContentAddress + offset);
	}

	public static final float get_float(final Binary bytes, final long offset)
	{
		return XVM.get_float(bytes.entityContentAddress + offset);
	}

	public static final int get_int(final Binary bytes, final long offset)
	{
		return XVM.get_int(bytes.entityContentAddress + offset);
	}

	// (28.10.2013 TM)XXX: move all these xxx(Binary, ...) methods to Binary class directly? Possible objections?
	public static final long get_long(final Binary bytes, final long offset)
	{
		return XVM.get_long(bytes.entityContentAddress + offset);
	}

	public static final long resolveFieldBinaryLength(final Class<?> fieldType)
	{
		return fieldType.isPrimitive()
			? resolvePrimitiveFieldBinaryLength(fieldType)
			: oidLength()
		;
	}

	public static final long resolvePrimitiveFieldBinaryLength(final Class<?> primitiveType)
	{
		return XVM.byteSizePrimitive(primitiveType);
	}

	public static final BinaryFieldLengthResolver createFieldLengthResolver()
	{
		return new BinaryFieldLengthResolver.Implementation();
	}
		
	public static PersistenceTypeDictionary provideTypeDictionaryFromFile(final File dictionaryFile)
	{
		final BinaryPersistenceFoundation<?> f = BinaryPersistenceFoundation.New()
			.setTypeDictionaryLoader(
				PersistenceTypeDictionaryFileHandler.New(dictionaryFile)
			)
		;
		return f.getTypeDictionaryProvider().provideTypeDictionary();
	}

}
