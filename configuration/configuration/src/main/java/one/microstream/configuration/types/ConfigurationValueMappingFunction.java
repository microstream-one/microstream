package one.microstream.configuration.types;

/*-
 * #%L
 * microstream-configuration
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

import one.microstream.configuration.exceptions.ConfigurationExceptionValueMappingFailed;

/**
 * Function which maps String values from {@link Configuration}s to a certain type.
 *
 * @param <T> the target type
 */
@FunctionalInterface
public interface ConfigurationValueMappingFunction<T>
{
	/**
	 * Maps the given value of a {@link Configuration} to the target type.
	 * 
	 * @param config source configuration
	 * @param key the assigned key
	 * @param value the value to map
	 * @return the mapped value
	 * @throws ConfigurationExceptionValueMappingFailed if the mapping failed
	 */
	public T map(Configuration config, String key, String value);
}
