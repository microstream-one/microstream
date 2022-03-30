package one.microstream.storage.restadapter.types;

/*-
 * #%L
 * microstream-storage-restadapter
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

import java.lang.reflect.Type;

import one.microstream.persistence.binary.types.Binary;

public class ViewerBinaryPrimitivesReader
{
	public static Object readPrimitive(final Type type, final Binary bytes, final long offset )
	{
		if(type == char.class)
		{
			return bytes.read_char(offset);
		}

		if(type == boolean.class)
		{
			return bytes.read_boolean(offset);
		}

		if(type == byte.class)
		{
			return bytes.read_byte(offset);
		}

		if(type == short.class)
		{
			return bytes.read_short(offset);
		}

		if(type == int.class)
		{
			return bytes.read_int(offset);
		}

		if(type == long.class)
		{
			return bytes.read_long(offset);
		}

		if(type == float.class)
		{
			return bytes.read_float(offset);
		}

		if(type == double.class)
		{
			return bytes.read_double(offset);
		}

		return null;
	}

	public static long readReference(final Binary bytes, final long offset)
	{
		return bytes.read_long(offset);
	}
}
