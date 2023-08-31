
package one.microstream.integrations.cdi.types.cache;

/*-
 * #%L
 * microstream-integrations-cdi3
 * %%
 * Copyright (C) 2019 - 2023 MicroStream Software
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
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.InjectionPoint;


/**
 * Load info the belongs to the {@link StorageCache} annotation.
 *
 * @param <K>
 *            the key property
 * @param <V>
 *            the value property
 */
class StorageCacheProperty<K, V>
{
	private final Class<K> key;
	
	private final Class<V> value;
	
	private final String   name;
	
	private StorageCacheProperty(final Class<K> key, final Class<V> value, final String name)
	{
		this.key   = key;
		this.value = value;
		this.name  = name;
	}
	
	public Class<K> getKey()
	{
		return this.key;
	}
	
	public Class<V> getValue()
	{
		return this.value;
	}
	
	public String getName()
	{
		return this.name;
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
		final StorageCacheProperty<?, ?> that = (StorageCacheProperty<?, ?>)o;
		return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value) && Objects.equals(this.name, that.name);
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(this.key, this.value, this.name);
	}
	
	@Override
	public String toString()
	{
		return "StorageCacheProperty{"
			+
			"key="
			+ this.key
			+
			", value="
			+ this.value
			+
			", name='"
			+ this.name
			+ '\''
			+
			'}';
	}
	
	@SuppressWarnings("unchecked")
	public static <K, V> StorageCacheProperty<K, V> of(final InjectionPoint injectionPoint)
	{
		final Annotated    annotated    = injectionPoint.getAnnotated();
		final StorageCache storageCache = annotated.getAnnotation(StorageCache.class);
		final String       cacheName    = storageCache.value();
		final Member       member       = injectionPoint.getMember();
		if(member instanceof Field)
		{
			final Field             field       = (Field)member;
			final ParameterizedType genericType = (ParameterizedType)field.getGenericType();
			final Type[]            arguments   = genericType.getActualTypeArguments();
			final Class<K>          key         = (Class<K>)arguments[0];
			final Class<V>          value       = (Class<V>)arguments[1];
			return new StorageCacheProperty<>(key, value, cacheName);
		}
		throw new IllegalArgumentException("");
	}
}
