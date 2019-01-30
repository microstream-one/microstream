package net.jadoth.persistence.binary.types;

import java.io.File;
import java.lang.reflect.Field;

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
import net.jadoth.memory.XMemory;
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
import net.jadoth.persistence.lazy.BinaryHandlerLazyReference;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceObjectIdResolver;
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.typing.XTypes;
import net.jadoth.util.BinaryHandlerSubstituterImplementation;

public final class BinaryPersistence extends Persistence
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	static final int
		LENGTH_LEN  = Long.BYTES,
		LENGTH_OID  = Long.BYTES,
		LENGTH_TID  = Long.BYTES
	;
	
	static final long
		OFFSET_LEN = 0L                     ,
		OFFSET_TID = OFFSET_LEN + LENGTH_LEN,
		OFFSET_OID = OFFSET_TID + LENGTH_TID,
		OFFSET_DAT = OFFSET_OID + LENGTH_OID
	;
	
	// header (currently) constists of only LEN, TID, OID. The extra constant has sementical reasons.
	static final int LENGTH_ENTITY_HEADER = (int)OFFSET_DAT;

	/* (29.01.2019 TM)TODO: test and comment bit shifting multiplication performance
	 * test and comment if this really makes a difference in performance.
	 * The redundant length is ugly.
	 */
	/**
	 * "<< 3" is a performance optimization for "* 8".
	 */
	private static final int LONG_BYTE_LENGTH_BITSHIFT_COUNT = 3;


		
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	/**
	 * @return the length in bytes of a peristent item's length field (8 bytes).
	 */
	public static final int lengthLength()
	{
		return LENGTH_LEN;
	}

	public static final boolean isValidGapLength(final long gapLength)
	{
		// gap total length cannot indicate less then its own length (length of the length field, 1 long)
		return gapLength >= LENGTH_LEN;
	}

	public static final boolean isValidEntityLength(final long entityLength)
	{
		return entityLength >= LENGTH_ENTITY_HEADER;
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
		return XMemory.get_long(entityAddress + OFFSET_TID);
	}

	public static final long getEntityObjectId(final long entityAddress)
	{
		return XMemory.get_long(entityAddress + OFFSET_OID);
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
		XMemory.set_long(address + OFFSET_LEN, entityLength  );
		XMemory.set_long(address + OFFSET_TID, entityTypeId  );
		XMemory.set_long(address + OFFSET_OID, entityObjectId);
	}

	public static final long oidByteLength()
	{
		return LENGTH_OID;
	}
		
	public static final long entityAddressFromContentAddress(final long entityContentAddress)
	{
		return entityContentAddress - LENGTH_ENTITY_HEADER;
	}
		
	private static final BinaryValueStorer STORE_1 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object         src      ,
			final long           srcOffset,
			final long           address  ,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_byte(address, XMemory.get_byte(src, srcOffset));
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
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_short(address, XMemory.get_short(src, srcOffset));
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
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_int(address, XMemory.get_int(src, srcOffset));
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
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(address, XMemory.get_long(src, srcOffset));
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
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(address, handler.apply(XMemory.getObject(src, srcOffset)));
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
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(targetAddress, handler.applyEager(XMemory.getObject(source, sourceOffset)));
			return targetAddress + LENGTH_OID;
		}
	};

	private static final BinaryValueSetter SETTER_1 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                      sourceAddress,
			final Object                    target       ,
			final long                      targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_byte(target, targetOffset, XMemory.get_byte(sourceAddress));
			return sourceAddress + Byte.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_2 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                      sourceAddress,
			final Object                    target       ,
			final long                      targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_short(target, targetOffset, XMemory.get_short(sourceAddress));
			return sourceAddress + Short.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_4 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                      sourceAddress,
			final Object                    target       ,
			final long                      targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_int(target, targetOffset, XMemory.get_int(sourceAddress));
			return sourceAddress + Integer.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_8 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                      sourceAddress,
			final Object                    target       ,
			final long                      targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_long(target, targetOffset, XMemory.get_long(sourceAddress));
			return sourceAddress + Long.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_REF = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                      sourceAddress,
			final Object                    target       ,
			final long                      targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.setObject(target, targetOffset, idResolver.lookupObject(XMemory.get_long(sourceAddress)));
			return sourceAddress + LENGTH_OID;
		}
	};

	private static final BinaryValueEqualator EQUAL_1 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                    src        ,
			final long                      srcOffset  ,
			final long                      address    ,
			final PersistenceObjectIdResolver oidResolver
		)
		{
			return XMemory.get_byte(src, srcOffset) == XMemory.get_byte(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_2 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                    src        ,
			final long                      srcOffset  ,
			final long                      address    ,
			final PersistenceObjectIdResolver oidResolver
		)
		{
			return XMemory.get_short(src, srcOffset) == XMemory.get_short(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_4 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                    src        ,
			final long                      srcOffset  ,
			final long                      address    ,
			final PersistenceObjectIdResolver oidResolver
		)
		{
			return XMemory.get_int(src, srcOffset) == XMemory.get_int(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_8 = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                    src        ,
			final long                      srcOffset  ,
			final long                      address    ,
			final PersistenceObjectIdResolver oidResolver
		)
		{
			return XMemory.get_long(src, srcOffset) == XMemory.get_long(address);
		}
	};

	private static final BinaryValueEqualator EQUAL_REF = new BinaryValueEqualator()
	{
		@Override
		public boolean equalValue(
			final Object                    src        ,
			final long                      srcOffset  ,
			final long                      address    ,
			final PersistenceObjectIdResolver oidResolver
		)
		{
			return XMemory.getObject(src, srcOffset) == oidResolver.lookupObject(XMemory.get_long(address));
		}
	};

	public static final PersistenceCustomTypeHandlerRegistry<Binary> createDefaultCustomTypeHandlerRegistry(
		final PersistenceSizedArrayLengthController controller
	)
	{
		final PersistenceCustomTypeHandlerRegistry.Implementation<Binary> defaultCustomTypeHandlerRegistry =
			PersistenceCustomTypeHandlerRegistry.<Binary>New()
			.registerTypeHandlers(nativeHandlers(controller))
			.registerTypeHandlers(defaultCustomHandlers(controller))
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
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlers(
		final PersistenceSizedArrayLengthController controller
	)
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

			new BinaryHandlerArrayList(controller) ,
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
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> defaultCustomHandlers(
		final PersistenceSizedArrayLengthController controller
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> defaultHandlers = ConstList.New(
			new BinaryHandlerBulkList(controller)  ,
			new BinaryHandlerLimitList(controller) ,
			new BinaryHandlerFixedList()           ,
			new BinaryHandlerConstList()           ,
			new BinaryHandlerEqBulkList(controller),
			new BinaryHandlerHashEnum()            ,
			new BinaryHandlerConstHashEnum()       ,
			new BinaryHandlerEqHashEnum()          ,
			new BinaryHandlerEqConstHashEnum()     ,
			new BinaryHandlerHashTable()           ,
			new BinaryHandlerConstHashTable()      ,
			new BinaryHandlerEqHashTable()         ,
			new BinaryHandlerEqConstHashTable()    ,

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
	


	public static final void updateFixedSize(
		final Object                      instance     ,
		final BinaryValueSetter[]         setters      ,
		final long[]                      memoryOffsets,
		final long                        dataAddress  ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		long address = dataAddress;
		for(int i = 0; i < setters.length; i++)
		{
			address = setters[i].setValueToMemory(address, instance, memoryOffsets[i], idResolver);
		}
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
				iterator.apply(XMemory.getObject(instance, referenceOffsets[i]));
			}
		}
	}

	// (28.10.2013 TM)XXX: consolidate "List~" naming with array (see "SizedArray").

	static final void iterateReferenceRange(
		final long           address ,
		final long           count   ,
		final _longProcedure iterator
	)
	{
		final long boundAddress = address + count * LENGTH_OID;
		for(long a = address; a < boundAddress; a += LENGTH_OID)
		{
			iterator.accept(XMemory.get_long(a));
		}
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
			iterator.accept(XMemory.get_long(a + elementOffset));
		}
	}



	public static final long store_int(final long address, final int value)
	{
		XMemory.set_int(address, value);
		return address + Integer.BYTES;
	}

	public static final long store_long(final long address, final long value)
	{
		XMemory.set_long(address, value);
		return address + Long.BYTES;
	}


	public static final long referenceBinaryLength(final long referenceCount)
	{
		return referenceCount << LONG_BYTE_LENGTH_BITSHIFT_COUNT; // reference (ID) binary length is 8
	}

	


	public static final long getEntityLength(final long entityAddress)
	{
		// (06.09.2014)TODO: test and comment if " + 0L" gets eliminated by JIT
		return XMemory.get_long(entityAddress + OFFSET_LEN);
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
			switch(XMemory.byteSizePrimitive(type))
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
			switch(XMemory.byteSizePrimitive(type))
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
			switch(XMemory.byteSizePrimitive(type))
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
		return type.isPrimitive() ? XMemory.byteSizePrimitive(type) : LENGTH_OID;
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



	public static final <T> T blankMemoryInstantiate(final Class<T> type)
	{
		return XMemory.instantiate(type);
	}

	public static final <T> BinaryInstantiator<T> blankMemoryInstantiator(final Class<T> type)
	{
		return new BinaryInstantiator<T>()
		{
			@Override
			public T newInstance(final long buildItemAddress) throws InstantiationRuntimeException
			{
				return BinaryPersistence.blankMemoryInstantiate(type);
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


	public static final long resolveFieldBinaryLength(final Class<?> fieldType)
	{
		return fieldType.isPrimitive()
			? resolvePrimitiveFieldBinaryLength(fieldType)
			: oidByteLength()
		;
	}

	public static final long resolvePrimitiveFieldBinaryLength(final Class<?> primitiveType)
	{
		return XMemory.byteSizePrimitive(primitiveType);
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
