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

/**
 * A utility interface to parse values from external formats to a {@link Configuration#Builder()}.
 *
 * @see Configuration.Builder#load(ConfigurationMapper, Object)
 * @see ConfigurationMapper
 */
@FunctionalInterface
public interface ConfigurationParser
{
	/**
	 * Creates a {@link Configuration#Builder()} and adds all entries contained in the given input.
	 * 
	 * @param input the source to parse the entries from
	 * @return a new {@link Configuration#Builder()}
	 */
	public default Configuration.Builder parseConfiguration(final String input)
	{
		return this.parseConfiguration(
			Configuration.Builder(),
			input
		);
	}
	
	/**
	 * Parses all entries contained in the input to the given {@link Configuration#Builder()}.
	 * 
	 * @param the builder to map the entries to
	 * @param input the source to parse the entries from
	 * @return the given {@link Configuration#Builder()}
	 */
	public Configuration.Builder parseConfiguration(Configuration.Builder builder, String input);
		
}
