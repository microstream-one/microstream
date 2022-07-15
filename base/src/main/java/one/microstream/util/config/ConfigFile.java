package one.microstream.util.config;

/*-
 * #%L
 * microstream-base
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

import one.microstream.collections.EqHashTable;

public final class ConfigFile
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final String                      name ;
	final EqHashTable<String, String> table;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ConfigFile(final String name, final EqHashTable<String, String> table)
	{
		super();
		this.name  = notNull(name );
		this.table = notNull(table);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public final String name()
	{
		return this.name;
	}

	public final EqHashTable<String, String> table()
	{
		return this.table;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final int hashCode()
	{
		return this.name.hashCode();
	}

	@Override
	public final boolean equals(final Object object)
	{
		if(this == object)
		{
			return true;
		}
		return object instanceof ConfigFile && this.name.equals(((ConfigFile)object).name());
	}

}
