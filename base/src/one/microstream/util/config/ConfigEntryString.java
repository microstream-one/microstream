package one.microstream.util.config;

final class ConfigEntryString extends ConfigEntry.AbstractImplementation<String>
{
	ConfigEntryString(final String key)
	{
		super(key);
	}

	@Override
	public final String parse(final String value)
	{
		return value;
	}

}
