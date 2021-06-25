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

import one.microstream.chars.VarString;

/**
 * Assembler for configurations to export to INI (properties) format.
 * 
 * @see Configuration#store(ConfigurationStorer, ConfigurationAssembler)
 */
public interface ConfigurationAssemblerIni extends ConfigurationAssembler
{
	/**
	 * Pseudo-constructor to create a new INI assembler.
	 * 
	 * @return a new INI assembler
	 */
	public static ConfigurationAssemblerIni New()
	{
		return new ConfigurationAssemblerIni.Default();
	}
	
	
	public static class Default implements ConfigurationAssemblerIni
	{
		Default()
		{
			super();
		}
		
		@Override
		public VarString assemble(
			final VarString     vs           ,
			final Configuration configuration
		)
		{
			configuration.coalescedTable().iterate(kv ->
				vs.add(kv.key()).add(" = ").add(kv.value()).lf()
			);
			
			return vs;
		}
		
	}
	
}
