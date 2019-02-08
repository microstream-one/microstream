package net.jadoth.persistence.binary.types;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceObjectIdResolver;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.typing.TypeMapping;

public final class BinaryValueTranslators
{
	/* (07.02.2019 TM)FIXME: JET-49: BinaryValueTranslators
	 * All of them require a byte order reversing variant
	 * (OMFG!)
	 */
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryValueSetter provideReferenceValueBinaryTranslator(
		final PersistenceTypeDescriptionMember sourceMember,
		final PersistenceTypeDescriptionMember targetMember
	)
	{
		// all references are stored as OID primitive values (long)
		if(targetMember == null)
		{
			return BinaryValueTranslators::skip_long;
		}
		
		if(!targetMember.isReference())
		{
			throwUnhandledTypeCompatibilityException(sourceMember.typeName(), targetMember.typeName());
		}
		
		return BinaryValueTranslators::copy_longTo_long;
	}
	
	private static void throwUnhandledTypeCompatibilityException(
		final String sourceType,
		final String targetType
	)
	{
		// (18.09.2018 TM)EXCP: proper exception
		throw new RuntimeException(
			"Cannot convert between primitive and reference values: "
			+ sourceType + " <-> " + targetType+ "."
		);
	}
	
	/**
	 * The default mapping only covers primitive types, because for arbitrary Object types, it cannot be
	 * safely assumed that instances of those types are unshared and that implicitely replacing one instance
	 * with another will never cause erronous behavior (e.g. identity comparisons suddenly yielding different
	 * results than would be expected based on the stored instances).<p>
	 * However, arbitrary mappings can be added to suit the needs of specific programs.
	 * 
	 * @return a default mapping of primitive-to-primitive binary value translators.
	 */
	public static final TypeMapping<BinaryValueSetter> createDefaultValueTranslators()
	{
		final TypeMapping<BinaryValueSetter> mapping = TypeMapping.New();
		registerPrimitivesToPrimitives(mapping);
		registerPrimitivesToWrappers(mapping);
		registerWrappersToPrimitives(mapping);
		registerWrappersToWrappers(mapping);
		registerCommonValueTypes(mapping);
		
		return mapping;
	}
	
	private static void registerPrimitivesToPrimitives(final TypeMapping<BinaryValueSetter> mapping)
	{
		mapping
		.register(byte.class, byte   .class, BinaryValueTranslators::copy_byteTo_byte   )
		.register(byte.class, boolean.class, BinaryValueTranslators::copy_byteTo_boolean)
		.register(byte.class, short  .class, BinaryValueTranslators::copy_byteTo_short  )
		.register(byte.class, char   .class, BinaryValueTranslators::copy_byteTo_char   )
		.register(byte.class, int    .class, BinaryValueTranslators::copy_byteTo_int    )
		.register(byte.class, float  .class, BinaryValueTranslators::copy_byteTo_float  )
		.register(byte.class, long   .class, BinaryValueTranslators::copy_byteTo_long   )
		.register(byte.class, double .class, BinaryValueTranslators::copy_byteTo_double )
		
		.register(boolean.class, byte   .class, BinaryValueTranslators::copy_booleanTo_byte   )
		.register(boolean.class, boolean.class, BinaryValueTranslators::copy_booleanTo_boolean)
		.register(boolean.class, short  .class, BinaryValueTranslators::copy_booleanTo_short  )
		.register(boolean.class, char   .class, BinaryValueTranslators::copy_booleanTo_char   )
		.register(boolean.class, int    .class, BinaryValueTranslators::copy_booleanTo_int    )
		.register(boolean.class, float  .class, BinaryValueTranslators::copy_booleanTo_float  )
		.register(boolean.class, long   .class, BinaryValueTranslators::copy_booleanTo_long   )
		.register(boolean.class, double .class, BinaryValueTranslators::copy_booleanTo_double )
		
		.register(short.class, byte   .class, BinaryValueTranslators::copy_shortTo_byte   )
		.register(short.class, boolean.class, BinaryValueTranslators::copy_shortTo_boolean)
		.register(short.class, short  .class, BinaryValueTranslators::copy_shortTo_short  )
		.register(short.class, char   .class, BinaryValueTranslators::copy_shortTo_char   )
		.register(short.class, int    .class, BinaryValueTranslators::copy_shortTo_int    )
		.register(short.class, float  .class, BinaryValueTranslators::copy_shortTo_float  )
		.register(short.class, long   .class, BinaryValueTranslators::copy_shortTo_long   )
		.register(short.class, double .class, BinaryValueTranslators::copy_shortTo_double )
		
		.register(char.class, byte   .class, BinaryValueTranslators::copy_charTo_byte   )
		.register(char.class, boolean.class, BinaryValueTranslators::copy_charTo_boolean)
		.register(char.class, short  .class, BinaryValueTranslators::copy_charTo_short  )
		.register(char.class, char   .class, BinaryValueTranslators::copy_charTo_char   )
		.register(char.class, int    .class, BinaryValueTranslators::copy_charTo_int    )
		.register(char.class, float  .class, BinaryValueTranslators::copy_charTo_float  )
		.register(char.class, long   .class, BinaryValueTranslators::copy_charTo_long   )
		.register(char.class, double .class, BinaryValueTranslators::copy_charTo_double )
		
		.register(int.class, byte   .class, BinaryValueTranslators::copy_intTo_byte   )
		.register(int.class, boolean.class, BinaryValueTranslators::copy_intTo_boolean)
		.register(int.class, short  .class, BinaryValueTranslators::copy_intTo_short  )
		.register(int.class, char   .class, BinaryValueTranslators::copy_intTo_char   )
		.register(int.class, int    .class, BinaryValueTranslators::copy_intTo_int    )
		.register(int.class, float  .class, BinaryValueTranslators::copy_intTo_float  )
		.register(int.class, long   .class, BinaryValueTranslators::copy_intTo_long   )
		.register(int.class, double .class, BinaryValueTranslators::copy_intTo_double )
		
		.register(float.class, byte   .class, BinaryValueTranslators::copy_floatTo_byte   )
		.register(float.class, boolean.class, BinaryValueTranslators::copy_floatTo_boolean)
		.register(float.class, short  .class, BinaryValueTranslators::copy_floatTo_short  )
		.register(float.class, char   .class, BinaryValueTranslators::copy_floatTo_char   )
		.register(float.class, int    .class, BinaryValueTranslators::copy_floatTo_int    )
		.register(float.class, float  .class, BinaryValueTranslators::copy_floatTo_float  )
		.register(float.class, long   .class, BinaryValueTranslators::copy_floatTo_long   )
		.register(float.class, double .class, BinaryValueTranslators::copy_floatTo_double )
		
		.register(long.class, byte   .class, BinaryValueTranslators::copy_longTo_byte   )
		.register(long.class, boolean.class, BinaryValueTranslators::copy_longTo_boolean)
		.register(long.class, short  .class, BinaryValueTranslators::copy_longTo_short  )
		.register(long.class, char   .class, BinaryValueTranslators::copy_longTo_char   )
		.register(long.class, int    .class, BinaryValueTranslators::copy_longTo_int    )
		.register(long.class, float  .class, BinaryValueTranslators::copy_longTo_float  )
		.register(long.class, long   .class, BinaryValueTranslators::copy_longTo_long   )
		.register(long.class, double .class, BinaryValueTranslators::copy_longTo_double )
		
		.register(double.class, byte   .class, BinaryValueTranslators::copy_doubleTo_byte   )
		.register(double.class, boolean.class, BinaryValueTranslators::copy_doubleTo_boolean)
		.register(double.class, short  .class, BinaryValueTranslators::copy_doubleTo_short  )
		.register(double.class, char   .class, BinaryValueTranslators::copy_doubleTo_char   )
		.register(double.class, int    .class, BinaryValueTranslators::copy_doubleTo_int    )
		.register(double.class, float  .class, BinaryValueTranslators::copy_doubleTo_float  )
		.register(double.class, long   .class, BinaryValueTranslators::copy_doubleTo_long   )
		.register(double.class, double .class, BinaryValueTranslators::copy_doubleTo_double )
		;
	}
	
	private static void registerPrimitivesToWrappers(final TypeMapping<BinaryValueSetter> mapping)
	{
		// (28.09.2018 TM)TODO: Legacy Type Mapping: Defaults for primitive -> primitive wrapper
	}
	
	private static void registerWrappersToPrimitives(final TypeMapping<BinaryValueSetter> mapping)
	{
		// (28.09.2018 TM)TODO: Legacy Type Mapping: Defaults for primitive wrapper -> primitive
	}
	
	private static void registerWrappersToWrappers(final TypeMapping<BinaryValueSetter> mapping)
	{
		// (28.09.2018 TM)TODO: Legacy Type Mapping: Defaults for primitive wrapper -> primitive wrapper
	}
	
	private static void registerCommonValueTypes(final TypeMapping<BinaryValueSetter> mapping)
	{
		/* (28.09.2018 TM)TODO: Legacy Type Mapping: Defaults for common value type translation
		 * (types that have no references to other - non-unshared - instances)
		 * 
		 * Obvious:
		 * String?
		 * BigInteger?
		 * BigDecimal?
		 * 
		 * Potentially:
		 * Date stuffs?
		 * File stuffs?
		 * StringBuilder stuffs?
		 * Primitive arrays?? (char[] is obvious, but then why not the other 7?)
		 * 
		 * Hm... writing and mapping converters for 20 types (8 primitives, 8 primitive wrappers plus the above)
		 * among each other would yield a whopping 400 methods.
		 * With primitive arrays, it would be near 800.
		 * With primitive wrapper arrays, near 1300.
		 * Hm...
		 */
	}
	
	private static int to_int(final boolean value)
	{
		return value
			? 1
			: 0
		;
	}
	
	public static long skip_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_byte();
	}
	
	public static long skip_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_boolean();
	}
	
	public static long skip_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_short();
	}
	
	public static long skip_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_char();
	}
	
	public static long skip_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_int();
	}
	
	public static long skip_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + Float.BYTES;
	}
	
	public static long skip_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_long();
	}
	
	public static long skip_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		return sourceAddress + XMemory.byteSize_double();
	}
		
	
	
	public static long copy_byteTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}

	public static long copy_byteTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_byte(sourceAddress));
		return sourceAddress + XMemory.byteSize_byte();
	}



	public static long copy_booleanTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, XMemory.get_boolean(sourceAddress));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}

	public static long copy_booleanTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, to_int(XMemory.get_boolean(sourceAddress)));
		return sourceAddress + XMemory.byteSize_boolean();
	}



	public static long copy_shortTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}

	public static long copy_shortTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_short(sourceAddress));
		return sourceAddress + XMemory.byteSize_short();
	}



	public static long copy_charTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}

	public static long copy_charTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_char(sourceAddress));
		return sourceAddress + XMemory.byteSize_char();
	}



	public static long copy_intTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}
	
	public static long copy_intTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}

	public static long copy_intTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_int(sourceAddress));
		return sourceAddress + XMemory.byteSize_int();
	}



	public static long copy_floatTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, (int)XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, (long)XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}

	public static long copy_floatTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_float(sourceAddress));
		return sourceAddress + Float.BYTES;
	}



	public static long copy_longTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, (int)XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}

	public static long copy_longTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_long(sourceAddress));
		return sourceAddress + XMemory.byteSize_long();
	}



	public static long copy_doubleTo_byte(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_byte(target, targetOffset, (byte)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_boolean(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_boolean(target, targetOffset, 0 != XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_short(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_short(target, targetOffset, (short)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_char(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_char(target, targetOffset, (char)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_int(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_int(target, targetOffset, (int)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_float(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_float(target, targetOffset, (float)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_long(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_long(target, targetOffset, (long)XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}

	public static long copy_doubleTo_double(
		final long                      sourceAddress,
		final Object                    target       ,
		final long                      targetOffset ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		XMemory.set_double(target, targetOffset, XMemory.get_double(sourceAddress));
		return sourceAddress + XMemory.byteSize_double();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private BinaryValueTranslators()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
