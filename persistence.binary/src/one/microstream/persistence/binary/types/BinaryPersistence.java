package one.microstream.persistence.binary.types;

import java.io.File;
import java.lang.reflect.Field;

import one.microstream.collections.BinaryHandlerBulkList;
import one.microstream.collections.BinaryHandlerConstHashEnum;
import one.microstream.collections.BinaryHandlerConstHashTable;
import one.microstream.collections.BinaryHandlerConstList;
import one.microstream.collections.BinaryHandlerEqBulkList;
import one.microstream.collections.BinaryHandlerEqConstHashEnum;
import one.microstream.collections.BinaryHandlerEqConstHashTable;
import one.microstream.collections.BinaryHandlerEqHashEnum;
import one.microstream.collections.BinaryHandlerEqHashTable;
import one.microstream.collections.BinaryHandlerFixedList;
import one.microstream.collections.BinaryHandlerHashEnum;
import one.microstream.collections.BinaryHandlerHashTable;
import one.microstream.collections.BinaryHandlerLimitList;
import one.microstream.collections.ConstList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.exceptions.InstantiationRuntimeException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.BinaryHandlerArrayList;
import one.microstream.persistence.binary.internal.BinaryHandlerBigDecimal;
import one.microstream.persistence.binary.internal.BinaryHandlerBigInteger;
import one.microstream.persistence.binary.internal.BinaryHandlerDate;
import one.microstream.persistence.binary.internal.BinaryHandlerFile;
import one.microstream.persistence.binary.internal.BinaryHandlerHashSet;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_boolean;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_byte;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_char;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_double;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_float;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_int;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_long;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeArray_short;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeBoolean;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeByte;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeCharacter;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeDouble;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeFloat;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeInteger;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeLong;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeObject;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeShort;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeString;
import one.microstream.persistence.binary.internal.BinaryHandlerNativeVoid;
import one.microstream.persistence.binary.internal.BinaryHandlerPrimitive;
import one.microstream.persistence.binary.internal.BinaryHandlerStringBuffer;
import one.microstream.persistence.binary.internal.BinaryHandlerStringBuilder;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.lazy.BinaryHandlerLazyReference;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeIdLookup;
import one.microstream.typing.XTypes;
import one.microstream.util.BinaryHandlerSubstituterImplementation;

public final class BinaryPersistence extends Persistence
{
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
	
	public static final PersistenceCustomTypeHandlerRegistry<Binary> createDefaultCustomTypeHandlerRegistry(
		final PersistenceTypeIdLookup               nativeTypeIdLookup,
		final PersistenceSizedArrayLengthController controller
	)
	{
		final PersistenceCustomTypeHandlerRegistry.Implementation<Binary> defaultCustomTypeHandlerRegistry =
			PersistenceCustomTypeHandlerRegistry.<Binary>New()
			.registerTypeHandlers(createNativeHandlers(nativeTypeIdLookup, controller))
			.registerTypeHandlers(defaultCustomHandlers(controller))
		;
		return defaultCustomTypeHandlerRegistry;
	}
		
	static final void initializeNativeTypeId(
		final PersistenceTypeHandler<Binary, ?> typeHandler       ,
		final PersistenceTypeIdLookup           nativeTypeIdLookup
	)
	{
		final long nativeTypeId = nativeTypeIdLookup.lookupTypeId(typeHandler.type());
		if(nativeTypeId == 0)
		{
			// (07.11.2018 TM)EXCP: proper exception
			throw new RuntimeException("No native TypeId found for type " + typeHandler.type());
		}
		
		typeHandler.initializeTypeId(nativeTypeId);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> createNativeHandlers(
		final PersistenceTypeIdLookup               nativeTypeIdLookup,
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
		
		nativeHandlers.iterate(handler ->
		{
			BinaryPersistence.initializeNativeTypeId(handler, nativeTypeIdLookup);
		});
		
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
	
	public static final long resolveFieldBinaryLength(final Class<?> fieldType)
	{
		return fieldType.isPrimitive()
			? resolvePrimitiveFieldBinaryLength(fieldType)
			: Binary.oidByteLength()
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
	
	static final int binaryValueSize(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: Binary.oidByteLength()
		;
	}

	public static int[] calculateBinarySizes(final XGettingSequence<Field> fields)
	{
		final int[] fieldOffsets = new int[XTypes.to_int(fields.size())];
		fields.iterateIndexed(new IndexedAcceptor<Field>()
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

	public static final void iterateInstanceReferences(
		final PersistenceFunction iterator,
		final Object              instance,
		final long[]      referenceOffsets
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

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private BinaryPersistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
