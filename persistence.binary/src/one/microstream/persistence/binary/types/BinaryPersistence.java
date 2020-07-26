package one.microstream.persistence.binary.types;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import one.microstream.afs.AFile;
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
import one.microstream.collections.BinaryHandlerSingleton;
import one.microstream.collections.ConstList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.functional.InstanceDispatcherLogic;
import one.microstream.java.io.BinaryHandlerFile;
import one.microstream.java.lang.BinaryHandlerBoolean;
import one.microstream.java.lang.BinaryHandlerByte;
import one.microstream.java.lang.BinaryHandlerCharacter;
import one.microstream.java.lang.BinaryHandlerClass;
import one.microstream.java.lang.BinaryHandlerDouble;
import one.microstream.java.lang.BinaryHandlerFloat;
import one.microstream.java.lang.BinaryHandlerInteger;
import one.microstream.java.lang.BinaryHandlerLong;
import one.microstream.java.lang.BinaryHandlerNativeArray_boolean;
import one.microstream.java.lang.BinaryHandlerNativeArray_byte;
import one.microstream.java.lang.BinaryHandlerNativeArray_char;
import one.microstream.java.lang.BinaryHandlerNativeArray_double;
import one.microstream.java.lang.BinaryHandlerNativeArray_float;
import one.microstream.java.lang.BinaryHandlerNativeArray_int;
import one.microstream.java.lang.BinaryHandlerNativeArray_long;
import one.microstream.java.lang.BinaryHandlerNativeArray_short;
import one.microstream.java.lang.BinaryHandlerObject;
import one.microstream.java.lang.BinaryHandlerShort;
import one.microstream.java.lang.BinaryHandlerString;
import one.microstream.java.lang.BinaryHandlerStringBuffer;
import one.microstream.java.lang.BinaryHandlerStringBuilder;
import one.microstream.java.lang.BinaryHandlerVoid;
import one.microstream.java.math.BinaryHandlerBigDecimal;
import one.microstream.java.math.BinaryHandlerBigInteger;
import one.microstream.java.net.BinaryHandlerInet4Address;
import one.microstream.java.net.BinaryHandlerInet6Address;
import one.microstream.java.net.BinaryHandlerInetAddress;
import one.microstream.java.net.BinaryHandlerInetSocketAddress;
import one.microstream.java.net.BinaryHandlerURI;
import one.microstream.java.net.BinaryHandlerURL;
import one.microstream.java.nio.file.BinaryHandlerPath;
import one.microstream.java.sql.BinaryHandlerSqlDate;
import one.microstream.java.sql.BinaryHandlerSqlTime;
import one.microstream.java.sql.BinaryHandlerSqlTimestamp;
import one.microstream.java.util.BinaryHandlerArrayDeque;
import one.microstream.java.util.BinaryHandlerArrayList;
import one.microstream.java.util.BinaryHandlerCopyOnWriteArrayList;
import one.microstream.java.util.BinaryHandlerCurrency;
import one.microstream.java.util.BinaryHandlerDate;
import one.microstream.java.util.BinaryHandlerHashMap;
import one.microstream.java.util.BinaryHandlerHashSet;
import one.microstream.java.util.BinaryHandlerHashtable;
import one.microstream.java.util.BinaryHandlerIdentityHashMap;
import one.microstream.java.util.BinaryHandlerLinkedHashMap;
import one.microstream.java.util.BinaryHandlerLinkedHashSet;
import one.microstream.java.util.BinaryHandlerLinkedList;
import one.microstream.java.util.BinaryHandlerLocale;
import one.microstream.java.util.BinaryHandlerOptionalDouble;
import one.microstream.java.util.BinaryHandlerOptionalInt;
import one.microstream.java.util.BinaryHandlerOptionalLong;
import one.microstream.java.util.BinaryHandlerPriorityQueue;
import one.microstream.java.util.BinaryHandlerProperties;
import one.microstream.java.util.BinaryHandlerStack;
import one.microstream.java.util.BinaryHandlerTreeMap;
import one.microstream.java.util.BinaryHandlerTreeSet;
import one.microstream.java.util.BinaryHandlerVector;
import one.microstream.java.util.BinaryHandlerWeakHashMap;
import one.microstream.java.util.concurrent.BinaryHandlerConcurrentHashMap;
import one.microstream.java.util.concurrent.BinaryHandlerConcurrentLinkedDeque;
import one.microstream.java.util.concurrent.BinaryHandlerConcurrentLinkedQueue;
import one.microstream.java.util.concurrent.BinaryHandlerConcurrentSkipListMap;
import one.microstream.java.util.concurrent.BinaryHandlerConcurrentSkipListSet;
import one.microstream.java.util.regex.BinaryHandlerPattern;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.BinaryHandlerPrimitive;
import one.microstream.persistence.binary.internal.BinaryHandlerSingletonStatelessEnum;
import one.microstream.persistence.binary.internal.BinaryHandlerStatelessConstant;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.internal.PersistenceTypeDictionaryFileHandler;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceSizedArrayLengthController;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerCreator;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.persistence.types.PersistenceTypeIdLookup;
import one.microstream.reference.BinaryHandlerLazyDefault;
import one.microstream.reference.Referencing;
import one.microstream.reference.Swizzling;
import one.microstream.typing.XTypes;
import one.microstream.util.BinaryHandlerSubstituterDefault;

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
		final Referencing<PersistenceTypeHandlerManager<Binary>>              typeHandlerManager,
		final PersistenceSizedArrayLengthController                           controller        ,
		final PersistenceTypeHandlerCreator<Binary>                           typeHandlerCreator,
		final XGettingCollection<? extends PersistenceTypeHandler<Binary, ?>> customHandlers
	)
	{
		/* (16.10.2019 TM)NOTE:
		 * Native handlers are split into value and referencing types since plugins that handle references
		 * differently (e.g. load all only on demand, like a data viewer REST service) can reuse the value
		 * type handlers but need to replace the referencing type handlers.
		 */
		final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersValueTypes =
			createNativeHandlersValueTypes(typeHandlerManager, controller, typeHandlerCreator)
		;
		final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersReferencingTypes =
			createNativeHandlersReferencingTypes(typeHandlerManager, controller, typeHandlerCreator)
		;
		
		final PersistenceCustomTypeHandlerRegistry.Default<Binary> defaultCustomTypeHandlerRegistry =
			PersistenceCustomTypeHandlerRegistry.<Binary>New()
			.registerTypeHandlers(nativeHandlersValueTypes)
			.registerTypeHandlers(nativeHandlersReferencingTypes)
			.registerTypeHandlers(defaultCustomHandlers(controller))
			.registerTypeHandlers(customHandlers)
		;
		
		return defaultCustomTypeHandlerRegistry;
	}
		
	static final void initializeNativeTypeId(
		final PersistenceTypeHandler<Binary, ?> typeHandler       ,
		final PersistenceTypeIdLookup           nativeTypeIdLookup
	)
	{
		final long nativeTypeId = nativeTypeIdLookup.lookupTypeId(typeHandler.type());
		if(Swizzling.isNotFoundId(nativeTypeId))
		{
			// (07.11.2018 TM)EXCP: proper exception
			throw new PersistenceException("No native TypeId found for type " + typeHandler.type());
		}
		
		typeHandler.initialize(nativeTypeId);
	}
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> createNativeHandlersValueTypes(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceSizedArrayLengthController              controller        ,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlersValueTypes = ConstList.New(
			BinaryHandlerPrimitive.New(byte   .class),
			BinaryHandlerPrimitive.New(boolean.class),
			BinaryHandlerPrimitive.New(short  .class),
			BinaryHandlerPrimitive.New(char   .class),
			BinaryHandlerPrimitive.New(int    .class),
			BinaryHandlerPrimitive.New(float  .class),
			BinaryHandlerPrimitive.New(long   .class),
			BinaryHandlerPrimitive.New(double .class),

			BinaryHandlerClass.New(typeHandlerManager),
			
			BinaryHandlerByte.New()     ,
			BinaryHandlerBoolean.New()  ,
			BinaryHandlerShort.New()    ,
			BinaryHandlerCharacter.New(),
			BinaryHandlerInteger.New()  ,
			BinaryHandlerFloat.New()    ,
			BinaryHandlerLong.New()     ,
			BinaryHandlerDouble.New()   ,
			BinaryHandlerVoid.New()     ,
			BinaryHandlerObject.New()   ,
			
			BinaryHandlerString.New()       ,
			BinaryHandlerStringBuffer.New() ,
			BinaryHandlerStringBuilder.New(),

			BinaryHandlerNativeArray_byte.New()   ,
			BinaryHandlerNativeArray_boolean.New(),
			BinaryHandlerNativeArray_short.New()  ,
			BinaryHandlerNativeArray_char.New()   ,
			BinaryHandlerNativeArray_int.New()    ,
			BinaryHandlerNativeArray_float.New()  ,
			BinaryHandlerNativeArray_long.New()   ,
			BinaryHandlerNativeArray_double.New() ,
			
			BinaryHandlerBigInteger.New(),
			BinaryHandlerBigDecimal.New(),
			
			BinaryHandlerFile.New()    ,
			BinaryHandlerDate.New()    ,
			BinaryHandlerLocale.New()  ,
			BinaryHandlerCurrency.New(),
			BinaryHandlerPattern.New() ,
			
			BinaryHandlerInetAddress.New() ,
			BinaryHandlerInet4Address.New(),
			BinaryHandlerInet6Address.New(),
			
			BinaryHandlerPath.New(), // "abstract type" TypeHandler
			
			BinaryHandlerInetSocketAddress.New(),
			
			BinaryHandlerURI.New(),
			BinaryHandlerURL.New(),

			// non-sensical handlers required for confused developers
			BinaryHandlerSqlDate.New()     ,
			BinaryHandlerSqlTime.New()     ,
			BinaryHandlerSqlTimestamp.New(),
			
			BinaryHandlerOptionalInt.New(),
			BinaryHandlerOptionalLong.New(),
			BinaryHandlerOptionalDouble.New(),
			
			/* (12.11.2019 TM)NOTE:
			 * One might think that "empty" implementations of a collection interface would have no fields, anyway.
			 * But no-ho-ho, not when the JDK geniuses are involved.
			 * The horrible, unexplicably stupidly written code for those classes brainlessly and uselessly
			 * extends 5 other classes, some of which bring along several times redundant delegate fields.
			 * Those fields cause access warnings (and access exceptions in the future) when trying to set them
			 * accessible in the generic handler implementation.
			 * To avoid all that hassle and compensate their intern-like design skills when it comes to interfaces
			 * (and Java in general), it is necessary to explicitly define stateless handlers for those
			 * pseudo-stateless empty types with useless fields.
			 * Rant over. Yes, I feel much better now.
			 * 
			 * Also, to avoid an erroneous instance creation that BinaryHandlerStateless might perform
			 * (e.g. when using a dummy object registry as tools might do), the constant instance itself
			 * has to be returned in case the create should ever be invoked.
			 */
			BinaryHandlerStatelessConstant.New(Collections.emptyNavigableSet()),
			BinaryHandlerStatelessConstant.New(Collections.emptyNavigableMap()),
			
			// not an enum, as opposed to NaturalOrderComparator.
			BinaryHandlerStatelessConstant.New(Collections.reverseOrder())
		);
		
		/* (24.10.2013 TM)TODO: priv#117 more native handlers (Path, Instant and whatnot)
		 * Also see class Persistence for default TypeIds
		 */
		
		return nativeHandlersValueTypes;
	}
	
	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> createNativeHandlersReferencingTypes(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager,
		final PersistenceSizedArrayLengthController              controller        ,
		final PersistenceTypeHandlerCreator<Binary>              typeHandlerCreator
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> nativeHandlers = ConstList.New(
			
			// creepy JDK 1.0 collections
			BinaryHandlerVector.New()               ,
			BinaryHandlerStack.New()                ,
			BinaryHandlerHashtable.New()            ,
			BinaryHandlerProperties.New()           ,
			
			// still creepy JDK 1.2 collections
			BinaryHandlerArrayList.New(),
			BinaryHandlerHashSet.New()              ,
			BinaryHandlerHashMap.New()              ,
			BinaryHandlerWeakHashMap.New()          ,
			BinaryHandlerLinkedList.New()           ,
			BinaryHandlerTreeMap.New()              ,
			BinaryHandlerTreeSet.New()              ,
			
			// still creepy JDK 1.4 collections
			BinaryHandlerIdentityHashMap.New()      ,
			BinaryHandlerLinkedHashMap.New()        ,
			BinaryHandlerLinkedHashSet.New()        ,
			
			// still creepy JDK 1.5 collections
			BinaryHandlerPriorityQueue.New()        ,
			BinaryHandlerConcurrentHashMap.New()    ,
			BinaryHandlerConcurrentLinkedQueue.New(),
			BinaryHandlerCopyOnWriteArrayList.New() ,
			
			// remaining JDK collections (wrappers and the like) are handled dynamically
			
			/*
			 * Would work for these, but Iterators are generally unpersistable for good reason.
			 * See Persistence#unpersistableTypes
			 */
//			BinaryHandlerStateless.New(Collections.emptyEnumeration().getClass()),
//			BinaryHandlerStateless.New(Collections.emptyIterator().getClass()),
//			BinaryHandlerStateless.New(Collections.emptyListIterator().getClass()),
			
			// changed with support of enums. And must change to keep TypeDictionary etc. consistent
			BinaryHandlerSingletonStatelessEnum.New(Comparator.naturalOrder().getClass()),
//			typeHandlerCreator.createTypeHandler(Comparator.naturalOrder().getClass()),
//			BinaryHandlerStateless.New(Comparator.naturalOrder().getClass()),
						
			// still creepy JDK 1.6 collections
			BinaryHandlerArrayDeque.New()           ,
			BinaryHandlerConcurrentSkipListMap.New(),
			BinaryHandlerConcurrentSkipListSet.New(),
			
			// still creepy JDK 1.7 collections
			BinaryHandlerConcurrentLinkedDeque.New(),

			BinaryHandlerLazyDefault.New(),
			
			// the way Optional is implemented, only a generically (low-level) working handler can handle it correctly
			typeHandlerCreator.createTypeHandlerGeneric(Optional.class)
		);
		
		return nativeHandlers;
	}

	public static final XGettingSequence<? extends PersistenceTypeHandler<Binary, ?>> defaultCustomHandlers(
		final PersistenceSizedArrayLengthController controller
	)
	{
		final ConstList<? extends PersistenceTypeHandler<Binary, ?>> defaultHandlers = ConstList.New(
			BinaryHandlerBulkList.New(controller)   ,
			BinaryHandlerLimitList.New(controller)  ,
			BinaryHandlerFixedList.New()            ,
			BinaryHandlerConstList.New()            ,
			BinaryHandlerEqBulkList.New(controller) ,
			BinaryHandlerHashEnum.New()             ,
			BinaryHandlerConstHashEnum.New()        ,
			BinaryHandlerEqHashEnum.New()           ,
			BinaryHandlerEqConstHashEnum.New()      ,
			BinaryHandlerHashTable.New()            ,
			BinaryHandlerConstHashTable.New()       ,
			BinaryHandlerEqHashTable.New()          ,
			BinaryHandlerEqConstHashTable.New()     ,
			BinaryHandlerSingleton.New()            ,
			BinaryHandlerSubstituterDefault.New()
			/* (29.10.2013 TM)TODO: more MicroStream default custom handlers
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
			: Binary.objectIdByteLength()
		;
	}

	public static final long resolvePrimitiveFieldBinaryLength(final Class<?> primitiveType)
	{
		return XMemory.byteSizePrimitive(primitiveType);
	}

	public static final BinaryFieldLengthResolver createFieldLengthResolver()
	{
		return new BinaryFieldLengthResolver.Default();
	}
		
	public static PersistenceTypeDictionary provideTypeDictionaryFromFile(final AFile dictionaryFile)
	{
		final BinaryPersistenceFoundation<?> f = BinaryPersistenceFoundation.New()
			.setTypeDictionaryLoader(
				PersistenceTypeDictionaryFileHandler.New(dictionaryFile)
			)
		;
		return f.getTypeDictionaryProvider().provideTypeDictionary();
	}
	
	public static final int binaryValueSize(final Class<?> type)
	{
		return type.isPrimitive()
			? XMemory.byteSizePrimitive(type)
			: Binary.objectIdByteLength()
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

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private BinaryPersistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
