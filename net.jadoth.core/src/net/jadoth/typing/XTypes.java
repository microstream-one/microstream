package net.jadoth.typing;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import net.jadoth.exceptions.NumberRangeException;

/**
 * Collection of generic util logic missing or too complicated in JDK API.
 *
 * @author Thomas Muenz
 */
public final class XTypes
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static boolean isBooleanType(final Class<?> c)
	{
		return c == boolean.class || c == Boolean.class;
	}

	public static boolean isByteType(final Class<?> c)
	{
		return c == byte.class || c == Byte.class;
	}

	public static boolean isShortType(final Class<?> c)
	{
		return c == short.class || c == Short.class;
	}

	public static boolean isIntegerType(final Class<?> c)
	{
		return c == int.class || c == Integer.class;
	}

	public static boolean isLongType(final Class<?> c)
	{
		return c == long.class || c == Long.class;
	}

	public static boolean isFloatType(final Class<?> c)
	{
		return c == float.class || c == Float.class;
	}

	public static boolean isDoubleType(final Class<?> c)
	{
		return c == double.class || c == Double.class;
	}

	public static boolean isCharacterType(final Class<?> c)
	{
		return c == char.class || c == Character.class;
	}

	// just for conformity in use along with the other ones
	public static boolean isStringType(final Class<?> c)
	{
		return c == String.class;
	}
	
	public static boolean isCharSequenceType(final Class<?> c)
	{
		return CharSequence.class.isAssignableFrom(c);
	}

	public static boolean isNaturalNumberType(final Class<?> c)
	{
		return c == byte.class
			|| c == Byte.class
			|| c == short.class
			|| c == Short.class
			|| c == int.class
			|| c == Integer.class
			|| c == long.class
			|| c == Long.class
			|| c == BigInteger.class
			|| c == AtomicInteger.class
			|| c == AtomicLong.class
		;
	}

	public static boolean isDecimalType(final Class<?> c)
	{
		return c == float.class
			|| c == Float.class
			|| c == double.class
			|| c == Double.class
			|| c == BigDecimal.class
		;
	}

	public static boolean isNumberType(final Class<?> c)
	{
		return c == byte.class
			|| c == short.class
			|| c == int.class
			|| c == long.class
			|| c == float.class
			|| c == double.class
			|| Number.class.isAssignableFrom(c)
		;
	}

	public static boolean isLiteralType(final Class<?> c)
	{
		return c == String.class || c == char.class || c == Character.class;
	}

	public static boolean isValueType(final Class<?> c)
	{
		// all value types, ordered in common use probability
		return c == String.class
			|| c == Integer.class
			|| c == Long.class
			|| c == Character.class
			|| c == Double.class
			|| c == Byte.class
			|| c == Boolean.class
			|| c == Float.class
			|| c == Short.class
			|| c == BigInteger.class
			|| c == BigDecimal.class
			|| c == Field.class // Field instances are no cached singletons but get copied over and over as value types
			|| ValueType.class.isAssignableFrom(c)
		;
	}

	
	
	public static boolean isNaturalNumber(final Object o)
	{
		// NOT Float or Double
		return o instanceof Integer
			|| o instanceof Short
			|| o instanceof Long
			|| o instanceof Byte
			|| o instanceof BigInteger
			|| o instanceof AtomicInteger
			|| o instanceof AtomicLong
		;
	}

	// just for conformity in use along with the other ones
	public static boolean isNumber(final Object o)
	{
		return o instanceof Number;
	}

	public static boolean isDecimal(final Object o)
	{
		return o instanceof Float || o instanceof Double || o instanceof BigDecimal;
	}

	public static boolean isLiteral(final Object o)
	{
		return o instanceof String || o instanceof Character;
	}

	// just for conformity in use along with the other ones
	public static boolean isBoolean(final Object o)
	{
		return o instanceof Boolean;
	}

	/**
	 * Checks if the type of the passed instance is an immutable special value type of the java language.
	 * This includes all primitive wrappers and {@link String}.
	 *
	 * @param o the instance to be checked
	 * @return {@code true} if the type of the passed instance is an immutable special value type.
	 */
	public static boolean isValueType(final Object o)
	{
		// all value types, ordered in common use probability
		return o instanceof String
			|| isPrimitiveWrapper(o)
			|| o instanceof BigInteger
			|| o instanceof BigDecimal
			|| o instanceof ValueType
		;
	}

	public static boolean isPrimitiveWrapper(final Object o)
	{
		// all primitive wrapper types, ordered in common use probability
		return o instanceof Integer
			|| o instanceof Long
			|| o instanceof Double
			|| o instanceof Character
			|| o instanceof Boolean
			|| o instanceof Float
			|| o instanceof Byte
			|| o instanceof Short
		;
	}

	public static final int to_int(final boolean value)
	{
		return value
			? 1
			: 0
		;
	}
	
	public static final int to_int(final long value) throws NumberRangeException
	{
		if(value > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(value + " > " + Integer.MAX_VALUE);
		}
		else if(value < Integer.MIN_VALUE)
		{
			throw new IllegalArgumentException(value + " < " + Integer.MIN_VALUE);
		}
		return (int)value;
	}

	public static final int to_int(final float value) throws NumberRangeException
	{
		if(value > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(value + " > " + Integer.MAX_VALUE);
		}
		else if(value < Integer.MIN_VALUE)
		{
			throw new IllegalArgumentException(value + " < " + Integer.MIN_VALUE);
		}
		return (int)value;
	}

	public static final int to_int(final double value) throws NumberRangeException
	{
		if(value > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException(value + " > " + Integer.MAX_VALUE);
		}
		else if(value < Integer.MIN_VALUE)
		{
			throw new IllegalArgumentException(value + " < " + Integer.MIN_VALUE);
		}
		return (int)value;
	}

	public static final int to_int(final Number value) throws NumberRangeException, NullPointerException
	{
		return to_int(value.longValue());
	}
	
	
	
	public static final Byte asByte(final Number value)
	{
		return value == null
			? null
			: value instanceof Byte
				? (Byte)value
				: Byte.valueOf(value.byteValue())
		;
	}
	
	public static final Short asShort(final Number value)
	{
		return value == null
			? null
			: value instanceof Short
				? (Short)value
				: Short.valueOf(value.shortValue())
		;
	}
	
	public static final Integer asInteger(final Number value)
	{
		return value == null
			? null
			: value instanceof Integer
				? (Integer)value
				: Integer.valueOf(value.intValue())
		;
	}
	
	public static final Float asFloat(final Number value)
	{
		return value == null
			? null
			: value instanceof Float
				? (Float)value
				: Float.valueOf(value.floatValue())
		;
	}
	
	public static final Long asLong(final Number value)
	{
		return value == null
			? null
			: value instanceof Long
				? (Long)value
				: Long.valueOf(value.longValue())
		;
	}
	
	public static final Double asDouble(final Number value)
	{
		return value == null
			? null
			: value instanceof Double
				? (Double)value
				: Double.valueOf(value.doubleValue())
		;
	}
	
	
	public static final TypeMapping<Float> createDefaultTypeSimilarity()
	{
		final Class<?>[] primitives =
		{
			byte.class, boolean.class, short.class, char.class, int.class, float.class, long.class, double.class
		};
		
		final Class<?>[] wrappers =
		{
			Byte.class, Boolean.class, Short.class, Character.class, Integer.class, Float.class, Long.class, Double.class
		};
		
		// always fun to use a class as a database :-D
		final int[][] primSims =
		{
			{100, 50, 80, 70, 60, 30, 40, 30},
			{ 50,100, 40, 10, 30, 20, 20, 10},
			{ 80, 40,100, 50, 80, 50, 60, 50},
			{ 70, 10, 50,100, 50, 40, 30, 20},
			{ 60, 30, 80, 50,100, 70, 80, 60},
			{ 30, 20, 50, 40, 70,100, 60, 80},
			{ 40, 20, 60, 30, 80, 60,100, 70},
			{ 30, 10, 50, 20, 60, 80, 70,100}
		};
		
		final Function percent   = value -> value * 0.010f;
		final Function prim2Wrap = value -> value * 0.008f; // primitive-to-wrapper similarity is flat 80%.
						
		final TypeMapping<Float> typeSimilarities = TypeMapping.New();
		
		// 256 mapping for the 8 primitives and their 8 wrappers amongst each other
		for(int x = 0; x < primitives.length; x++)
		{
			registerDual(typeSimilarities, primitives[x], percent  , primitives, primSims[x]);
			registerDual(typeSimilarities, wrappers  [x], percent  , wrappers  , primSims[x]);
			registerDual(typeSimilarities, wrappers  [x], prim2Wrap, primitives, primSims[x]);
			registerDual(typeSimilarities, primitives[x], prim2Wrap, wrappers  , primSims[x]);
		}
		
		// additional common types' similarities to the primitives
		registerDual(typeSimilarities, BigInteger.class, percent, primitives, 30, 10, 50, 20, 70, 50, 90, 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, primitives, 20,  5, 40, 10, 50, 70, 60, 90);
		registerDual(typeSimilarities, String    .class, percent, primitives, 20, 10, 20, 60, 20, 30, 20, 30);
		registerDual(typeSimilarities, Date      .class, percent, primitives, 10,  0, 20,  0, 30,  0, 60,  0);

		// additional common types' similarities to the primitives wrappers (values identical to primitives above)
		registerDual(typeSimilarities, BigInteger.class, percent, wrappers, 30, 10, 50, 20, 70, 50, 90, 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, wrappers, 20,  5, 40, 10, 50, 70, 60, 90);
		registerDual(typeSimilarities, String    .class, percent, wrappers, 20, 10, 20, 60, 20, 30, 20, 30);
		registerDual(typeSimilarities, Date      .class, percent, wrappers, 10,  0, 20,  0, 30,  0, 60,  0);

		// additional common types among each other, without self-mappings
		registerDual(typeSimilarities, BigInteger.class, percent, BigDecimal.class, 80);
		registerDual(typeSimilarities, BigInteger.class, percent, String.class    , 20);
		registerDual(typeSimilarities, BigInteger.class, percent, Date.class      , 60);
		registerDual(typeSimilarities, BigDecimal.class, percent, String.class    , 30);
		registerDual(typeSimilarities, BigDecimal.class, percent, Date.class      , 20);
		registerDual(typeSimilarities, String.class    , percent, Date.class      , 40);
		
		return typeSimilarities;
	}
		
	interface Function
	{
		public float apply(int value);
	}
		
	static void registerDual(
		final TypeMapping<Float> typeSimilarities,
		final Class<?>           type           ,
		final Function           function        ,
		final Class<?>[]         types           ,
		final int...             values
	)
	{
		for(int i = 0; i < types.length; i++)
		{
			registerDual(typeSimilarities, type, function, types[i], values[i]);
		}
	}
	
	static void registerDual(
		final TypeMapping<Float> typeSimilarities,
		final Class<?>           type1           ,
		final Function           function        ,
		final Class<?>           type2           ,
		final int                value
	)
	{
		final float similarity = function.apply(value);
		typeSimilarities.register(type1, type2, similarity);
		typeSimilarities.register(type2, type1, similarity);
	}
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private XTypes()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
