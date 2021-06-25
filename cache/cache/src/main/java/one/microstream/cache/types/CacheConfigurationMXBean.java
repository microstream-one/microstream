
package one.microstream.cache.types;

/*-
 * #%L
 * microstream-cache
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

import javax.cache.configuration.CompleteConfiguration;


public interface CacheConfigurationMXBean extends javax.cache.management.CacheMXBean
{
	public static class Default implements CacheConfigurationMXBean
	{
		private final CompleteConfiguration<?, ?> configuration;
		
		Default(final CompleteConfiguration<?, ?> configuration)
		{
			super();
			
			this.configuration = configuration;
		}
		
		@Override
		public String getKeyType()
		{
			return this.configuration.getKeyType().getName();
		}
		
		@Override
		public String getValueType()
		{
			return this.configuration.getValueType().getName();
		}
		
		@Override
		public boolean isReadThrough()
		{
			return this.configuration.isReadThrough();
		}
		
		@Override
		public boolean isWriteThrough()
		{
			return this.configuration.isWriteThrough();
		}
		
		@Override
		public boolean isStoreByValue()
		{
			return this.configuration.isStoreByValue();
		}
		
		@Override
		public boolean isStatisticsEnabled()
		{
			return this.configuration.isStatisticsEnabled();
		}
		
		@Override
		public boolean isManagementEnabled()
		{
			return this.configuration.isManagementEnabled();
		}
		
	}
	
}
