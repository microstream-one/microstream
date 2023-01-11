package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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

import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryValueFunctions
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final BinaryValueStorer STORE_byte = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_byte(trgAddress, XMemory.get_byte(source, srcOffset));
			return trgAddress + Byte.BYTES;
		}
	};
	
	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_boolean = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_boolean(trgAddress, XMemory.get_boolean(source, srcOffset));
			return trgAddress + Byte.BYTES; // there is no Boolean.BYTES because lol
		}
	};

	private static final BinaryValueStorer STORE_short = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_short(trgAddress, XMemory.get_short(source, srcOffset));
			return trgAddress + Short.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_char = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_char(trgAddress, XMemory.get_char(source, srcOffset));
			return trgAddress + Character.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_int = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_int(trgAddress, XMemory.get_int(source, srcOffset));
			return trgAddress + Integer.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_float = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_float(trgAddress, XMemory.get_float(source, srcOffset));
			return trgAddress + Float.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_long = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(trgAddress, XMemory.get_long(source, srcOffset));
			return trgAddress + Long.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_double = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_double(trgAddress, XMemory.get_double(source, srcOffset));
			return trgAddress + Double.BYTES;
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(trgAddress, handler.apply(XMemory.getObject(source, srcOffset)));
			return trgAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_EAGER = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(trgAddress, handler.applyEager(XMemory.getObject(source, srcOffset)));
			return trgAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_short_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_short(trgAddress, Short.reverseBytes(XMemory.get_short(source, srcOffset)));
			return trgAddress + Short.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_char_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_char(trgAddress, Character.reverseBytes(XMemory.get_char(source, srcOffset)));
			return trgAddress + Character.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_int_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_int(trgAddress, Integer.reverseBytes(XMemory.get_int(source, srcOffset)));
			return trgAddress + Integer.BYTES;
		}
	};
	
	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_float_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final int rawBits = Float.floatToRawIntBits(XMemory.get_float(source, srcOffset));
			final int reversed = Integer.reverseBytes(rawBits);
						
			/* (21.11.2019 TM)NOTE:
			 * Float.intBitsToFloat JavaDoc states:
			 * "[...] this method may not be able to return a float NaN
			 * with exactly [the] same bit pattern as the int argument."
			 * To compensate for this, the reversed int is used directly.
			 * A switched byte order reader or the inverse function to this
			 * will both read the correct float value.
			 * DirectByteBuffer does the same thing when reversing a double.
			 */
			XMemory.set_int(trgAddress, reversed);
			return trgAddress + Float.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_long_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(trgAddress, Long.reverseBytes(XMemory.get_long(source, srcOffset)));
			return trgAddress + Long.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueStorer STORE_double_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			final long rawBits = Double.doubleToRawLongBits(XMemory.get_double(source, srcOffset));
			final long reversed = Long.reverseBytes(rawBits);
			
			/* (21.11.2019 TM)NOTE:
			 * Double.longBitsToDouble JavaDoc states:
			 * "[...] this method may not be able to return a double NaN
			 * with exactly [the] same bit pattern as the long argument."
			 * To compensate for this, the reversed long is used directly.
			 * A switched byte order reader or the inverse function to this
			 * will both read the correct double value.
			 * DirectByteBuffer does the same thing when reversing a double.
			 */
			XMemory.set_long(trgAddress, reversed);
			return trgAddress + Double.BYTES;
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(
				trgAddress,
				Long.reverseBytes(handler.apply(XMemory.getObject(source, srcOffset)))
			);
			return trgAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_EAGER_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                          source    ,
			final long                            srcOffset ,
			final long                            trgAddress,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			XMemory.set_long(
				trgAddress,
				Long.reverseBytes(handler.applyEager(XMemory.getObject(source, srcOffset)))
			);
			return trgAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueSetter SETTER_byte = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_byte(target, trgOffset, XMemory.get_byte(srcAddress));
			return srcAddress + Byte.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_boolean = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_boolean(target, trgOffset, XMemory.get_boolean(srcAddress));
			return srcAddress + Byte.BYTES; // there is no Boolean.BYTES
		}
	};
	
	private static final BinaryValueSetter SETTER_char = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_char(target, trgOffset, XMemory.get_char(srcAddress));
			return srcAddress + Character.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_short = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_short(target, trgOffset, XMemory.get_short(srcAddress));
			return srcAddress + Short.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_int = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_int(target, trgOffset, XMemory.get_int(srcAddress));
			return srcAddress + Integer.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_float = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_float(target, trgOffset, XMemory.get_float(srcAddress));
			return srcAddress + Float.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_long = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_long(target, trgOffset, XMemory.get_long(srcAddress));
			return srcAddress + Long.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_double = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_double(target, trgOffset, XMemory.get_double(srcAddress));
			return srcAddress + Double.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_REF = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.setObject(target, trgOffset, handler.lookupObject(XMemory.get_long(srcAddress)));
			return srcAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueSetter SETTER_short_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_short(target, trgOffset, Short.reverseBytes(XMemory.get_short(srcAddress)));
			return srcAddress + Short.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_char_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_char(target, trgOffset, Character.reverseBytes(XMemory.get_char(srcAddress)));
			return srcAddress + Character.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_int_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_int(target, trgOffset, Integer.reverseBytes(XMemory.get_int(srcAddress)));
			return srcAddress + Integer.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_float_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			/* (21.11.2019 TM)NOTE:
			 * Float.intBitsToFloat JavaDoc states:
			 * "[...] this method may not be able to return a float NaN
			 * with exactly [the] same bit pattern as the int argument."
			 * To compensate for this, the raw value is read as an int and
			 * reversed to produce the proper bit pattern for the
			 * original/desired float value before calling the conversion.
			 */
			final int reversed = Integer.reverseBytes(XMemory.get_int(srcAddress));
			XMemory.set_float(target, trgOffset, Float.intBitsToFloat(reversed));
			return srcAddress + Float.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_long_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.set_long(target, trgOffset, Long.reverseBytes(XMemory.get_long(srcAddress)));
			return srcAddress + Long.BYTES;
		}
	};

	// required for use with reflection instead of Unsafe
	private static final BinaryValueSetter SETTER_double_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			/* (21.11.2019 TM)NOTE:
			 * Double.longBitsToDouble JavaDoc states:
			 * "[...] this method may not be able to return a double NaN
			 * with exactly [the] same bit pattern as the long argument."
			 * To compensate for this, the raw value is read as a long and
			 * reversed to produce the proper bit pattern for the
			 * original/desired double value before calling the conversion.
			 */
			final long reversed = Long.reverseBytes(XMemory.get_long(srcAddress));
			XMemory.set_double(target, trgOffset, Double.longBitsToDouble(reversed));
			return srcAddress + Double.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_REF_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			XMemory.setObject(
				target,
				trgOffset,
				handler.lookupObject(Long.reverseBytes(XMemory.get_long(srcAddress)))
			);
			return srcAddress + Binary.objectIdByteLength();
		}
	};
	
	private static final BinaryValueSetter SETTER_SKIP_1 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			// just skipping the binary value
			return srcAddress + Byte.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_SKIP_2 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			// just skipping the binary value
			return srcAddress + Short.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_SKIP_4 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			// just skipping the binary value
			return srcAddress + Integer.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_SKIP_8 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                   srcAddress,
			final Object                 target    ,
			final long                   trgOffset ,
			final PersistenceLoadHandler handler
		)
		{
			// just skipping the binary value
			return srcAddress + Long.BYTES;
		}
	};

	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryValueStorer getObjectValueStorer(
		final Class<?> type           ,
		final boolean  isEager        ,
		final boolean  switchByteOrder
	)
		throws IllegalArgumentException
	{
		if(switchByteOrder)
		{
			return getObjectValueStorerReversed(type, isEager);
		}
		
		/* Primitive special cases.
		 * Must be all 8 type-specific instead of just 4 size-specific for reflection fallback
		 * See MemoryAccessorGeneric.
		 */
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			if(type == byte.class)
			{
				return STORE_byte;
			}
			if(type == boolean.class)
			{
				return STORE_boolean;
			}
			if(type == short.class)
			{
				return STORE_short;
			}
			if(type == char.class)
			{
				return STORE_char;
			}
			if(type == int.class)
			{
				return STORE_int;
			}
			if(type == float.class)
			{
				return STORE_float;
			}
			if(type == long.class)
			{
				return STORE_long;
			}
			if(type == double.class)
			{
				return STORE_double;
			}
			
			// unknown / unhandled primitive (e.g. void)
			throw new IllegalArgumentException();
		}

		// reference case. Either "eager" or normal.
		return isEager
			? STORE_REFERENCE_EAGER
			: STORE_REFERENCE
		;
	}
	
	public static BinaryValueStorer getObjectValueStorerReversed(
		final Class<?> type   ,
		final boolean  isEager
	)
		throws IllegalArgumentException
	{
		/* Primitive special cases.
		 * Must be all 8 type-specific instead of just 4 size-specific for reflection fallback
		 * See MemoryAccessorGeneric.
		 */
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			if(type == byte.class)
			{
				return STORE_byte;
			}
			if(type == boolean.class)
			{
				return STORE_boolean;
			}
			if(type == short.class)
			{
				return STORE_short_REVERSED;
			}
			if(type == char.class)
			{
				return STORE_char_REVERSED;
			}
			if(type == int.class)
			{
				return STORE_int_REVERSED;
			}
			if(type == float.class)
			{
				return STORE_float_REVERSED;
			}
			if(type == long.class)
			{
				return STORE_long_REVERSED;
			}
			if(type == double.class)
			{
				return STORE_double_REVERSED;
			}
			
			// unknown / unhandled primitive (e.g. void)
			throw new IllegalArgumentException();
		}

		// reference case. Either "eager" or normal.
		return isEager
			? STORE_REFERENCE_EAGER_REVERSED
			: STORE_REFERENCE_REVERSED
		;
	}

	public static BinaryValueSetter getObjectValueSetter(final Class<?> type, final boolean switchByteOrder)
	{
		if(switchByteOrder)
		{
			return getObjectValueSetterReversed(type);
		}
		
		/* Primitive special cases.
		 * Must be all 8 type-specific instead of just 4 size-specific for reflection fallback
		 * See MemoryAccessorGeneric.
		 */
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			if(type == byte.class)
			{
				return SETTER_byte;
			}
			if(type == boolean.class)
			{
				return SETTER_boolean;
			}
			if(type == short.class)
			{
				return SETTER_short;
			}
			if(type == char.class)
			{
				return SETTER_char;
			}
			if(type == int.class)
			{
				return SETTER_int;
			}
			if(type == float.class)
			{
				return SETTER_float;
			}
			if(type == long.class)
			{
				return SETTER_long;
			}
			if(type == double.class)
			{
				return SETTER_double;
			}
			
			// unknown / unhandled primitive (e.g. void)
			throw new IllegalArgumentException();
		}

		// normal case of standard reference as anything that is not primitive must be a reference.
		return SETTER_REF;
	}
	
	public static BinaryValueSetter getObjectValueSetterReversed(final Class<?> type)
	{
		/* Primitive special cases.
		 * Must be all 8 type-specific instead of just 4 size-specific for reflection fallback
		 * See MemoryAccessorGeneric.
		 */
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			if(type == byte.class)
			{
				return SETTER_byte;
			}
			if(type == boolean.class)
			{
				return SETTER_boolean;
			}
			if(type == short.class)
			{
				return SETTER_short_REVERSED;
			}
			if(type == char.class)
			{
				return SETTER_char_REVERSED;
			}
			if(type == int.class)
			{
				return SETTER_int_REVERSED;
			}
			if(type == float.class)
			{
				return SETTER_float_REVERSED;
			}
			if(type == long.class)
			{
				return SETTER_long_REVERSED;
			}
			if(type == double.class)
			{
				return SETTER_double_REVERSED;
			}
			
			// unknown / unhandled primitive (e.g. void)
			throw new IllegalArgumentException();
		}

		// normal case of standard reference as anything that is not primitive must be a reference.
		return SETTER_REF_REVERSED;
	}
	
	public static BinaryValueSetter getObjectValueSettingSkipper(final Class<?> type)
	{
		// note: byte order is irrelevant for just skipping n bytes
		
		switch(BinaryPersistence.binaryValueSize(type))
		{
			case Byte.BYTES   : return SETTER_SKIP_1; // byte & boolean
			case Short.BYTES  : return SETTER_SKIP_2; // short & char
			case Integer.BYTES: return SETTER_SKIP_4; // int & float
			case Long.BYTES   : return SETTER_SKIP_8; // long & double & reference
			default: throw new IllegalArgumentException(); // e.g. void
		}
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private BinaryValueFunctions()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
