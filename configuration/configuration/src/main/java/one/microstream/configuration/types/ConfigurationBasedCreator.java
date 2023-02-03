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

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ConfigurationBasedCreator<T>
{
	public Class<T> resultType();
	
	public T create(Configuration configuration);
	
	
	@SuppressWarnings("unchecked") // type-safety ensured by logic
	public static <T> List<ConfigurationBasedCreator<T>> registeredCreators(
		final Class<T> resultType
	)
	{
		return StreamSupport.stream(
			ServiceLoader.load(ConfigurationBasedCreator.class).spliterator(),
			false
		)
		.filter(creator -> resultType.isAssignableFrom(creator.resultType()))
		.map(c -> (ConfigurationBasedCreator<T>)c)
		.collect(Collectors.toList());
	}
	
	
	public static abstract class Abstract<T> implements ConfigurationBasedCreator<T>
	{
		private final Class<T> resultType;

		protected Abstract(
			final Class<T> resultType
		)
		{
			super();
			this.resultType = notNull(resultType);
		}
		
		@Override
		public Class<T> resultType()
		{
			return this.resultType;
		}
		
	}
	
}
