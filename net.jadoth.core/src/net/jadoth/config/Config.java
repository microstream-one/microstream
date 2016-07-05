package net.jadoth.config;

import net.jadoth.collections.types.XGettingTable;

public interface Config
{
	public XGettingTable<String, String> table();

	public String get(String key);

	public String identifier();

	public <T> T get(ConfigEntry<T> entry);
}
