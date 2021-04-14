package one.microstream.util.config;

final class ConfigEntryInteger extends ConfigEntry.Abstract<Integer>
{
	ConfigEntryInteger(final String key)
	{
		super(key);
	}

	@Override
	public final Integer parse(final String value)
	{
		return Integer.valueOf(value);
	}

}
