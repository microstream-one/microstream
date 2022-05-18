
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

import javax.cache.configuration.CacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListener;


public interface CacheEntryListenerRegistration<K, V>
{
	public CacheEntryListener<? super K, ? super V> getCacheEntryListener();
	
	public CacheEntryEventFilter<? super K, ? super V> getCacheEntryFilter();
	
	public CacheEntryListenerConfiguration<K, V> getConfiguration();
	
	public boolean isOldValueRequired();
	
	public boolean isSynchronous();
	
	public static <K, V> CacheEntryListenerRegistration<K, V>
		New(final CacheEntryListenerConfiguration<K, V> configuration)
	{
		return new Default<>(configuration);
	}
	
	public static class Default<K, V> implements CacheEntryListenerRegistration<K, V>
	{
		private final CacheEntryListenerConfiguration<K, V>       configuration;
		private final CacheEntryListener<? super K, ? super V>    listener;
		private final CacheEntryEventFilter<? super K, ? super V> filter;
		private final boolean                                     oldValueRequired;
		private final boolean                                     synchronous;
		
		Default(final CacheEntryListenerConfiguration<K, V> configuration)
		{
			super();
			
			this.configuration    = configuration;
			this.listener         = configuration.getCacheEntryListenerFactory().create();
			this.filter           = configuration.getCacheEntryEventFilterFactory() == null
				? null
				: configuration.getCacheEntryEventFilterFactory().create();
			this.oldValueRequired = configuration.isOldValueRequired();
			this.synchronous      = configuration.isSynchronous();
		}
		
		@Override
		public CacheEntryListener<? super K, ? super V> getCacheEntryListener()
		{
			return this.listener;
		}
		
		@Override
		public CacheEntryEventFilter<? super K, ? super V> getCacheEntryFilter()
		{
			return this.filter;
		}
		
		@Override
		public CacheEntryListenerConfiguration<K, V> getConfiguration()
		{
			return this.configuration;
		}
		
		@Override
		public boolean isOldValueRequired()
		{
			return this.oldValueRequired;
		}
		
		@Override
		public boolean isSynchronous()
		{
			return this.synchronous;
		}
		
		@Override
		public int hashCode()
		{
			final int prime  = 31;
			int       result = 1;
			result = prime * result + (this.filter == null ? 0 : this.filter.hashCode());
			result = prime * result + (this.oldValueRequired ? 1231 : 1237);
			result = prime * result + (this.synchronous ? 1231 : 1237);
			result = prime * result + (this.listener == null ? 0 : this.listener.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(final Object object)
		{
			if(this == object)
			{
				return true;
			}
			if(object == null)
			{
				return false;
			}
			if(!(object instanceof CacheEntryListenerRegistration))
			{
				return false;
			}
			final CacheEntryListenerRegistration<?, ?> other = (CacheEntryListenerRegistration<?, ?>)object;
			if(this.filter == null)
			{
				if(other.getCacheEntryFilter() != null)
				{
					return false;
				}
			}
			else if(!this.filter.equals(other.getCacheEntryFilter()))
			{
				return false;
			}
			if(this.oldValueRequired != other.isOldValueRequired())
			{
				return false;
			}
			if(this.synchronous != other.isSynchronous())
			{
				return false;
			}
			if(this.listener == null)
			{
				if(other.getCacheEntryListener() != null)
				{
					return false;
				}
			}
			else if(!this.listener.equals(other.getCacheEntryListener()))
			{
				return false;
			}
			return true;
		}
		
	}
	
}
