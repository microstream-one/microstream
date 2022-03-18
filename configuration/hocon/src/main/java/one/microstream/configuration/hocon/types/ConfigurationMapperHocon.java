package one.microstream.configuration.hocon.types;

/*-
 * #%L
 * microstream-configuration-hocon
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

import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import one.microstream.configuration.types.Configuration;
import one.microstream.configuration.types.ConfigurationMapper;
import one.microstream.configuration.types.Configuration.Builder;

public interface ConfigurationMapperHocon extends ConfigurationMapper<ConfigObject>
{
	public static ConfigurationMapperHocon New()
	{
		return new ConfigurationMapperHocon.Default();
	}
	
	
	public static class Default implements ConfigurationMapperHocon
	{
		Default()
		{
			super();
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder      builder,
			final ConfigObject source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		private void mapConfiguration(
			final Builder      builder,
			final ConfigObject source ,
			final String       prefix
		)
		{
			source.entrySet().forEach(e ->
			{
				final String      key   = prefix.concat(e.getKey());
				final ConfigValue value = e.getValue();
				if(value instanceof ConfigObject)
				{
					this.mapConfiguration(
						builder,
						(ConfigObject)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(key, value.unwrapped().toString());
				}
			});
		}
		
	}
	
}
