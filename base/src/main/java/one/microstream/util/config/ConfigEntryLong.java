package one.microstream.util.config;

final class ConfigEntryLong extends ConfigEntry.Abstract<Long>
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
