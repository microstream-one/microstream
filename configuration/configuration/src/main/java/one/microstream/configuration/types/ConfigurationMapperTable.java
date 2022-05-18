package one.microstream.configuration.types;

/*-
 * #%L
 * microstream-configuration
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

import static one.microstream.X.notNull;

import java.util.function.Function;

import one.microstream.collections.types.XGettingTable;
import one.microstream.configuration.types.Configuration.Builder;

/**
 * Mapper which maps entries from a {@link XGettingTable} to a {@link Configuration#Builder()}.
 * 
 * @param <V> the value type
 */
public interface ConfigurationMapperTable<V> extends ConfigurationMapper<XGettingTable<String, V>>
{
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param <V> the value type
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New()
	{
		return new ConfigurationMapperTable.Default<>(Object::toString);
	}
	
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param <V> the value type
	 * @param toStringMapper function which converts values from the table to String values
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New(
		final Function<Object, String> toStringMapper
	)
	{
		return new ConfigurationMapperTable.Default<>(
			notNull(toStringMapper)
		);
	}
	
	
	public static class Default<V> implements ConfigurationMapperTable<V>
	{
		private final Function<Object, String> toStringMapper;
		
		Default(
			final Function<Object, String> toStringMapper
		)
		{
			super();
			this.toStringMapper = toStringMapper;
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder                  builder,
			final XGettingTable<String, V> source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		@SuppressWarnings("unchecked")
		private void mapConfiguration(
			final Builder                  builder,
			final XGettingTable<String, V> source ,
			final String                   prefix
		)
		{
			source.iterate(kv ->
			{
				final String key   = prefix.concat(kv.key());
				final Object value = kv.value();
				if(value instanceof XGettingTable)
				{
					this.mapConfiguration(
						builder,
						(XGettingTable<String, V>)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(
						key,
						this.toStringMapper.apply(value)
					);
				}
			});
		}
		
	}
	
}
