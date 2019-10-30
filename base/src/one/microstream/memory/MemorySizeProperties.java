package one.microstream.memory;

import java.lang.reflect.Field;

public interface MemorySizeProperties
{
	// memory size querying logic //
	
	/**
	 * Returns the system's memory "page size" (whatever that may be exactely for a given system).
	 * Use with care (and the dependency to a system value in mind!).
	 * 
	 * @return the system's memory "page size".
	 */
	public int pageSize();
		
	public int byteSizeReference();
	
	public int byteSizeInstance(Class<?> type);
	
	public int byteSizeObjectHeader(Class<?> type);

	public default int byteSizeFieldValue(final Field field)
	{
		return this.byteSizeFieldValue(field.getType());
	}
	
	public int byteSizeFieldValue(Class<?> type);
	
	public long byteSizeArray_byte(long elementCount);

	public long byteSizeArray_boolean(long elementCount);

	public long byteSizeArray_short(long elementCount);

	public long byteSizeArray_char(long elementCount);

	public long byteSizeArray_int(long elementCount);

	public long byteSizeArray_float(long elementCount);

	public long byteSizeArray_long(long elementCount);

	public long byteSizeArray_double(long elementCount);

	public long byteSizeArrayObject(long elementCount);
	
		
	
	public static MemorySizeProperties Default()
	{
		return new MemorySizeProperties.Default();
	}
	
	/**
	 * Default implementation that returns {@value -1} for every method.
	 * 
	 * @author TM
	 */
	public final class Default implements MemorySizeProperties
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final int pageSize()
		{
			return -1;
		}

		@Override
		public final int byteSizeReference()
		{
			return -1;
		}

		@Override
		public int byteSizeInstance(final Class<?> type)
		{
			return -1;
		}

		@Override
		public final int byteSizeObjectHeader(final Class<?> type)
		{
			return -1;
		}

		@Override
		public final int byteSizeFieldValue(final Class<?> type)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_byte(final long elementCount)
		{
			return -1;
		}

		@Override
		public long byteSizeArray_boolean(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_short(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_char(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_int(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_float(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_long(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArray_double(final long elementCount)
		{
			return -1;
		}

		@Override
		public final long byteSizeArrayObject(final long elementCount)
		{
			return -1;
		}
		
	}
	
}
