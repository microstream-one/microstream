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
import net.jadoth.persistence.types.PersistenceSizedArrayLengthController;
import net.jadoth.persistence.types.PersistenceTypeDictionary;
import net.jadoth.persistence.types.PersistenceTypeHandler;
import net.jadoth.typing.XTypes;
import net.jadoth.util.BinaryHandlerSubstituterImplementation;

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
