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

import one.microstream.chars.VarString;

/**
 * Assembler to export a configuration into an external format.
 *
 */
@FunctionalInterface
public interface ConfigurationAssembler
{
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public default VarString assemble(final Configuration configuration)
	{
		return this.assemble(VarString.New(), configuration);
	}
	
	/**
	 * Assembles all entries and child-configurations to an external format.
	 * 
	 * @param vs existing target VarString
	 * @param configuration the source
	 * @return a String representation of the external format
	 */
	public VarString assemble(VarString vs, Configuration configuration);
}
