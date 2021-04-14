package one.microstream.cache.hibernate.types;

import one.microstream.chars.XChars;

public enum MissingCacheStrategy
{
	FAIL("fail"),
	CREATE_WARN("create-warn"),
	CREATE("create");

	
	public static MissingCacheStrategy Default()
	{
		return MissingCacheStrategy.CREATE_WARN;
	}
	
	
	private final String externalRepresentation;

	MissingCacheStrategy(
		final String externalRepresentation
	) 
	{
		this.externalRepresentation = externalRepresentation;
	}
	
	public String getExternalRepresentation()
	{
		return this.externalRepresentation;
	}

	public static MissingCacheStrategy ofSetting(Object value) 
	{
		if(value instanceof MissingCacheStrategy) 
		{
			return (MissingCacheStrategy)value;
		}

		final String externalRepresentation = value == null 
			? null 
			: value.toString().trim();

		if(XChars.isEmpty(externalRepresentation)) 
		{
			return MissingCacheStrategy.Default();
		}

		for(MissingCacheStrategy strategy : values()) 
		{
			if(strategy.externalRepresentation.equals(externalRepresentation))
			{
				return strategy;
			}
		}

		throw new IllegalArgumentException("Unrecognized missing cache strategy value : `" + value + '`');
	}
}
