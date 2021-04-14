package one.microstream.util.config;

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
