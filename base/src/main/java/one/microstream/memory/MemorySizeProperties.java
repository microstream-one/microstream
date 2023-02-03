package one.microstream.memory;

/*-
 * #%L
 * microstream-base
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

import java.lang.reflect.Field;

public interface MemorySizeProperties
{
	// memory size querying logic //

	/**
	 * Returns the system's memory "page size" (whatever that may be exactly for a given system).
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



	public static MemorySizeProperties Unsupported()
	{
		return new MemorySizeProperties.Unsupported();
	}

	/**
	 * Default implementation that returns <code>-1</code> for every method.
	 *
	 * 
	 */
	public final class Unsupported implements MemorySizeProperties
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Unsupported()
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
		public final int byteSizeInstance(final Class<?> type)
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
		public final long byteSizeArray_boolean(final long elementCount)
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
