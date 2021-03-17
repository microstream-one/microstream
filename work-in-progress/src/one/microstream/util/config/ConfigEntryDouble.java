package one.microstream.util.config;

final class ConfigEntryDouble extends ConfigEntry.Abstract<Double>
{
	ConfigEntryDouble(final String key)
	{
		super(key);
	}

	@Override
	public final Double parse(final String value)
	{
		return Double.valueOf(value);
	}

}
