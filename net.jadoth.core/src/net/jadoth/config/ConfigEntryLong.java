package net.jadoth.config;

final class ConfigEntryLong extends ConfigEntry.AbstractImplementation<Long>
{
	ConfigEntryLong(final String key)
	{
		super(key);
	}

	@Override
	public final Long parse(final String value)
	{
		return Long.valueOf(value);
	}

}
