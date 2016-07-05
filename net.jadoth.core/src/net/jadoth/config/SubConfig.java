package net.jadoth.config;

import static net.jadoth.Jadoth.notNull;
import net.jadoth.collections.EqHashTable;
import net.jadoth.collections.types.XGettingTable;
import net.jadoth.collections.types.XImmutableTable;


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

	SubConfig(final RootConfig parent, final String identifier)
	{
		super(identifier);
		this.parent     = notNull(parent);
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
	public final String get(final String key)
	{
		return this.table().get(key);
	}

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
