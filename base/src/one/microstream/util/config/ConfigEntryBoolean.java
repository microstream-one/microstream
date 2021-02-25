package one.microstream.util.config;

final class ConfigEntryBoolean extends ConfigEntry.Abstract<Boolean>
{
	ConfigEntryBoolean(final String key)
	{
		super(key);
	}

	@Override
	public final Boolean parse(final String value)
	{
		if("1".equals(value))
		{
			return Boolean.TRUE;
		}
		if("0".equals(value))
		{
			return Boolean.FALSE;
		}
		return Boolean.valueOf(value);
	}

}
