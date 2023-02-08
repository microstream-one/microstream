package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.afs.types.AFS;
import one.microstream.afs.types.AFile;
import one.microstream.chars.StringTable;
import one.microstream.chars.XChars;
import one.microstream.collections.BulkList;
import one.microstream.collections.ConstHashEnum;
import one.microstream.collections.HashEnum;
import one.microstream.collections.HashTable;
import one.microstream.collections.interfaces.ChainStorage;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XGettingSet;
import one.microstream.collections.types.XIterable;
import one.microstream.io.XIO;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyInvalidObjectId;
import one.microstream.persistence.exceptions.PersistenceExceptionConsistencyInvalidTypeId;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeConsistencyDefinitionResolveTypeName;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.reference.Lazy;
import one.microstream.reference.Swizzling;
import one.microstream.reflect.XReflect;
import one.microstream.typing.Composition;
import one.microstream.typing.KeyValue;
import one.microstream.util.xcsv.XCSV;
import one.microstream.util.xcsv.XCsvConfiguration;
import one.microstream.util.xcsv.XCsvDataType;


public class Persistence
{
	// (23.11.2018 TM)TODO: cleanup Persistence class

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long START_CID_BASE =  9_000_000_000_000_000_000L; // first assigned CID is 9...1
	static final long START_OID_BASE =  1_000_000_000_000_000_000L; // first assigned OID is 1...1
	static final long START_TID_BASE =                          0L; // first assigned TID is 1 (see below)

	static final long FIRST_CID      =  START_CID_BASE + 1;
	static final long FIRST_OID      =  START_OID_BASE + 1;
	static final long FIRST_TID      =  START_TID_BASE + 1;

	static final long BOUND_CID      =  9_100_000_000_000_000_000L;
	static final long BOUND_OID      =  START_CID_BASE;
	static final long BOUND_TID      =  START_OID_BASE;

	static final long START_CID_BYTE      = START_CID_BASE + 1_000;
	static final long START_CID_BOOLEAN   = START_CID_BASE + 2_000;
	static final long START_CID_SHORT     = START_CID_BASE + 3_000;
	static final long START_CID_CHARACTER = START_CID_BASE + 4_000;
	static final long START_CID_INTEGER   = START_CID_BASE + 5_000;
	static final long START_CID_LONG      = START_CID_BASE + 6_000;

	/* (27.11.2018 TM)NOTE: actually, the bound can be dynamically defined by a JVM system property
	 * But the problem is that a database with persisted data cannot change its instances from one
	 * JVM start to another.
	 * It is assumed here that no one will modify the default bound, anyway.
	 * Let's see how long this will hold ...
	 */
	static final int JSL_CACHE_INTEGER_START   = -128; // inclusive (first value)
	static final int JSL_CACHE_INTEGER_BOUND   = +128; // exclusive (bounding value)
	static final int JSL_CACHE_CHARACTER_START =    0; // inclusive (first value)
	static final int JSL_CACHE_CHARACTER_BOUND = +128; // exclusive (bounding value)

	static final long START_CID_REAL = START_CID_BASE +    10_000L; // first 10K reserved for JLS constants
	static final long START_TID_REAL = START_TID_BASE + 1_000_000L; // first new type gets 1M1 assigned.

	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged

	// java.lang and basic types.
	static final long TID_PRIMITIVE_byte        =  1L;
	static final long TID_PRIMITIVE_boolean     =  2L;
	static final long TID_PRIMITIVE_short       =  3L;
	static final long TID_PRIMITIVE_char        =  4L;
	static final long TID_PRIMITIVE_int         =  5L;
	static final long TID_PRIMITIVE_float       =  6L;
	static final long TID_PRIMITIVE_long        =  7L;
	static final long TID_PRIMITIVE_double      =  8L;
	static final long TID_PRIMITIVE_void        =  9L; // "kind of" primitive. Or whatever.
	static final long TID_Object                = 10L;
	static final long TID_Byte                  = 11L;
	static final long TID_Boolean               = 12L;
	static final long TID_Short                 = 13L;
	static final long TID_Character             = 14L;
	static final long TID_Integer               = 15L;
	static final long TID_Float                 = 16L;
	static final long TID_Long                  = 17L;
	static final long TID_Double                = 18L;
	static final long TID_Void                  = 19L;

	static final long TID_Class                 = 20L;
	static final long TID_Enum                  = 21L;

	static final long TID_String                = 30L;
	static final long TID_AbstractStringBuilder = 31L;
	static final long TID_StringBuffer          = 32L;
	static final long TID_StringBuilder         = 33L;

	static final long TID_java_io_File          = 34L;
	static final long TID_java_util_Date        = 35L;

	static final long TID_Number                = 36L;
	static final long TID_java_math_BigInteger  = 37L;
	static final long TID_java_math_BigDecimal  = 38L;

	// java.util collections
	static final long TID_java_util_AbstractCollection     = 40L;
	static final long TID_java_util_AbstractList           = 41L;
	static final long TID_java_util_AbstractSet            = 42L;
	static final long TID_java_util_ArrayList              = 43L;
	static final long TID_java_util_HashSet                = 44L;
	static final long TID_java_util_AbstractMap            = 45L;
	static final long TID_java_util_HashMap                = 46L;
	static final long TID_java_util_Dictionary             = 47L;
	static final long TID_java_util_Hashtable              = 48L;
	static final long TID_java_util_ArrayDeque             = 49L;
	static final long TID_java_util_IdentityHashMap        = 50L;
	static final long TID_java_util_LinkedHashMap          = 51L;
	static final long TID_java_util_LinkedHashSet          = 52L;
	static final long TID_java_util_AbstractSequentialList = 53L;
	static final long TID_java_util_LinkedList             = 54L;
	static final long TID_java_util_AbstractQueue          = 55L;
	static final long TID_java_util_PriorityQueue          = 56L;
	static final long TID_java_util_TreeMap                = 57L;
	static final long TID_java_util_TreeSet                = 58L;
	static final long TID_java_util_Vector                 = 59L;
	static final long TID_java_util_Stack                  = 60L;
	static final long TID_java_util_Properties             = 61L;
	// java.util.concurrent collections
	static final long TID_java_util_ConcurrentHashMap      = 62L;
	static final long TID_java_util_ConcurrentLinkedDeque  = 63L;
	static final long TID_java_util_ConcurrentLinkedQueue  = 64L;
	static final long TID_java_util_ConcurrentSkipListMap  = 65L;
	static final long TID_java_util_ConcurrentSkipListSet  = 66L;
	static final long TID_java_util_WeakHashMap            = 67L;

	static final long TID_java_util_Locale                 = 68L;

	// arrays (only 1D) of common types
	static final long TID_ARRAY_byte           = 100L + TID_PRIMITIVE_byte   ;
	static final long TID_ARRAY_boolean        = 100L + TID_PRIMITIVE_boolean;
	static final long TID_ARRAY_short          = 100L + TID_PRIMITIVE_short  ;
	static final long TID_ARRAY_char           = 100L + TID_PRIMITIVE_char   ;
	static final long TID_ARRAY_int            = 100L + TID_PRIMITIVE_int    ;
	static final long TID_ARRAY_float          = 100L + TID_PRIMITIVE_float  ;
	static final long TID_ARRAY_long           = 100L + TID_PRIMITIVE_long   ;
	static final long TID_ARRAY_double         = 100L + TID_PRIMITIVE_double ;

	static final long TID_ARRAY_Object         = 100L + TID_Object   ;
	static final long TID_ARRAY_Byte           = 100L + TID_Byte     ;
	static final long TID_ARRAY_Boolean        = 100L + TID_Boolean  ;
	static final long TID_ARRAY_Short          = 100L + TID_Short    ;
	static final long TID_ARRAY_Character      = 100L + TID_Character;
	static final long TID_ARRAY_Integer        = 100L + TID_Integer  ;
	static final long TID_ARRAY_Float          = 100L + TID_Float    ;
	static final long TID_ARRAY_Long           = 100L + TID_Long     ;
	static final long TID_ARRAY_Double         = 100L + TID_Double   ;
	static final long TID_ARRAY_Void           = 100L + TID_Void     ;

	static final long TID_ARRAY_Class          = 100L + TID_Class;
	static final long TID_ARRAY_Enum           = 100L + TID_Enum;

	static final long TID_ARRAY_String         = 100L + TID_String;
	static final long TID_ARRAY_AbsStringBuffr = 100L + TID_AbstractStringBuilder;
	static final long TID_ARRAY_StringBuffer   = 100L + TID_StringBuffer         ;
	static final long TID_ARRAY_StringBuilder  = 100L + TID_StringBuilder        ;

	static final long TID_persistence_Lazy_Default = 10000L;

	// CHECKSTYLE.ON: ConstantName

	static final String OBJECT_ID_LABEL  = "ObjectId";
	static final String OBJECT_ID_LABEL_SHORT = "OID";



	public static String engineName()
	{
		/*
		 * Kind of weird to put it here, but it has to be somewhere
		 * and the Persistence layer is the base for everything.
		 */
		return "MicroStream";
	}

	public static final String objectIdLabel()
	{
		return OBJECT_ID_LABEL;
	}

	public static final String objectIdShortLabel()
	{
		return OBJECT_ID_LABEL_SHORT;
	}

	/**
	 * Central architectural information method that always returns {@code long.class}.
	 *
	 * @return {@code long.class}
	 */
	public static final Class<?> objectIdType()
	{
		return long.class;
	}

	public static final PersistenceTypeIdLookup createDefaultTypeLookup()
	{
		return new PersistenceTypeIdLookup()
		{
			@Override
			public long lookupTypeId(final Class<?> type)
			{
				final Long nativeTypeId = NATIVE_TYPES.get(type);
				return nativeTypeId == null
					? Swizzling.notFoundId()
					: nativeTypeId.longValue()
				;
			}
		};
	}


	public static final long defaultStartTypeId()
	{
		return START_TID_REAL;
	}

	public static final long defaultStartConstantId()
	{
		return START_CID_REAL;
	}

	public static final long defaultStartObjectId()
	{
		return START_OID_BASE;
	}

	public static final long defaultBoundConstantId()
	{
		return BOUND_CID;
	}


	// (22.03.2013 TM)XXX: remove or optionally replace with PersistenceTypeDictionary lookup

	static final HashTable<Class<?>, Long> NATIVE_TYPES = HashTable.New();
	static
	{
		// note: correct order is important for recursive super type registration
		NATIVE_TYPES.add(byte           .class, TID_PRIMITIVE_byte   );
		NATIVE_TYPES.add(boolean        .class, TID_PRIMITIVE_boolean);
		NATIVE_TYPES.add(short          .class, TID_PRIMITIVE_short  );
		NATIVE_TYPES.add(char           .class, TID_PRIMITIVE_char   );
		NATIVE_TYPES.add(int            .class, TID_PRIMITIVE_int    );
		NATIVE_TYPES.add(float          .class, TID_PRIMITIVE_float  );
		NATIVE_TYPES.add(long           .class, TID_PRIMITIVE_long   );
		NATIVE_TYPES.add(double         .class, TID_PRIMITIVE_double );
		NATIVE_TYPES.add(void           .class, TID_PRIMITIVE_void   );
		NATIVE_TYPES.add(Object         .class, TID_Object           );
		NATIVE_TYPES.add(Number         .class, TID_Number           );
		NATIVE_TYPES.add(Byte           .class, TID_Byte             );
		NATIVE_TYPES.add(Boolean        .class, TID_Boolean          );
		NATIVE_TYPES.add(Short          .class, TID_Short            );
		NATIVE_TYPES.add(Character      .class, TID_Character        );
		NATIVE_TYPES.add(Integer        .class, TID_Integer          );
		NATIVE_TYPES.add(Float          .class, TID_Float            );
		NATIVE_TYPES.add(Long           .class, TID_Long             );
		NATIVE_TYPES.add(Double         .class, TID_Double           );
		NATIVE_TYPES.add(Void           .class, TID_Void             );

		NATIVE_TYPES.add(Class          .class, TID_Class            );
		NATIVE_TYPES.add(Enum           .class, TID_Enum             );

		NATIVE_TYPES.add(String         .class, TID_String           );
		// stupid default visibility on such a common type.
		NATIVE_TYPES.add(StringBuffer.class.getSuperclass(), TID_AbstractStringBuilder);
		NATIVE_TYPES.add(StringBuffer   .class, TID_StringBuffer     );
		NATIVE_TYPES.add(StringBuilder  .class, TID_StringBuilder    );

		NATIVE_TYPES.add(java.io  .File      .class, TID_java_io_File        );
		NATIVE_TYPES.add(java.util.Date      .class, TID_java_util_Date      );

		NATIVE_TYPES.add(java.lang.Number    .class, TID_Number              );
		NATIVE_TYPES.add(java.math.BigInteger.class, TID_java_math_BigInteger);
		NATIVE_TYPES.add(java.math.BigDecimal.class, TID_java_math_BigDecimal);

		NATIVE_TYPES.add(java.util.ArrayList.class.getSuperclass().getSuperclass(), TID_java_util_AbstractCollection );
		NATIVE_TYPES.add(java.util.ArrayList.class.getSuperclass(), TID_java_util_AbstractList          );
		NATIVE_TYPES.add(java.util.HashSet  .class.getSuperclass(), TID_java_util_AbstractSet           );
		NATIVE_TYPES.add(java.util.ArrayList                .class, TID_java_util_ArrayList             );
		NATIVE_TYPES.add(java.util.HashSet                  .class, TID_java_util_HashSet               );
		NATIVE_TYPES.add(java.util.AbstractMap              .class, TID_java_util_AbstractMap           );
		NATIVE_TYPES.add(java.util.HashMap                  .class, TID_java_util_HashMap               );
		NATIVE_TYPES.add(java.util.Dictionary               .class, TID_java_util_Dictionary            );
		NATIVE_TYPES.add(java.util.Hashtable                .class, TID_java_util_Hashtable             );
		NATIVE_TYPES.add(java.util.ArrayDeque               .class, TID_java_util_ArrayDeque            );
		NATIVE_TYPES.add(java.util.IdentityHashMap          .class, TID_java_util_IdentityHashMap       );
		NATIVE_TYPES.add(java.util.LinkedHashMap            .class, TID_java_util_LinkedHashMap         );
		NATIVE_TYPES.add(java.util.LinkedHashSet            .class, TID_java_util_LinkedHashSet         );
		NATIVE_TYPES.add(java.util.AbstractSequentialList   .class, TID_java_util_AbstractSequentialList);
		NATIVE_TYPES.add(java.util.LinkedList               .class, TID_java_util_LinkedList            );
		NATIVE_TYPES.add(java.util.AbstractQueue            .class, TID_java_util_AbstractQueue         );
		NATIVE_TYPES.add(java.util.PriorityQueue            .class, TID_java_util_PriorityQueue         );
		NATIVE_TYPES.add(java.util.TreeMap                  .class, TID_java_util_TreeMap               );
		NATIVE_TYPES.add(java.util.TreeSet                  .class, TID_java_util_TreeSet               );
		NATIVE_TYPES.add(java.util.Vector                   .class, TID_java_util_Vector                );
		NATIVE_TYPES.add(java.util.Stack                    .class, TID_java_util_Stack                 );
		NATIVE_TYPES.add(java.util.Properties               .class, TID_java_util_Properties            );

		/*
		 * (18.07.2019 TM)NOTE: intentionally no native TypeId for the later added WeakHashMap
		 * A special runtime construct like that should not be part of a persistent entity graph
		 * and if it is nonetheless, it's perfectly fine to assign a dynamic typeId to it instead
		 * of glueing a native id entry to it.
		 * Actually, it is questionable if natively defined TypeIds beyond BigDecimal are reasonable
		 * in the first place.
		 */

		NATIVE_TYPES.add(java.util.concurrent.ConcurrentHashMap    .class, TID_java_util_ConcurrentHashMap    );
		NATIVE_TYPES.add(java.util.concurrent.ConcurrentLinkedDeque.class, TID_java_util_ConcurrentLinkedDeque);
		NATIVE_TYPES.add(java.util.concurrent.ConcurrentLinkedQueue.class, TID_java_util_ConcurrentLinkedQueue);
		NATIVE_TYPES.add(java.util.concurrent.ConcurrentSkipListMap.class, TID_java_util_ConcurrentSkipListMap);
		NATIVE_TYPES.add(java.util.concurrent.ConcurrentSkipListSet.class, TID_java_util_ConcurrentSkipListSet);

		NATIVE_TYPES.add(java.util.Locale.class, TID_java_util_Locale);

		// basic array types (arrays of java.lang. types)
		NATIVE_TYPES.add(byte[]         .class, TID_ARRAY_byte   );
		NATIVE_TYPES.add(boolean[]      .class, TID_ARRAY_boolean);
		NATIVE_TYPES.add(short[]        .class, TID_ARRAY_short  );
		NATIVE_TYPES.add(char[]         .class, TID_ARRAY_char   );
		NATIVE_TYPES.add(int[]          .class, TID_ARRAY_int    );
		NATIVE_TYPES.add(float[]        .class, TID_ARRAY_float  );
		NATIVE_TYPES.add(long[]         .class, TID_ARRAY_long   );
		NATIVE_TYPES.add(double[]       .class, TID_ARRAY_double );
		// invalid: void[].class
		NATIVE_TYPES.add(Class[]        .class, TID_ARRAY_Class        );
		NATIVE_TYPES.add(Byte[]         .class, TID_ARRAY_Byte         );
		NATIVE_TYPES.add(Boolean[]      .class, TID_ARRAY_Boolean      );
		NATIVE_TYPES.add(Short[]        .class, TID_ARRAY_Short        );
		NATIVE_TYPES.add(Character[]    .class, TID_ARRAY_Character    );
		NATIVE_TYPES.add(Integer[]      .class, TID_ARRAY_Integer      );
		NATIVE_TYPES.add(Float[]        .class, TID_ARRAY_Float        );
		NATIVE_TYPES.add(Long[]         .class, TID_ARRAY_Long         );
		NATIVE_TYPES.add(Double[]       .class, TID_ARRAY_Double       );
		NATIVE_TYPES.add(Void[]         .class, TID_ARRAY_Void         );
		NATIVE_TYPES.add(Object[]       .class, TID_ARRAY_Object       );
		NATIVE_TYPES.add(String[]       .class, TID_ARRAY_String       );
		NATIVE_TYPES.add(StringBuffer[] .class, TID_ARRAY_StringBuffer );
		NATIVE_TYPES.add(StringBuilder[].class, TID_ARRAY_StringBuilder);
		NATIVE_TYPES.add(Enum[]         .class, TID_ARRAY_Enum         );

		// framework types //

		NATIVE_TYPES.add(Lazy.Default.class, TID_persistence_Lazy_Default);
	}



	public static final long classTypeId()
	{
		return TID_Class;
	}

	public static final boolean isNativeType(final Class<?> type)
	{
		return NATIVE_TYPES.get(type) != null;
	}

	public static final Long getNativeTypeId(final Class<?> type)
	{
		return NATIVE_TYPES.get(type);
	}

	public static final <R extends PersistenceObjectRegistry> R registerJavaNatives(final R registry)
	{
//		registerJavaBasicTypes(registry);
		registerJavaConstants(registry);
		return registry;
	}

	public static final <R extends PersistenceTypeRegistry> R registerJavaBasicTypes(final R registry)
	{
		iterateJavaBasicTypes((c, tid) ->
		{
			registry.registerType(tid, c);
		});

		return registry;
	}

	public static final <C extends BiConsumer<Class<?>, Long>> C iterateJavaBasicTypes(final C iterator)
	{
		NATIVE_TYPES.iterate(e ->
			iterator.accept(e.key(), e.value())
		);

		return iterator;
	}

	public static final <R extends PersistenceObjectRegistry> R registerJavaConstants(final R registry)
	{
		long
			oidByte      = START_CID_BYTE     ,
			oidBoolean   = START_CID_BOOLEAN  ,
			oidShort     = START_CID_SHORT    ,
			oidCharacter = START_CID_CHARACTER,
			oidInteger   = START_CID_INTEGER  ,
			oidLong      = START_CID_LONG
		;

		// Booleans
		{
			registry.registerConstant(oidBoolean++, Boolean.FALSE);
			registry.registerConstant(oidBoolean++, Boolean.TRUE );
		}

		// primitive numeric wrappers (Byte, Short, Integer, Long)
		for(int i = JSL_CACHE_INTEGER_START; i < JSL_CACHE_INTEGER_BOUND; i++)
		{
			registry.registerConstant(oidByte++   , Byte.valueOf((byte)i)  );
			registry.registerConstant(oidShort++  , Short.valueOf((short)i));
			registry.registerConstant(oidInteger++, Integer.valueOf(i)     );
			registry.registerConstant(oidLong++   , Long.valueOf(i)        );
		}

		// Characters
		for(int i = JSL_CACHE_CHARACTER_START; i < JSL_CACHE_CHARACTER_BOUND; i++)
		{
			registry.registerConstant(oidCharacter++, Character.valueOf((char)i));
		}

		return registry;
	}



	public static long validateObjectId(final long id) throws PersistenceExceptionConsistencyInvalidObjectId
	{
		if(id < START_OID_BASE)
		{
			throw new PersistenceExceptionConsistencyInvalidObjectId(id);
		}
		return id;
	}

	public static long validateTypeId(final long id) throws PersistenceExceptionConsistencyInvalidTypeId
	{
		if(id < START_TID_BASE)
		{
			throw new PersistenceExceptionConsistencyInvalidTypeId(id);
		}
		return id;
	}

	public static final void iterateReferences(
		final PersistenceFunction iterator,
		final Object[]            array   ,
		final int                 offset  ,
		final int                 length
	)
	{
		final int bound = offset + length;
		for(int i = offset; offset < bound; i++)
		{
			iterator.apply(array[i]);
		}
	}

	public static final void iterateReferences(final PersistenceFunction iterator, final XIterable<?> elements)
	{
		elements.iterate(iterator::apply);
	}

	public static final void iterateReferencesIterable(final PersistenceFunction iterator, final Iterable<?> elements)
	{
		// using forEach would create two temporary instances, this (and #iterateReferences) only creates one.
		for(final Object element : elements)
		{
			iterator.apply(element);
		}
	}

	public static final void iterateReferencesMap(final PersistenceFunction iterator, final Map<?, ?> elements)
	{
		// using forEach would create two temporary instances, this (and the method above) only creates one.
		for(final Map.Entry<?, ?> element : elements.entrySet())
		{
			iterator.apply(element.getKey());
			iterator.apply(element.getValue());
		}
	}


	/**
	 * Reasons for choosing UTF8 as the standard charset:
	 * 1.) It is independent from endianess.
	 * 2.) It is massively smaller due to most content containing almost only single-byte ASCII characters
	 * 3.) It is overall more commonly and widespread used and compatible than any specific format.
	 * @return the UTF8 charset
	 */
	public static final Charset standardCharset()
	{
		return XChars.utf8();
	}

	public static String defaultFilenameTypeDictionary()
	{
		// why permanently occupy additional memory with fields and instances for constant values?
		return "PersistenceTypeDictionary.ptd";
	}



	/*
	 * Rationale:
	 *
	 * Composition:
	 * The very nature of this interface is to indicate that instances of that type are NOT meant
	 * to be treated as autonomous entities. It's like a "NoEntity" type and therefore not allowed to be
	 * treated as one.
	 *
	 * Enumerations and Iterators:
	 * Iterators are basically logic-helpers, like an implemented for loop on a complex structure.
	 * Such a thing can never meant to be a reasonably persistable entity.
	 * Additionally, Iterator implementations typically access instances, that are actually meant to be unshared.
	 * In other words: The Iterator implementation is a composition type of an actual entity-worthy type.
	 * Should the special case ever occur, that a proper entity type implements Iterator (despite not being
	 * supposed to do so), it can still be handled by explicitely registering a custom type handler for it.
	 *
	 * Various SubLists:
	 * The JDK in its usual progamming quality, lacking use of proper interfaces, etc., sadly provides no way
	 * of reading the offset values used in sub lists (similar to loadFactor in hashing collections).
	 * Thus, there is no way to store the required data of JDK sub lists in generic way.
	 * A tailored (and JDK-version-specific) custom handler implementation can always be registered as an override
	 */
	private static final ConstHashEnum<Class<?>> UNPERSISTABLE_TYPES = ConstHashEnum.New(

		// types that are explicitly marked as unpersistable. E.g. the persistence logic itself!
		Unpersistable.class,

		// system stuff (cannot be restored intrinsically due to ties to JVM internals)
		ClassLoader.class,
		Thread.class,

		// IO stuff (cannot be restored intrinsically due to ties to external resources like files, etc.)
		InputStream.class,
		OutputStream.class,
		FileChannel.class,
		Socket.class,
		ServerSocket.class,

		// unshared composition types (those are internal helper class instances, not entities)
		Composition.class,
		ChainStorage.class,
		ChainStorage.Entry.class,
		Map.Entry.class,

		// there is sadly no (plain-string-independant) sane way to get these. Classical JDK.
		new LinkedList<>().subList(0, 0).getClass()          , // java.util.SubList
		new ArrayList<>(0).subList(0, 0).getClass()          , // java.util.ArrayList$SubList
		Collections.emptyList().subList(0, 0).getClass()     , // java.util.RandomAccessSubList
		new CopyOnWriteArrayList<>().subList(0, 0).getClass(), // java.util.concurrent.CopyOnWriteArrayList$COWSubList

		Enumeration.class,
		Iterator.class,

		// it makes no sense to support/allow these "magical" volatile references in a persistent context.
		Reference.class,

		// for now, not supported because of JVM-managed fields etc.
		Throwable.class

		// note: lambdas don't have a super class as such. See usages of "LambdaTypeRecognizer" instead
	);

	/**
	 * Types whose instances cannot be persisted. E.g. {@link Unpersistable}, {@link Thread}, {@link ClassLoader}, etc.
	 *
	 * Note that the {@link Class} instances representing these types are very well persistable and will get
	 * empty type descriptions to assign type ids to them. Only their instances cannot be persisted.
	 * 
	 * @return the types whose instances cannot be persisted
	 */
	public static XGettingEnum<Class<?>> unpersistableTypes()
	{
		return UNPERSISTABLE_TYPES;
	}



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static boolean isPersistable(final Class<?> type)
	{
		return !isUnpersistable(type);
	}

	public static boolean isUnpersistable(final Class<?> type)
	{
		return XReflect.isOfAnyType(type, unpersistableTypes());
	}

	public static final <D> PersistenceTypeMismatchValidator<D> typeMismatchValidatorFailing()
	{
		return PersistenceTypeMismatchValidator.Failing();
	}

	public static final <D> PersistenceTypeMismatchValidator<D> typeMismatchValidatorNoOp()
	{
		return PersistenceTypeMismatchValidator.NoOp();
	}

	public static final PersistenceTypeEvaluator defaultTypeEvaluatorPersistable()
	{
		return type ->
			isPersistable(type)
		;
	}

	public static final boolean isPersistableField(final Class<?> entityType, final Field field)
	{
		return !XReflect.isTransient(field);
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluatorPersistable()
	{
		return Persistence::isPersistableField;
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluatorPersister()
	{
		// the type check is hardcoded to be unremovable. The evaluator only enables the feature and covers customizing.
		return (entityType, field) ->
			true
		;
	}

	public static final boolean isPersisterField(final Field field)
	{
		// the field's type must be Persister or "lower" / more specific, e.g. StorageManager.
		return Persister.class.isAssignableFrom(field.getType());
	}

	public static boolean isHandleableEnumField(final Class<?> enumClass, final Field field)
	{
		// actually, even the crazy sh*t enum sub types with persistent state should be safely handleable.
		return true;

//		// just in case and to guarantee the correctness of the algorithm below
//		if(!XReflect.isEnum(enumClass)) // Class#isEnum is bugged!
//		{
//			return true;
//		}
//
//		// a "normal", "top level" enum class. No restrictions on their instance fields, so return true
//		if(enumClass.getSuperclass() == java.lang.Enum.class)
//		{
//			return true;
//		}
//
//		// below here is the "dungeon" of crazy sh*t enum subtypes, defined like an anonymous class instance.
//
//		// exception to the exception: all fields declared "above" the crazy subtype are unproblematic.
//		if(field.getDeclaringClass() != enumClass)
//		{
//			return true;
//		}
//
//		XDebug.println("Unhandleable enum field: " + enumClass.getName() + "." + field.getName());
//
//		/*
//		 * transient (actually: not persistable) fields are already filtered out before,
//		 * hence ANY field reaching this point is not handleable.
//		 */
//		return false;
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluatorEnum()
	{
		/* No transient check necessary since transient fields are filtered out in general
		 * Or more precisely:
		 * What is considered transient must be defined at one place / by one logic (isPersistable)
		 * and may not be sneakily reverted to just checking the transient keyword.
		 */
		return (entityType, field) ->
			isHandleableEnumField(entityType, field)
		;
	}

	public static boolean isHandleableCollectionField(final Class<?> collectionClass, final Field field)
	{
		final Class<?> fieldType = field.getType();

		// primitives are, of course, never a problem
		if(fieldType.isPrimitive())
		{
			return true;
		}

		// having a Comparator type of any sort is unproblematic and occurs in sorted collections
		if(Comparator.class.isAssignableFrom(collectionClass))
		{
			return true;
		}

		// referencing another collection means the collection type being analyzed is just a wrapper implementation.
		if(XReflect.isJavaUtilCollectionType(collectionClass))
		{
			return true;
		}

		// referencing element types directly is also not a problem
		final XGettingSet<Type> entityClassTypeVairable = getTypeVariales(collectionClass);
		if(entityClassTypeVairable.contains(field.getGenericType()))
		{
			return true;
		}

		/*
		 * Kind of an overkill / loophole, but the idea is that mutex references are usually just Object-typed
		 * fields. It is highly unlikely that an internal collection structure (array, Entry type, etc.) would
		 * be referenced by just an Object-typed field instead of a properly typed field.
		 */
		if(collectionClass == Object.class)
		{
			return true;
		}

		// required to handle wrappers like Arrays$ArrayList correctly and not a big overhead in general
		if(fieldType.isArray())
		{
			final Class<?> componentType = fieldType.getComponentType();

			// check for element type
			if(entityClassTypeVairable.contains(componentType))
			{
				return true;
			}

			// assume an Object[] is a non-type-parametrized element array
			if(componentType == Object.class)
			{
				return true;
			}

			// anything else (like Entry[] of a complex datastructure] is intentionally not supported.
			return false;
		}

		/*
		 * Any other typed field is assumed to point to a complex data structure (like arrays,
		 * lists/trees of Entry instances, etc.). While technically handleable, this is rejected by the
		 * analyzing logic in order to prevent extremely inefficient storing structures.
		 * Consider a LinkedList, for example: persisting every Node instance directly would store an additional
		 * amount of instances equal to the list's size. Like storing a list with size 1 million would store
		 * 1 million and 1 instances just for the list instead of only one instance, the list itself.
		 * It would also cause 24 bytes of overhead (3 references) for every single element contained in the list.
		 * The loading would be catastrophically slow since every single Node instance would represent one layer
		 * of recursive loading.
		 * A single array (like in ArrayList) would be acceptable efficiency-wise, but in the end, consistency is
		 * more important: every collection that cannot be generically handled (e.g. a wrapper implementation) must
		 * have a tailored TypeHandler registered for it. At least an explicitely registered generic handler like
		 * BinaryHandlerList and the like.
		 */
		return false;
	}

	public static final PersistenceFieldEvaluator defaultFieldEvaluatorCollection()
	{
		/* No transient check necessary since transient fields are filtered out in general
		 * Or more precisely:
		 * What is considered transient must be defined at one place / by one logic (isPersistable)
		 * and may not be sneakily reverted to just checking the transient keyword.
		 */
		return (entityType, field) ->
			isHandleableCollectionField(entityType, field)
		;
	}

	private static XGettingSet<Type> getTypeVariales(final Class<?> entityType)
	{
		final TypeVariable<?>[] tvs = entityType.getTypeParameters();
		if(tvs == null || tvs.length == 0)
		{
			return X.empty();
		}

		// identity equality is sufficient, tested via debugger
		return HashEnum.New(tvs);
	}

	public static final PersistenceEagerStoringFieldEvaluator defaultReferenceFieldEagerEvaluator()
	{
		// by default, no field is eager
		return (entityType, field) ->
			false
		;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" }) // type safety guaranteed by the passed typename. The typename String "is" the T.
	public static <T> Class<T> resolveEnumeratedClassIdentifierSeparatedType(
		final String      typeName   ,
		final ClassLoader classLoader,
		final String      substituteClassIdentifierSeparator
	)
	{
		// there can only be one at the most, so a simple indexOf is sufficient.
		final int sepIndex = typeName.indexOf(substituteClassIdentifierSeparator);
		if(sepIndex < 0)
		{
			return null;
		}

		final String properTypeName = typeName.substring(0, sepIndex);
		final Class<?> type = resolveType(properTypeName, classLoader, substituteClassIdentifierSeparator);

		if(!XReflect.isDeclaredEnum(type))
		{
			// it could also be used for anonymous inner classes, but Class provides no way to query those...
			throw new UnsupportedOperationException("EnumeratedClassIdentifierNaming is only supported for sub enums");
		}

		final String enumConstantName = typeName.substring(sepIndex + substituteClassIdentifierSeparator.length());

		final Enum<?> enumConstant = Enum.valueOf((Class<Enum>)type, enumConstantName);

		return (Class<T>)enumConstant.getClass();
	}

	// type safety guaranteed by the passed typename. The typename String "is" the T.
	public static <T> Class<T> resolveType(final String typeName, final ClassLoader classLoader)
	{
		return resolveType(typeName, classLoader, substituteClassIdentifierSeparator());
	}

	@SuppressWarnings("unchecked") // type safety guaranteed by the passed typename. The typename String "is" the T.
	public static <T> Class<T> resolveType(
		final String      typeName   ,
		final ClassLoader classLoader,
		final String      substituteClassIdentifierSeparator
	)
	{
		final Class<?> c = resolveEnumeratedClassIdentifierSeparatedType(
			typeName,
			classLoader,
			substituteClassIdentifierSeparator
		);
		if(c != null)
		{
			return (Class<T>)c;
		}

		try
		{
			return (Class<T>)XReflect.resolveType(typeName, classLoader);
		}
		catch(final ClassNotFoundException e)
		{
			throw new PersistenceExceptionTypeConsistencyDefinitionResolveTypeName(typeName, e);
		}
	}

	public static <T> Class<T> tryResolveType(final String typeName, final ClassLoader classLoader)
	{
		try
		{
			return Persistence.resolveType(typeName, classLoader);
		}
		catch(final PersistenceExceptionTypeConsistencyDefinitionResolveTypeName e)
		{
			// intentionally return null
			return null;
		}
	}

	public static String deriveEnumRootIdentifier(final PersistenceTypeHandler<?, ?> typeHandler)
	{
		XReflect.validateIsEnum(typeHandler.type());

		if(Swizzling.isNotProperId(typeHandler.typeId()))
		{
			throw new PersistenceException(
				"Type handler not initialized for type " + typeHandler.type()
				+ ". This is probably caused by a missing type dictionary entry for that type."
			);
		}

		return XReflect.typename_enum() + " " + typeHandler.typeId();
	}

	public static Object[] collectEnumConstants(final PersistenceTypeHandler<?, ?> typeHandler)
	{
		XReflect.validateIsEnum(typeHandler.type());

		final Object[] enumConstants = typeHandler.type().getEnumConstants();

		// intentionally type Object[], not some T[] in covariant disguise.
		final Object[] copy = new Object[enumConstants.length];
		System.arraycopy(enumConstants, 0, copy, 0, enumConstants.length);

		return copy;
	}

	/*
	 * This is important:
	 * A lot of custom enum identifiers can start with the 4 letter e, n, u, m. E.h. "enumerationsStuffs",
	 * but the defined enum root identifier is exactely "enum ".
	 */
	private static final String ENUM_ROOT_IDENTIFIER_START = XReflect.typename_enum() + " ";

	public static String enumRootIdentifierStart()
	{
		return ENUM_ROOT_IDENTIFIER_START;
	}

	public static Long parseEnumRootIdentifierTypeId(final String enumRootIdentifier)
	{
		// quick check before doing any instantiation. Has virtually no redundancy to the code below
		if(!isEnumRootIdentifier(enumRootIdentifier))
		{
			return null;
		}

		final String typeIdPart = enumRootIdentifier.substring(enumRootIdentifierStart().length());
		try
		{
			return Long.valueOf(Long.parseLong(typeIdPart));
		}
		catch(final NumberFormatException e)
		{
			// should never happen due to the quick check above, but who knows.
			return null;
		}
	}

	public static boolean isEnumRootIdentifier(final String enumRootIdentifier)
	{
		return isPotentialEnumRootIdentifier(enumRootIdentifier)
			&& XChars.applies(enumRootIdentifier, enumRootIdentifierStart().length(), XChars::isDigit)
		;
	}

	public static boolean isPotentialEnumRootIdentifier(final String enumRootIdentifier)
	{
		return enumRootIdentifier != null && enumRootIdentifier.startsWith(enumRootIdentifierStart());
	}

	@Deprecated
	public static final String defaultRootIdentifier()
	{
		return "defaultRoot";
	}

	@Deprecated
	public static final String customRootIdentifier()
	{
		return "root";
	}

	public static final String rootIdentifier()
	{
		// must be upper case to be distinct from old custom root concept for automatic version change detection.
		return "ROOT";
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final Path refactoringsFile
	)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringsFile)
		);
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final String refactoringMappings
	)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringMappings)
		);
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final String refactoringMappings,
		final char   valueSeparator
	)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringMappings, valueSeparator)
		);
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final String       refactoringMappings,
		final XCsvDataType dataType
	)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringMappings, dataType)
		);
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final String            refactoringMappings,
		final XCsvConfiguration configuration
	)
	{
		return RefactoringMapping(
			readRefactoringMappings(refactoringMappings, configuration)
		);
	}

	public static final PersistenceRefactoringMappingProvider RefactoringMapping(
		final XGettingSequence<KeyValue<String, String>> refactoringMappings
	)
	{
		return PersistenceRefactoringMappingProvider.New(refactoringMappings);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(final Path file)
	{
		final StringTable stringTable = XCSV.readFromFile(file);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(final AFile file)
	{
		final String       fileSuffix  = XIO.getFileSuffix(file.identifier());
		final String       normalized  = fileSuffix == null ? null : fileSuffix.trim().toLowerCase();
		final XCsvDataType dataType    = XCsvDataType.fromIdentifier(normalized);
		final String       fileContent = AFS.readString(file);
		final StringTable  stringTable = XCSV.parse(fileContent, dataType);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(
		final String string
	)
	{
		final StringTable stringTable = XCSV.parse(string);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(
		final String string        ,
		final char   valueSeparator
	)
	{
		final StringTable stringTable = XCSV.parse(string, valueSeparator);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(
		final String       string  ,
		final XCsvDataType dataType
	)
	{
		final StringTable stringTable = XCSV.parse(string, dataType);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> readRefactoringMappings(
		final String            string       ,
		final XCsvConfiguration configuration
	)
	{
		final StringTable stringTable = XCSV.parse(string, configuration);

		return parseRefactoringMappings(stringTable);
	}

	public static XGettingSequence<KeyValue<String, String>> parseRefactoringMappings(final StringTable stringTable)
	{
		final BulkList<KeyValue<String, String>> entries = BulkList.New(stringTable.rows().size());

		stringTable.mapTo(
			(k, v) ->
				entries.add(X.KeyValue(k, v)),
			row ->
				XChars.trimEmptyToNull(row[0]), // debuggability line break, do not remove!
			row ->
				XChars.trimEmptyToNull(row[1])  // debuggability line break, do not remove!
		);

		return entries;
	}

	/**
	 * Persistence-specific separator between a class name and a proper identifier
	 * that replaces unreliable class names (like "$1", "$2" etc.) by a reliably identifying substitute name.
	 * @return separator between a class name and a proper identifier
	 */
	public static final String substituteClassIdentifierSeparator()
	{
		return "$§";
	}

	public static final String derivePersistentTypeName(final Class<?> type)
	{
		return derivePersistentTypeName(type, substituteClassIdentifierSeparator());
	}

	public static final String derivePersistentTypeName(
		final Class<?> type,
		final String   substituteClassIdentifierSeparator
	)
	{
		// handleable special case of sub-enum classes with enumerating class names.
		if(XReflect.isSubEnum(type))
		{
			return derivePersistentTypeNameEnum(type, substituteClassIdentifierSeparator);
		}

		// unhandleable general case of classes with enumerating class names.
		if(XReflect.hasEnumeratedTypeName(type))
		{
			throw new PersistenceExceptionTypeNotPersistable(type,
				"Synthetic classes ($1 etc.) are not reliably persistable since a simple reordering of source code"
				+ " elements would change the name identity of a class. For a type system that has to rely upon"
				+ " resolving types by their identifying name, this would silently cause a potentially fatal error."
				+ " If handling synthetic classes (e.g. anonymous inner classes) is absolutely necessary, a custom "
				+ PersistenceTypeResolver.class.getName() + " can be used to remove the exception and assume "
				+ " complete responsibility for correctly handling synthetic class names."
			);
		}

		// "normal" types without problematic enumerating class names.
		return type.getName();
	}

	public static final String derivePersistentTypeNameEnum(
		final Class<?> type,
		final String   substituteClassIdentifierSeparator
	)
	{
		if(!XReflect.isSubEnum(type))
		{
			throw new PersistenceException("Not an Enum type: " + type.getName());
		}

		notNull(substituteClassIdentifierSeparator);

		final Class<?> declaredEnumType = XReflect.getDeclaredEnumClass(type);

		for(final Object enumConstant : declaredEnumType.getEnumConstants())
		{
			if(enumConstant.getClass() == type)
			{
				return declaredEnumType.getName() + substituteClassIdentifierSeparator + ((Enum<?>)enumConstant).name();
			}
		}

		throw new PersistenceException("Orphan sub enum type: " + type.getName());
	}


	/**
	 * Searches the methods of the passed entityType for a static method with arbitrary name and visibility,
	 * no arguments and {@link PersistenceTypeHandler} or a sub type of it as its return type.<p>
	 * Which method to select is also determined by testing the returned {@link PersistenceTypeHandler} instance
	 * if it has the correct {@link PersistenceTypeHandler#dataType()} for the used persistence context
	 * and the correct {@link PersistenceTypeHandler#type()} for the given entity class.
	 * <p>
	 * This mechanism is a convenience shortcut alternative to
	 * {@link PersistenceFoundation#registerCustomTypeHandler(PersistenceTypeHandler)}.
	 *
	 * @param <D> the data type
	 * @param <T> the entity type
	 * @param dataType the class for the data type
	 * @param entityType the class for the entity type
	 * @param selector custom selector logic
	 * @return the provided type handler if found
	 * @throws ReflectiveOperationException if a reflection error occurs
	 */
	public static <D, T> PersistenceTypeHandler<D, T> searchProvidedTypeHandler(
		final Class<D>                  dataType  ,
		final Class<T>                  entityType,
		final Predicate<? super Method> selector
	)
		throws ReflectiveOperationException
	{
		// ONLY declared methods of the specific type, not of super classes, since every class needs a specific handler
		for(final Method m : entityType.getDeclaredMethods())
		{
			// only static methods are admissible.
			if(!XReflect.isStatic(m))
			{
				continue;
			}

			// only parameter-less methods are admissible.
			if(m.getParameterCount() != 0)
			{
				continue;
			}

			// only methods returning an instance of PersistenceTypeHandler are admissible.
			if(!PersistenceTypeHandler.class.isAssignableFrom(m.getReturnType()))
			{
				continue;
			}

			m.setAccessible(true);
			final PersistenceTypeHandler<?, ?> providedTypeHandler = (PersistenceTypeHandler<?, ?>)m.invoke(null);

			// context checks
			if(providedTypeHandler.dataType() != dataType)
			{
				continue;
			}
			if(providedTypeHandler.type() != entityType)
			{
				continue;
			}

			// hook for custom selector logic, e.g. filtering for a certain annotation or name.
			if(selector != null && !selector.test(m))
			{
				continue;
			}

			@SuppressWarnings("unchecked")
			final PersistenceTypeHandler<D, T> applicableTypeHandler = (PersistenceTypeHandler<D, T>)providedTypeHandler;

			return applicableTypeHandler;
		}

		return null;
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 *
	 * @throws UnsupportedOperationException when called
	 */
	protected Persistence()
	{
		// static only
		throw new UnsupportedOperationException();
	}


	public static enum IdType
	{
		NULL
		{
			@Override
			public boolean isInRange(final long id)
			{
				return Swizzling.isNullId(id);
			}
		},
		TID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Persistence.FIRST_TID && id < BOUND_TID;
			}
		},
		OID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Persistence.FIRST_OID && id < BOUND_OID;
			}
		},
		CID
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id >= Persistence.FIRST_CID && id < BOUND_CID;
			}
		},
		UNDEFINED
		{
			@Override
			public boolean isInRange(final long id)
			{
				return id < START_TID_BASE || id >= BOUND_CID;
			}
		};


		public boolean isInRange(final long id)
		{
			return true;
		}

		public static IdType determineFromValue(final long id)
		{
			// order of checks is designed according to probability of type (OID having the highest etc.)
			return id >= Persistence.FIRST_OID
				? id >= Persistence.FIRST_CID
					? id >= Persistence.BOUND_CID
						? UNDEFINED
						: CID
					: OID
				: id >= Persistence.FIRST_TID
					? TID
					: Swizzling.isNullId(id)
						? NULL
						: UNDEFINED
			;
		}

	}

}
