
package one.microstream.integrations.cdi.types.extension;

/*-
 * #%L
 * microstream-integrations-cdi
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
import java.util.Objects;
import java.util.function.Supplier;

import one.microstream.exceptions.IllegalAccessRuntimeException;


class FieldMetadata implements Supplier<Field>
{
	private final Field  field;
	private final String name ;
	
	private FieldMetadata(final Field field)
	{
		this.field = field;
		this.name  = field.getName();
	}
	
	Object read(final Object entity)
	{
		try
		{
			return this.field.get(entity);
		}
		catch(final IllegalAccessException e)
		{
			throw new IllegalAccessRuntimeException(e);
		}
	}
	
	public String getName()
	{
		return this.name;
	}
	
	@Override
	public Field get()
	{
		return this.field;
	}
	
	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || this.getClass() != o.getClass())
		{
			return false;
		}
		final FieldMetadata that = (FieldMetadata)o;
		return Objects.equals(this.field, that.field);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.field);
	}
	
	@Override
	public String toString()
	{
		return "FieldMetadata{"
			+
			"field="
			+ this.field
			+
			", name='"
			+ this.name
			+ '\''
			+
			'}';
	}
	
	static FieldMetadata of(final Field field)
	{
		return new FieldMetadata(field);
	}
	
}
