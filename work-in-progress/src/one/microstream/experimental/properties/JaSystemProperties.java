package one.microstream.experimental.properties;

@Deprecated // (28.08.2013 TM)XXX: obsolete / experimental System Properties framework, delete
public class JaSystemProperties
{
	public static SystemProperty systemProperty(final String name)
	{
		return new SystemProperty.Named(null, name);
	}

	public static SystemProperty systemProperty(final SystemProperty parent, final String name)
	{
		return new SystemProperty.Named(parent, name);
	}

	public static SystemProperty systemPropertyDefaulted(final SystemProperty parent, final String name)
	{
		return new SystemProperty.Defaulted(parent, name, null);
	}

	public static SystemProperty systemPropertyDefaulted(final SystemProperty parent, final String name, final String defaultValue)
	{
		return new SystemProperty.Defaulted(parent, name, defaultValue);
	}

	public static SystemProperty systemPropertyDefaulted(final String name, final String defaultValue)
	{
		return new SystemProperty.Defaulted(null, name, defaultValue);
	}

	public static SystemProperty systemPropertyFallThrough(final SystemProperty parent, final String name)
	{
		return new SystemProperty.FallThrough(parent, name, null);
	}

	public static SystemProperty systemPropertyFallThrough(final SystemProperty parent, final String name, final String defaultValue)
	{
		return new SystemProperty.FallThrough(parent, name, defaultValue);
	}

	public static GlobalProperty globalPropertyComposite(final GlobalProperty parent, final GlobalProperty extension)
	{
		return new GlobalProperty.Composite(parent, extension, null);
	}

	public static GlobalProperty globalPropertyComposite(final SystemProperty... components)
	{
		if(components.length == 1)
		{
			return new GlobalProperty.Composite(components[0], null, null);
		}
		GlobalProperty current = components[0];
		for(int i = 1; i < components.length; i++)
		{
			current = new GlobalProperty.Composite(current, components[i], null);
		}
		return current;
	}

	public static GlobalProperty globalPropertyComposite(
		final GlobalProperty parent,
		final GlobalProperty extension,
		final String defaultExtensionValue
	)
	{
		return new GlobalProperty.Composite(parent, extension, defaultExtensionValue);
	}
}
