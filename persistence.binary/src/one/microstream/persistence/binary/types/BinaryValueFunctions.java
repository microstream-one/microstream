package one.microstream.persistence.binary.types;

import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryValueFunctions
{
	private static final BinaryValueStorer STORE_1 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_byte(targetAddress, XMemory.get_byte(src, srcOffset));
			return targetAddress + Byte.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_2 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_short(targetAddress, XMemory.get_short(src, srcOffset));
			return targetAddress + Short.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_4 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_int(targetAddress, XMemory.get_int(src, srcOffset));
			return targetAddress + Integer.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_8 = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(targetAddress, XMemory.get_long(src, srcOffset));
			return targetAddress + Long.BYTES;
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(targetAddress, handler.apply(XMemory.getObject(src, srcOffset)));
			return targetAddress + Binary.oidByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_EAGER = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  source       ,
			final long                    sourceOffset ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(targetAddress, handler.applyEager(XMemory.getObject(source, sourceOffset)));
			return targetAddress + Binary.oidByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_2_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_short(targetAddress, Short.reverseBytes(XMemory.get_short(src, srcOffset)));
			return targetAddress + Short.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_4_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_int(targetAddress, Integer.reverseBytes(XMemory.get_int(src, srcOffset)));
			return targetAddress + Integer.BYTES;
		}
	};

	private static final BinaryValueStorer STORE_8_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(targetAddress, Long.reverseBytes(XMemory.get_long(src, srcOffset)));
			return targetAddress + Long.BYTES;
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  src          ,
			final long                    srcOffset    ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(
				targetAddress,
				Long.reverseBytes(handler.apply(XMemory.getObject(src, srcOffset)))
			);
			return targetAddress + Binary.oidByteLength();
		}
	};
	
	private static final BinaryValueStorer STORE_REFERENCE_EAGER_REVERSED = new BinaryValueStorer()
	{
		@Override
		public long storeValueFromMemory(
			final Object                  source       ,
			final long                    sourceOffset ,
			final long                    targetAddress,
			final PersistenceStoreHandler handler
		)
		{
			XMemory.set_long(
				targetAddress,
				Long.reverseBytes(handler.applyEager(XMemory.getObject(source, sourceOffset)))
			);
			return targetAddress + Binary.oidByteLength();
		}
	};
	
	private static final BinaryValueSetter SETTER_1 = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
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
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
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
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
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
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
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
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.setObject(target, targetOffset, idResolver.lookupObject(XMemory.get_long(sourceAddress)));
			return sourceAddress + Binary.oidByteLength();
		}
	};
	
	private static final BinaryValueSetter SETTER_2_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_short(target, targetOffset, Short.reverseBytes(XMemory.get_short(sourceAddress)));
			return sourceAddress + Short.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_4_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_int(target, targetOffset, Integer.reverseBytes(XMemory.get_int(sourceAddress)));
			return sourceAddress + Integer.BYTES;
		}
	};

	private static final BinaryValueSetter SETTER_8_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.set_long(target, targetOffset, Long.reverseBytes(XMemory.get_long(sourceAddress)));
			return sourceAddress + Long.BYTES;
		}
	};
	
	private static final BinaryValueSetter SETTER_REF_REVERSED = new BinaryValueSetter()
	{
		@Override
		public long setValueToMemory(
			final long                        sourceAddress,
			final Object                      target       ,
			final long                        targetOffset ,
			final PersistenceObjectIdResolver idResolver
		)
		{
			XMemory.setObject(
				target,
				targetOffset,
				idResolver.lookupObject(Long.reverseBytes(XMemory.get_long(sourceAddress)))
			);
			return sourceAddress + Binary.oidByteLength();
		}
	};

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
		// primitive special cases
		if(type.isPrimitive())
		{
			// "forced" is not applicable for primitive values
			switch(XMemory.byteSizePrimitive(type))
			{
				case Byte.BYTES   : return STORE_1; // byte & boolean
				case Short.BYTES  : return STORE_2_REVERSED; // short & char
				case Integer.BYTES: return STORE_4_REVERSED; // int & float
				case Long.BYTES   : return STORE_8_REVERSED; // long & double
				default: throw new IllegalArgumentException();
			}
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

		// normal case of standard reference as anything that is not primitive must be a reference.
		return SETTER_REF;
	}
	
	public static BinaryValueSetter getObjectValueSetterReversed(final Class<?> type)
	{
		// primitive special cases
		if(type.isPrimitive())
		{
			switch(XMemory.byteSizePrimitive(type))
			{
				case Byte.BYTES   : return SETTER_1; // byte & boolean
				case Short.BYTES  : return SETTER_2_REVERSED; // short & char
				case Integer.BYTES: return SETTER_4_REVERSED; // int & float
				case Long.BYTES   : return SETTER_8_REVERSED; // long & double
				default: throw new IllegalArgumentException();
			}
		}

		// normal case of standard reference as anything that is not primitive must be a reference.
		return SETTER_REF_REVERSED;
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private BinaryValueFunctions()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
