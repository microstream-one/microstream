package one.microstream.util.config;

import one.microstream.collections.EqConstHashTable;
import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingMap;
import one.microstream.collections.types.XGettingTable;


public class RootConfig extends AbstractConfig
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final EqHashTable<String, SubConfig>   children     = EqHashTable.New();
	private final XGettingTable<String, SubConfig> viewChildren = this.children.view();
	private       EqConstHashTable<String, String> configTable ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public RootConfig(
		final String                      identifier        ,
		final XGettingMap<String, String> customVariables   ,
		final Character                   variableStarter   ,
		final Character                   variableTerminator
	)
	{
		super(identifier, customVariables, variableStarter, variableTerminator);
	}

	public RootConfig(final String identifier, final XGettingMap<String, String> customVariables)
	{
		super(identifier, customVariables);
	}

	public RootConfig(final String identifier)
	{
		super(identifier);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final void register(final SubConfig child)
	{
		this.children.add(child.identifier(), child);
	}

	public final RootConfig updateDefaults(final EqHashTable<String, ConfigFile> configFiles)
	{
		this.updateFiles(configFiles);
		this.configTable = this.compileEntries();

		// update all children (coalesce defaults with local overrides)
		this.children.values().iterate(e ->
			e.updateFromParent()
		);
		return this;
	}

	public final XGettingTable<String, SubConfig> children()
	{
		return this.viewChildren;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final XGettingTable<String, String> table()
	{
		if(this.configTable == null)
		{
			// (15.07.2013 TM)EXCP: proper exception
			throw new RuntimeException("Default config not initialized");
		}
		return this.configTable;
	}

}
