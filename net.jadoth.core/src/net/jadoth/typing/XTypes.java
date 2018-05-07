/*
 * Copyright (c) 2008-2010, Thomas Muenz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.jadoth.typing;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
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
	// Class Type Classifiers //
	///////////////////////////

	public static boolean isBoolean(final Class<?> c)
	{
		return c == boolean.class || c == Boolean.class;
	}

	public static boolean isByte(final Class<?> c)
	{
		return c == byte.class || c == Byte.class;
	}

	public static boolean isShort(final Class<?> c)
	{
		return c == short.class || c == Short.class;
	}

	public static boolean isInteger(final Class<?> c)
	{
		return c == int.class || c == Integer.class;
	}

	public static boolean isLong(final Class<?> c)
	{
		return c == long.class || c == Long.class;
	}

	public static boolean isFloat(final Class<?> c)
	{
		return c == float.class || c == Float.class;
	}

	public static boolean isDouble(final Class<?> c)
	{
		return c == double.class || c == Double.class;
	}

	public static boolean isCharacter(final Class<?> c)
	{
		return c == char.class || c == Character.class;
	}

	//just for conformity in use along with the other ones
	public static boolean isString(final Class<?> c)
	{
		return c == String.class;
	}



	///////////////////////////////////////////////////////////////////////////
	// Class Category Classifiers //
	///////////////////////////////

	public static boolean isNaturalNumber(final Class<?> c)
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

	public static boolean isDecimal(final Class<?> c)
	{
		return c == float.class
			|| c == Float.class
			|| c == double.class
			|| c == Double.class
			|| c == BigDecimal.class
		;
	}

	public static boolean isNumber(final Class<?> c)
	{
		return c == byte.class
			|| c == Byte.class
			|| c == short.class
			|| c == Short.class
			|| c == int.class
			|| c == Integer.class
			|| c == long.class
			|| c == Long.class
			|| c == float.class
			|| c == Float.class
			|| c == double.class
			|| c == Double.class
			|| c == BigInteger.class
			|| c == AtomicInteger.class
			|| c == AtomicLong.class
			|| c == BigDecimal.class
		;
	}

	public static boolean isLiteral(final Class<?> c)
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



	///////////////////////////////////////////////////////////////////////////
	// Object Category Classifiers //
	////////////////////////////////

	public static boolean isNaturalNumber(final Object o)
	{
		// NOT Float and Double
		return o instanceof Byte
			|| o instanceof Short
			|| o instanceof Integer
			|| o instanceof Long
			|| o instanceof BigInteger
			|| o instanceof AtomicInteger
			|| o instanceof AtomicLong
		;
	}

	//just for conformity in use along with the other ones
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

	//just for conformity in use along with the other ones
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
			|| isPrimitiveWrapperType(o)
			|| o instanceof BigInteger
			|| o instanceof BigDecimal
			|| o instanceof ValueType
		;
	}

	public static boolean isPrimitiveWrapperType(final Object o)
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
	

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private XTypes()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
