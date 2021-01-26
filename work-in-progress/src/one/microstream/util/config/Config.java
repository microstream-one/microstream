package one.microstream.util.config;

import one.microstream.collections.types.XGettingTable;

public interface Config
{
	public XGettingTable<String, String> table();

	public default String getValue(final String key)
	{
		return this.getRawValue(key);
	}

	public default String getRawValue(final String key)
	{
		return this.table().get(key);
	}

	public String identifier();

	public <T> T get(ConfigEntry<T> entry);
}
