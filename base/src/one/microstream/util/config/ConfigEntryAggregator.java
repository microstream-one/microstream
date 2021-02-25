package one.microstream.util.config;

import static one.microstream.X.notNull;

import one.microstream.collections.EqHashTable;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.util.Substituter;
import one.microstream.util.xcsv.XCsvRowCollector;


public final class ConfigEntryAggregator implements XCsvRowCollector
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final ConfigEntryAggregator New()
	{
		return New(Substituter.<String>New());
	}

	public static final ConfigEntryAggregator New(final Substituter<String> stringCache)
	{
		return new ConfigEntryAggregator(notNull(stringCache));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Substituter<String>             stringCache;
	private final EqHashTable<String, ConfigFile> configs     = EqHashTable.New();
	private final String[]                        entry       = new String[2];

	private EqHashTable<String, String>           config    ;
	private String                                name      ;
	private int                                   entryIndex;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private ConfigEntryAggregator(final Substituter<String> stringCache)
	{
		super();
		this.stringCache = stringCache;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public ConfigEntryAggregator setNewConfig(final String name)
	{
		// register old file
		this.completeConfig();

		// can never be the same table twice, as filenames are unique inside a folder
		this.config = EqHashTable.New();
		this.name = name;

		return this;
	}

	public void completeConfig()
	{
		if(this.config != null)
		{
			this.configs.add(this.name, new ConfigFile(this.name, this.config));
			this.name = null;
			this.config = null;
		}
	}

	private void clear()
	{
		this.entry[0] = null;
		this.entry[1] = null;
		this.entryIndex = 0;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void beginTable(
		final String                   tableName  ,
		final XGettingSequence<String> columnNames,
		final XGettingList<String>     columnTypes
	)
	{
		// no-op, although this method could be used for column validation (exactely 2 columns)
	}

	@Override
	public final void accept(final char[] data, final int offset, final int length)
	{
		this.entry[this.entryIndex++] = data == null
			? null
			: this.stringCache.substitute(new String(data, offset, length))
		;
	}

	@Override
	public final void completeRow()
	{
		this.config.add(this.entry[0], this.entry[1]);
		this.clear();
	}

	@Override
	public final void completeTable()
	{
		// cleanup at end
		this.clear();
	}

	public final EqHashTable<String, ConfigFile> yield()
	{
		// close last read file
		this.completeConfig();
		return this.configs;
	}

}
