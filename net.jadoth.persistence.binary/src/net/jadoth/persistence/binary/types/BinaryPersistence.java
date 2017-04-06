package net.jadoth.persistence.binary.types;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.function.Consumer;

import net.jadoth.Jadoth;
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
import net.jadoth.collections.BulkList;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.exceptions.InstantiationRuntimeException;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.Dispatcher;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionIncompleteChunk;
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
import net.jadoth.persistence.binary.internal.BinaryHandlerNativeClass;
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
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.persistence.types.PersistenceTypeDescriptionBuilder;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeDictionaryProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerCustom;
import net.jadoth.persistence.types.PersistenceTypeResolver;
import net.jadoth.swizzling.types.BinaryHandlerLazyReference;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.swizzling.types.SwizzleObjectIdResolving;
import net.jadoth.swizzling.types.SwizzleTypeIdLookup;
import net.jadoth.util.BinaryHandlerSubstituterImplementation;
import net.jadoth.util.KeyValue;
import net.jadoth.util.VMUtils;
//CHECKSTYLE.OFF: IllegalImport: low-level system tools are required for high performance low-level operations
import sun.misc.Unsafe;
//CHECKSTYLE.ON: IllegalImport

public final class BinaryPersistence extends Persistence
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final Unsafe VM = (Unsafe)VMUtils.getSystemInstance();

	static final int
		LENGTH_LONG = Memory.byteSize_long()              ,
		LENGTH_LEN  = LENGTH_LONG                         ,
		LENGTH_OID  = LENGTH_LONG                         ,
		LENGTH_TID  = LENGTH_OID                          , // tid IS AN oid, so it must have the same length
		LENGTH_LTO  = LENGTH_LEN + LENGTH_TID + LENGTH_OID,
		LENGTH_TO   = LENGTH_TID + LENGTH_OID
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

	private static final int
//		BITS_1 = 1,
//		BITS_2 = 2,
		BITS_3 = 3
	;

	private static final int
		BYTE_SIZE_1 = 1,
		BYTE_SIZE_2 = 2,
		BYTE_SIZE_4 = 4,
		BYTE_SIZE_8 = 8
	;


	private static final int
		BINARY_ARRAY_OFFSET_BYTE_LENGTH   = 0                                              ,
		BINARY_ARRAY_OFFSET_ELEMENT_COUNT = BINARY_ARRAY_OFFSET_BYTE_LENGTH   + LENGTH_LONG,
		BINARY_ARRAY_OFFSET_ELEMENT_DATA  = BINARY_ARRAY_OFFSET_ELEMENT_COUNT + LENGTH_LONG
	;



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
		// gap total length cannot indicate less then its own length (1 long)
		return gapLength >= LENGTH_LONG;
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
		return Memory.get_long(binaryArrayByteLengthAddress(valueAddress));
	}

	static final long binaryArrayElementCountAddress(final long valueAddress)
	{
		return valueAddress + BINARY_ARRAY_OFFSET_ELEMENT_COUNT;
	}

	static final long binaryArrayElementCount(final long valueAddress)
	{
		return Memory.get_long(binaryArrayElementCountAddress(valueAddress));
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
		return entityContentLength + LENGTH_ENTITY_HEADER;
	}

	// (23.05.2013)XXX: Consolidate different naming patterns (with/without get~ etc)

	public static final long entityDataOffset(final long entityAbsoluteOffset)
	{
		return entityAbsoluteOffset - LENGTH_ENTITY_HEADER;
	}

	public static final long entityDataLength(final long entityTotalLength)
	{
		return entityTotalLength - LENGTH_ENTITY_HEADER;
	}

	public static final long storeEntityHeader(
		final long address            ,
		final long entityContentLength, // note: entity CONTENT length (without header length!)
		final long entityTypeId       ,
		final long entityObjectId
	)
	{
		setEntityHeaderValues(address, LENGTH_ENTITY_HEADER + entityContentLength, entityTypeId, entityObjectId);
		return address + LENGTH_ENTITY_HEADER + entityContentLength;
	}

	public static final void setEntityHeaderValues(
		final long address       ,
		final long entityLength  , // note: entity TOTAL length (including header length!)
		final long entityTypeId  ,
		final long entityObjectId
	)
	{
		Memory.set_long(address + OFFSET_LEN, entityLength  );
		Memory.set_long(address + OFFSET_TID, entityTypeId  );
		Memory.set_long(address + OFFSET_OID, entityObjectId);
	}

	public static final long oidLength()
	{
		return LENGTH_OID;
	}

	private static final BinaryValueStorer STORE_1 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object          src      ,
			final long            srcOffset,
			final long            address  ,
			final SwizzleFunction persister
		)
		{
			VM.putByte(address, VM.getByte(src, srcOffset));
			return address + BYTE_SIZE_1;
		}
	};

	private static final BinaryValueStorer STORE_2 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object          src      ,
			final long            srcOffset,
			final long            address  ,
			final SwizzleFunction persister
		)
		{
			VM.putShort(address, VM.getShort(src, srcOffset));
			return address + BYTE_SIZE_2;
		}
	};

	private static final BinaryValueStorer STORE_4 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object          src      ,
			final long            srcOffset,
			final long            address  ,
			final SwizzleFunction persister
		)
		{
			VM.putInt(address, VM.getInt(src, srcOffset));
			return address + BYTE_SIZE_4;
		}
	};

	private static final BinaryValueStorer STORE_8 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object          src      ,
			final long            srcOffset,
			final long            address  ,
			final SwizzleFunction persister
		)
		{
			VM.putLong(address, VM.getLong(src, srcOffset));
			return address + BYTE_SIZE_8;
		}
	};

	private static final BinaryValueStorer STORE_REF = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object          src      ,
			final long            srcOffset,
			final long            address  ,
			final SwizzleFunction persister
		)
		{
			VM.putLong(address, persister.apply(VM.getObject(src, srcOffset)));
			return address + LENGTH_OID;
		}
	};

	private static final BinaryValueSetter SETTER_1 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     srcAddress,
			final Object                   dst       ,
			final long                     dstOffset ,
			final SwizzleObjectIdResolving idResolver
		)
		{
			VM.putByte(dst, dstOffset, VM.getByte(srcAddress));
			return srcAddress + BYTE_SIZE_1;
		}
	};

	private static final BinaryValueSetter SETTER_2 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     srcAddress,
			final Object                   dst       ,
			final long                     dstOffset ,
			final SwizzleObjectIdResolving idResolver
		)
		{
			VM.putShort(dst, dstOffset, VM.getShort(srcAddress));
			return srcAddress + BYTE_SIZE_2;
		}
	};

	private static final BinaryValueSetter SETTER_4 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     srcAddress,
			final Object                   dst       ,
			final long                     dstOffset ,
			final SwizzleObjectIdResolving idResolver
		)
		{
			VM.putInt(dst, dstOffset, VM.getInt(srcAddress));
			return srcAddress + BYTE_SIZE_4;
		}
	};

	private static final BinaryValueSetter SETTER_8 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     srcAddress,
			final Object                   dst       ,
			final long                     dstOffset ,
			final SwizzleObjectIdResolving idResolver
		)
		{
			VM.putLong(dst, dstOffset, VM.getLong(srcAddress));
			return srcAddress + BYTE_SIZE_8;
		}
	};

	private static final BinaryValueSetter SETTER_REF = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                     srcAddress,
			final Object                   dst       ,
			final long                     dstOffset ,
			final SwizzleObjectIdResolving idResolver
		)
		{
			VM.putObject(dst, dstOffset, idResolver.lookupObject(VM.getLong(srcAddress)));
			return srcAddress + LENGTH_OID;
		}
	};

	private static final BinaryValueEqualator EQUAL_1 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                   src        ,
			final long                     srcOffset  ,
			final long                     address    ,
			final SwizzleObjectIdResolving oidResolver
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
			final SwizzleObjectIdResolving oidResolver
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
			final SwizzleObjectIdResolving oidResolver
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
			final SwizzleObjectIdResolving oidResolver
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
			final SwizzleObjectIdResolving oidResolver
		)
		{
			return VM.getObject(src, srcOffset) == oidResolver.lookupObject(VM.getLong(address));
		}
	};

	private static <T> PersistenceTypeDescription<?> primitiveTypeDescription(
		final Class<T>            type,
		final SwizzleTypeIdLookup typeLookup
	)
	{
		// funny thing in this method: primitive generics typing :D
		return new BinaryHandlerPrimitive<>(type, typeLookup.lookupTypeId(type));
	}

	public static final <D extends PersistenceTypeDictionary> D createDefaultTypeDictionary(
		final D                   typeDictionary,
		final SwizzleTypeIdLookup typeLookup
	)
	{
		typeDictionary.registerTypes(X.List(
			// type handlers for primitives have to be added seperately here as special cases
			primitiveTypeDescription(byte.class   , typeLookup),
			primitiveTypeDescription(boolean.class, typeLookup),
			primitiveTypeDescription(short.class  , typeLookup),
			primitiveTypeDescription(char.class   , typeLookup),
			primitiveTypeDescription(int.class    , typeLookup),
			primitiveTypeDescription(float.class  , typeLookup),
			primitiveTypeDescription(long.class   , typeLookup),
			primitiveTypeDescription(double.class , typeLookup),

			// implementation of class type handler doesn't matter here as it is only used to create the type desc.
			new BinaryHandlerNativeClass(null, typeLookup.lookupTypeId(Class.class))
		));
		createDefaultCustomTypeHandlerRegistry().updateTypeDictionary(typeDictionary, typeLookup);
		return typeDictionary;
	}

	public static final PersistenceCustomTypeHandlerRegistry<Binary> createDefaultCustomTypeHandlerRegistry()
	{
		final PersistenceCustomTypeHandlerRegistry.Implementation<Binary> defaultCustomTypeHandlerRegistry =
			new PersistenceCustomTypeHandlerRegistry.Implementation<Binary>()
			.registerTypeHandlerClasses(DEFAULT_HANDLERS)
		;
		return defaultCustomTypeHandlerRegistry;
	}

	/* (17.04.2013 TM)FIXME: register NotPersistable Handler instances
	 * instances (not just classes) for unpersistable types (like Thread, WeakReference, etc.)
	 * have to be registered somewhere.
	 * Also unpersistable:
	 * - class loader
	 * - any kind of io stream, channel, etc.
	 */
	private static final BulkList<Class<? extends PersistenceTypeHandlerCustom<Binary, ?>>> DEFAULT_HANDLERS =
		BulkList.<Class<? extends PersistenceTypeHandlerCustom<Binary, ?>>> New(
			BinaryHandlerNativeByte     .class,
			BinaryHandlerNativeBoolean  .class,
			BinaryHandlerNativeShort    .class,
			BinaryHandlerNativeCharacter.class,
			BinaryHandlerNativeInteger  .class,
			BinaryHandlerNativeFloat    .class,
			BinaryHandlerNativeLong     .class,
			BinaryHandlerNativeDouble   .class,
			BinaryHandlerNativeVoid     .class,
			BinaryHandlerNativeObject   .class,
			BinaryHandlerNativeString   .class,
			BinaryHandlerStringBuffer   .class,
			BinaryHandlerStringBuilder  .class,

			BinaryHandlerLazyReference.class,

			BinaryHandlerNativeArray_byte   .class,
			BinaryHandlerNativeArray_boolean.class,
			BinaryHandlerNativeArray_short  .class,
			BinaryHandlerNativeArray_char   .class,
			BinaryHandlerNativeArray_int    .class,
			BinaryHandlerNativeArray_float  .class,
			BinaryHandlerNativeArray_long   .class,
			BinaryHandlerNativeArray_double .class,

			BinaryHandlerArrayList          .class,
			BinaryHandlerBigInteger         .class,
			BinaryHandlerBigDecimal         .class,
			BinaryHandlerFile               .class,
			BinaryHandlerDate               .class,
			BinaryHandlerHashSet            .class,
			// (24.10.2013 TM)TODO: more native handlers (old collections etc.)

			BinaryHandlerNativeArray_byte   .class,
			BinaryHandlerNativeArray_boolean.class,
			BinaryHandlerNativeArray_short  .class,
			BinaryHandlerNativeArray_char   .class,
			BinaryHandlerNativeArray_int    .class,
			BinaryHandlerNativeArray_float  .class,
			BinaryHandlerNativeArray_long   .class,
			BinaryHandlerNativeArray_double .class,

			BinaryHandlerBulkList           .class,
			BinaryHandlerLimitList          .class,
			BinaryHandlerFixedList          .class,
			BinaryHandlerConstList          .class,
			BinaryHandlerEqBulkList         .class,
			BinaryHandlerHashEnum           .class,
			BinaryHandlerConstHashEnum      .class,
			BinaryHandlerEqHashEnum         .class,
			BinaryHandlerEqConstHashEnum    .class,
			BinaryHandlerHashTable          .class,
			BinaryHandlerConstHashTable     .class,
			BinaryHandlerEqHashTable        .class,
			BinaryHandlerEqConstHashTable   .class,

			BinaryHandlerSubstituterImplementation.class
			/* (29.10.2013 TM)TODO: more jadoth native handlers
			 * - VarString
			 * - VarByte
			 * - _intList etc.
			 */
		)
	;

	public static final void storeFixedSize(
		final Binary              bytes        ,
		final SwizzleFunction     persister    ,
		final long                length       ,
		final long                typeId       ,
		final long                objectId     ,
		final Object              instance     ,
		final long[]              memoryOffsets,
		final BinaryValueStorer[] storers
	)
	{
		long address = bytes.storeEntityHeader(length, typeId, objectId);
		for(int i = 0; i < memoryOffsets.length; i++)
		{
			address = storers[i].storeValueFromMemory(instance, memoryOffsets[i], address, persister);
		}
	}

	public static final void updateFixedSize(
		final Object                   instance     ,
		final BinaryValueSetter[]      setters      ,
		final long[]                   memoryOffsets,
		final long                     dataAddress  ,
		final SwizzleObjectIdResolving idResolver
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
		final SwizzleObjectIdResolving oidResolver
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
		final SwizzleFunction iterator,
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
		return address + BYTE_SIZE_4;
	}

	public static final long store_long(final long address, final long value)
	{
		VM.putLong(address, value);
		return address + BYTE_SIZE_8;
	}

	public static final void storeByte(final Binary bytes, final long tid, final long oid, final byte value)
	{
		VM.putByte(bytes.storeEntityHeader(BYTE_SIZE_1, tid, oid), value);
	}

	public static final void storeBoolean(final Binary bytes, final long tid, final long oid, final boolean value)
	{
		// where the heck is Unsafe#putBoolean(long, boolean)? Forgot to implement? Wtf?
		VM.putBoolean(null, bytes.storeEntityHeader(BYTE_SIZE_1, tid, oid), value);
	}

	public static final void storeShort(final Binary bytes, final long tid, final long oid, final short value)
	{
		VM.putShort(bytes.storeEntityHeader(BYTE_SIZE_2, tid, oid), value);
	}

	public static final void storeCharacter(final Binary bytes, final long tid, final long oid, final char value)
	{
		VM.putChar(bytes.storeEntityHeader(BYTE_SIZE_2, tid, oid), value);
	}

	public static final void storeInteger(final Binary bytes, final long tid, final long oid, final int value)
	{
		VM.putInt(bytes.storeEntityHeader(BYTE_SIZE_4, tid, oid), value);
	}

	public static final void storeFloat(final Binary bytes, final long tid, final long oid, final float value)
	{
		VM.putFloat(bytes.storeEntityHeader(BYTE_SIZE_4, tid, oid), value);
	}

	public static final void storeLong(final Binary bytes, final long tid, final long oid, final long value)
	{
		VM.putLong(bytes.storeEntityHeader(BYTE_SIZE_8, tid, oid), value);
	}

	public static final void storeDouble(final Binary bytes, final long tid, final long oid, final double value)
	{
		VM.putDouble(bytes.storeEntityHeader(BYTE_SIZE_8, tid, oid), value);
	}

	public static final void storeStateless(final Binary bytes, final long tid, final long oid)
	{
		bytes.storeEntityHeader(0L, tid, oid); // so funny :D
	}

	public static final void storeStringValue(final Binary bytes, final long tid, final long oid, final String string)
	{
		final char[] chars = Memory.accessChars(string); // thank god they fixed that stupid String storage mess
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
		final SwizzleFunction persister   ,
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
		final SwizzleFunction persister   ,
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
			Memory.set_long(elementsDataAddress + referenceBinaryLength(i), persister.apply(array[i]));
		}
	}

	public static final long storeListHeader(
		final long storeAddress        ,
		final long elementsBinaryLength,
		final long elementsCount
	)
	{
		Memory.set_long(storeAddress + LIST_OFFSET_LENGTH, calculateListTotalBinaryLength(elementsBinaryLength));
		Memory.set_long(storeAddress + LIST_OFFSET_COUNT , elementsCount);
		return storeAddress + LIST_OFFSET_ELEMENTS;
	}

	public static final void storeIterableContentAsList(
		final long            storeAddress,
		final SwizzleFunction persister   ,
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
			Memory.set_long(address, persister.apply(element));
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
		final SwizzleFunction                    persister   ,
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
			Memory.set_long(address                  , persister.apply(element.key())  );
			Memory.set_long(address + referenceLength, persister.apply(element.value()));
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
			elementsDataAddress = storeChars(elementsDataAddress, Memory.accessChars(strings[i]));
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
		final long elementsBinaryLength = length * Memory.byteSize_char();
		final long elementsDataAddress  = storeListHeader(storeAddress, elementsBinaryLength, length);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			Memory.set_char(elementsDataAddress + (i << 1), chars[i]);
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
			target[i] = Memory.wrapCharsAsString(BinaryPersistence.buildArray_char(elementAddress)); // build element
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
			array = new byte[Jadoth.checkArrayRange(elementCount)],
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
			array = new char[Jadoth.checkArrayRange(elementCount)],
			Unsafe.ARRAY_CHAR_BASE_OFFSET,
			elementCount << 1
		);
		return array;
	}

	// array restrukturierung marker //

	public static final byte[] createArray_byte(final Binary bytes)
	{
		return new byte[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new boolean[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new short[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new char[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new int[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new float[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return new long[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
	}

	public static final void updateArray_long(final long[] array, final Binary bytes)
	{
		Memory.copyRangeToArray(binaryArrayElementDataAddress(bytes.entityContentAddress), array);
	}

	public static final double[] createArray_double(final Binary bytes)
	{
		return new double[Jadoth.checkArrayRange(binaryArrayElementCount(bytes.entityContentAddress))];
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
		return Memory.wrapCharsAsString(buildArray_char(bytes));
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

	public static final long getEntityTypeId(final long entityAddress)
	{
		return VM.getLong(entityAddress + OFFSET_TID);
	}

	public static final long getEntityObjectId(final long entityAddress)
	{
		return VM.getLong(entityAddress + OFFSET_OID);
	}

	public static final long entityBinaryPosition(final long entityDataOffset)
	{
		return LENGTH_ENTITY_HEADER + entityDataOffset;
	}

	public static final long entityDataAddress(final long entityAddress)
	{
		return LENGTH_ENTITY_HEADER + entityAddress;
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
		return STORE_REF;
	}

	public static BinaryValueStorer getObjectValueStorer(final Class<?> type) throws IllegalArgumentException
	{
		// primitive special cases
		if(type.isPrimitive())
		{
			switch(Memory.byteSizePrimitive(type))
			{
				case  BYTE_SIZE_1: return STORE_1;
				case  BYTE_SIZE_2: return STORE_2;
				case  BYTE_SIZE_4: return STORE_4;
				case  BYTE_SIZE_8: return STORE_8;
				default: throw new IllegalArgumentException(); // void applies here
			}
		}

		// normal case of standard reference
		return STORE_REF;
	}

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
			switch(Memory.byteSizePrimitive(type))
			{
				case  BYTE_SIZE_1: return SETTER_1;
				case  BYTE_SIZE_2: return SETTER_2;
				case  BYTE_SIZE_4: return SETTER_4;
				case  BYTE_SIZE_8: return SETTER_8;
				default: throw new IllegalArgumentException(); // void applies here
			}
		}

		// normal case of standard reference
		return SETTER_REF;
	}

	public static BinaryValueEqualator getObjectValueEqualator(final Class<?> type)
	{
		if(type.isPrimitive())
		{
			switch(Memory.byteSizePrimitive(type))
			{
				case  BYTE_SIZE_1: return EQUAL_1;
				case  BYTE_SIZE_2: return EQUAL_2;
				case  BYTE_SIZE_4: return EQUAL_4;
				case  BYTE_SIZE_8: return EQUAL_8;
				default: throw new IllegalArgumentException(); // void applies here
			}
		}
		return EQUAL_REF;
	}

	static final int binaryValueSize(final Class<?> type)
	{
		return type.isPrimitive() ? Memory.byteSizePrimitive(type) : LENGTH_OID;
	}

	public static int[] calculateBinarySizes(final XGettingSequence<Field> fields)
	{
		final int[] fieldOffsets = new int[Jadoth.to_int(fields.size())];
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
		final SwizzleObjectIdResolving oidResolver
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
		final SwizzleObjectIdResolving oidResolver ,
		final Object[]                 array       ,
		final int                      offset      ,
		final int                      length
	)
	{
		if(BinaryPersistence.getListElementCount(bytes) < length)
		{
			throw new BinaryPersistenceExceptionStateArrayLength(
				array,
				Jadoth.checkArrayRange(BinaryPersistence.getListElementCount(bytes, binaryOffset))
			);
		}
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			array[offset + i] = oidResolver.lookupObject(
				Memory.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public static final void collectElementsIntoArray(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final SwizzleObjectIdResolving oidResolver ,
		final Object[]                 target
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < target.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			target[i] = oidResolver.lookupObject(
				Memory.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public static final int collectListObjectReferences(
		final Binary                   bytes       ,
		final long                     binaryOffset,
		final SwizzleObjectIdResolving oidResolver ,
		final Consumer<Object>        collector
	)
	{
		final int size = Jadoth.checkArrayRange(BinaryPersistence.getListElementCount(bytes, binaryOffset));
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
		final SwizzleObjectIdResolving oidResolver ,
		final Consumer<Object>         collector
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				oidResolver.lookupObject(
					Memory.get_long(binaryElementsStartAddress + referenceBinaryLength(i))
				)
			);
		}
	}

	public static final int collectKeyValueReferences(
		final Binary                      bytes       ,
		final long                        binaryOffset,
		final int                         length      ,
		final SwizzleObjectIdResolving    oidResolver ,
		final BiProcedure<Object, Object> collector
	)
	{
		final long binaryElementsStartAddress = BinaryPersistence.getListElementsAddress(bytes, binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				// key (on every nth oid position)
				oidResolver.lookupObject(
					Memory.get_long(binaryElementsStartAddress + referenceBinaryLength(i << 1))
				),
				// value (on every (n + 1)th oid position)
				oidResolver.lookupObject(
					Memory.get_long(binaryElementsStartAddress + referenceBinaryLength(i << 1) + LENGTH_OID)
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

	public static BinaryPersistenceFoundation factory()
	{
		return factory(null);
	}

	public static BinaryPersistenceFoundation factory(final Dispatcher dispatcher)
	{
		final BinaryPersistenceFoundation.Implementation factory =
			new BinaryPersistenceFoundation.Implementation()
			.setInstanceDispatcher(dispatcher)
		;
		return factory;
	}

	public static final long readChunkLength(
		final ByteBuffer          lengthBuffer,
		final ReadableByteChannel channel     ,
		final MessageWaiter       messageWaiter
	)
		throws IOException
	{
		// not complicated to read a long from a channel. Not complicated at all. Just crap.
		lengthBuffer.clear().limit(LENGTH_LONG); // too dumb to write a properly typed chaining method, no joke.
		fillBuffer(lengthBuffer, channel, messageWaiter);
//		return lengthBuffer.getLong();
		/* OMG they convert every single primitive to big endian, even if it's just from the same machine
		 * to the same machine. With checking global "aligned" state like noobs and what not.
		 * Giant runtime effort ruining everything just to avoid caring about / communicating local endianess.
		 * Which is especially stupid as 90% of all machines are little endian anyway.
		 * Who cares about negligible overpriced SUN hardware and other exotics.
		 * They simply have to synchronize endianess in network communication via communication protocol.
		 * Messing up the standard case with RUNTIME effort just for those is so stupid I can't tell.
		 */

		// good thing is: doing it manually gets rid of the clumsy flipping in this case
		return Memory.get_long(Memory.directByteBufferAddress(lengthBuffer));
	}

	public static final void fillBuffer(
		final ByteBuffer          buffer       ,
		final ReadableByteChannel channel      ,
		final MessageWaiter       messageWaiter
	)
		throws IOException
	{
		while(true)
		{
			final int readCount;
			if((readCount = channel.read(buffer)) < 0 && buffer.hasRemaining())
			{
				throw new BinaryPersistenceExceptionIncompleteChunk(buffer.position(), buffer.limit());
			}
			if(!buffer.hasRemaining())
			{
				break; // chunk complete, stop reading without calling waiter again
			}
			messageWaiter.waitForBytes(readCount);
		}
		// intentionally no flipping here.
	}


	public static final float get_float(final Binary bytes, final long offset)
	{
		return Memory.get_float(bytes.entityContentAddress + offset);
	}

	public static final int get_int(final Binary bytes, final long offset)
	{
		return Memory.get_int(bytes.entityContentAddress + offset);
	}

	// (28.10.2013 TM)XXX: move all these xxx(Binary, ...) methods to Binary class directly? Possible objections?
	public static final long get_long(final Binary bytes, final long offset)
	{
		return Memory.get_long(bytes.entityContentAddress + offset);
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
		return Memory.byteSizePrimitive(primitiveType);
	}

	public static final BinaryFieldLengthResolver createFieldLengthResolver()
	{
		return new BinaryFieldLengthResolver.Implementation();
	}
	
	public static final PersistenceTypeDescriptionBuilder createTypeDescriptionBuilder()
	{
		return PersistenceTypeDescriptionBuilder.New(PersistenceTypeResolver.Failing());
	}

	public static PersistenceTypeDictionaryProvider createTypeDictionaryProviderFromFile(final File dictionaryFile)
	{
		final PersistenceTypeDictionaryProvider typeDictionaryProvider =
			PersistenceTypeDictionaryProvider.NewFromFile(
				dictionaryFile                ,
				createFieldLengthResolver()   ,
				createTypeDescriptionBuilder()
			)
		;
		return typeDictionaryProvider;
	}

	public static PersistenceTypeDictionary provideTypeDictionaryFromFile(final File dictionaryFile)
	{
		final PersistenceTypeDictionaryProvider dp = createTypeDictionaryProviderFromFile(dictionaryFile);
		return dp.provideDictionary();
	}

}
