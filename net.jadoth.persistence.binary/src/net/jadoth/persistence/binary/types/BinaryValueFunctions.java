package net.jadoth.persistence.binary.types;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.types.PersistenceObjectIdResolver;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryValueFunctions
{
	// (31.01.2019 TM)FIXME: JET-49: must create a second set of binary value functions with byte order switching.
	
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
			return address + Binary.oidByteLength();
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
			return targetAddress + Binary.oidByteLength();
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
			return sourceAddress + Binary.oidByteLength();
		}
	};

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

		// normal case of standard reference as anything that is not primitive must be a reference.
		return SETTER_REF;
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
