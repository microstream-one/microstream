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
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingTable;
import one.microstream.collections.types.XImmutableTable;


public final class SubConfig extends AbstractConfig
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final RootConfig                parent        ;
	private XImmutableTable<String, String> overrideTable ;
	private XImmutableTable<String, String> coalescedTable;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	SubConfig(
		final RootConfig                  parent            ,
		final String                      identifier        ,
		final XGettingMap<String, String> customVariables   ,
		final Character                   variableStarter   ,
		final Character                   variableTerminator
	)
	{
		super(identifier, customVariables, variableStarter, variableTerminator);
		this.parent = notNull(parent);
	}

	SubConfig(final RootConfig parent, final String identifier, final XGettingMap<String, String> customVariables)
	{
		super(identifier, customVariables);
		this.parent = notNull(parent);
	}

	SubConfig(final RootConfig parent, final String identifier)
	{
		super(identifier);
		this.parent = notNull(parent);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final void updateFromParent()
	{
		this.updateOverrideTable(this.overrideTable);
	}

	public final SubConfig updateOverrides(final EqHashTable<String, ConfigFile> configFiles)
	{
		// override config gets cleared before updates as overrides are optional, so only the new ones count.
		this.configFiles.clear();
		this.updateFiles(configFiles);
		return this.updateOverrideTable(this.compileEntries());
	}

	final SubConfig updateOverrideTable(final XGettingTable<String, String> konditionsjahrTable)
	{
		final EqHashTable<String, String> coalesced = EqHashTable.New();
		coalesced
		.putAll(this.parent.table()) // put or add makes no difference here
		.putAll(konditionsjahrTable) // put is essential here to replace the values
		;
		this.coalescedTable = coalesced.immure();
		this.overrideTable  = konditionsjahrTable.immure();
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final XGettingTable<String, String> table()
	{
		if(this.coalescedTable == null)
		{
			this.updateFromParent();
		}
		return this.coalescedTable;
	}

}
