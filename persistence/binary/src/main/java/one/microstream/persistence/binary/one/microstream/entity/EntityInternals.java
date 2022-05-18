package one.microstream.persistence.binary.one.microstream.entity;

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

import java.lang.reflect.Field;

import one.microstream.collections.EqHashTable;
import one.microstream.entity.EntityLayerVersioning;
import one.microstream.entity.EntityVersionContext;
import one.microstream.memory.XMemory;
import one.microstream.memory.sun.JdkInternals;

final class EntityInternals
{
	// CHECKSTYLE.OFF: ConstantName: type names are intentionally unchanged
	private static final long
		OFFSET_EntityLayerVersioning_context  = getFieldOffset(EntityLayerVersioning.class, "context" ),
		OFFSET_EntityLayerVersioning_versions = getFieldOffset(EntityLayerVersioning.class, "versions")
	;
	// CHECKSTYLE.ON: ConstantName
	
	static final long getFieldOffset(final Class<?> type, final String declaredFieldName)
	{
		// minimal algorithm, only for local use
		for(Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass())
		{
			try
			{
				for(final Field field : c.getDeclaredFields())
				{
					if(field.getName().equals(declaredFieldName))
					{
						return JdkInternals.VM().objectFieldOffset(field);
					}
				}
			}
			catch(final Exception e)
			{
				throw new Error(e); // explode and die :)
			}
		}
		throw new Error("Field not found: " + type.getName() + '#' + declaredFieldName);
	}
	
	public static EntityVersionContext<?> getContext(final EntityLayerVersioning<?> layer)
	{
		return (EntityVersionContext<?>)XMemory.getObject(layer, OFFSET_EntityLayerVersioning_context);
	}
	
	public static EqHashTable<?, ?> getVersions(final EntityLayerVersioning<?> layer)
	{
		return (EqHashTable<?, ?>)XMemory.getObject(layer, OFFSET_EntityLayerVersioning_versions);
	}
	
	public static void setContext(final EntityLayerVersioning<?> layer, final EntityVersionContext<?> context)
	{
		XMemory.setObject(layer, OFFSET_EntityLayerVersioning_context, context);
	}
	
	public static void setVersions(final EntityLayerVersioning<?> layer, final EqHashTable<?, ?> versions)
	{
		XMemory.setObject(layer, OFFSET_EntityLayerVersioning_versions, versions);
	}
	
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private EntityInternals()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}
