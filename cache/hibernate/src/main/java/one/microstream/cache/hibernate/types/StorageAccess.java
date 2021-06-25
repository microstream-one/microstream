package one.microstream.cache.hibernate.types;

/*-
 * #%L
 * microstream-cache-hibernate
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import static one.microstream.X.notNull;

import javax.cache.CacheException;

import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.engine.spi.SharedSessionContractImplementor;

import one.microstream.cache.types.Cache;


public interface StorageAccess extends DomainDataStorageAccess
{
	public static StorageAccess New(
		final Cache<Object, Object> cache
	)
	{
		return new Default(notNull(cache));
	}
	
	
	public static class Default implements StorageAccess
	{
		private final Cache<Object, Object> cache;
		
		Default(
			final Cache<Object, Object> cache
		)
		{
			super();
			this.cache = cache;
		}
		
		@Override
		public Object getFromCache(
			final Object key, 
			final SharedSessionContractImplementor session
		)
		{
			try
			{
				return this.cache.get(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void putIntoCache(
			final Object key, 
			final Object value, 
			final SharedSessionContractImplementor session
		)
		{
			try
			{
				this.cache.put(key, value);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public boolean contains(
			final Object key
		)
		{
			try
			{
				return this.cache.containsKey(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void evictData()
		{
			try
			{
				this.cache.removeAll();
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void evictData(
			final Object key
		)
		{
			try
			{
				this.cache.remove(key);
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
		@Override
		public void release()
		{
			try
			{
				this.cache.close();
			}
			catch(CacheException e)
			{
				throw new org.hibernate.cache.CacheException(e);
			}
		}
		
	}
	
}
