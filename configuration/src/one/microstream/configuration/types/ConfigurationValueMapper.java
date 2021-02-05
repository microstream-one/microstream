package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.time.Duration;

import one.microstream.configuration.exceptions.ConfigurationExceptionValueMappingFailed;

public interface ConfigurationValueMapper<T> extends ConfigurationValueMappingFunction<T>
{
	public Class<T> type();
	
	
	public static ConfigurationValueMapper<Boolean> Boolean()
	{
		return New(
			Boolean.class,
			(config, key, value) -> value == null
				? null
				: Boolean.parseBoolean(value)
		);
	}
	
	public static ConfigurationValueMapper<Byte> Byte()
	{
		return New(Byte.class, (config, key, value) ->
		{
			try
			{
				return Byte.parseByte(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Short> Short()
	{
		return New(Short.class, (config, key, value) ->
		{
			try
			{
				return Short.parseShort(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Integer> Integer()
	{
		return New(Integer.class, (config, key, value) ->
		{
			try
			{
				return Integer.parseInt(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Long> Long()
	{
		return New(Long.class, (config, key, value) ->
		{
			try
			{
				return Long.parseLong(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Float> Float()
	{
		return New(Float.class, (config, key, value) ->
		{
			try
			{
				return Float.parseFloat(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Double> Double()
	{
		return New(Double.class, (config, key, value) ->
		{
			try
			{
				return Double.parseDouble(value);
			}
			catch(final NumberFormatException e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<ByteSize> ByteSize()
	{
		final ByteSizeParser parser = ByteSizeParser.New();
		return New(ByteSize.class, (config, key,value) ->
		{
			try
			{
				return parser.parse(value);
			}
			catch(final Exception e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<ByteSize> ByteSize(
		final ByteUnit defaultUnit
	)
	{
		final ByteSizeParser parser = ByteSizeParser.New(defaultUnit);
		return New(ByteSize.class, (config, key,value) ->
		{
			try
			{
				return parser.parse(value);
			}
			catch(final Exception e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Duration> Duration()
	{
		final DurationParser parser = DurationParser.New();
		return New(Duration.class, (config, key,value) ->
		{
			try
			{
				return parser.parse(value);
			}
			catch(final Exception e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	public static ConfigurationValueMapper<Duration> Duration(
		final DurationUnit defaultUnit
	)
	{
		final DurationParser parser = DurationParser.New(defaultUnit);
		return New(Duration.class, (config, key,value) ->
		{
			try
			{
				return parser.parse(value);
			}
			catch(final Exception e)
			{
				throw new ConfigurationExceptionValueMappingFailed(config, e, key, value);
			}
		});
	}
	
	
	public static <T> ConfigurationValueMapper<T> New(
		final Class<T>                             type           ,
		final ConfigurationValueMappingFunction<T> mappingFunction
	)
	{
		return new ConfigurationValueMapper.Default<>(
			notNull(type)           ,
			notNull(mappingFunction)
		);
	}
	
	
	public static class Default<T> implements ConfigurationValueMapper<T>
	{
		private final Class<T>                             type           ;
		private final ConfigurationValueMappingFunction<T> mappingFunction;

		Default(
			final Class<T>                             type           ,
			final ConfigurationValueMappingFunction<T> mappingFunction
		)
		{
			super();
			this.type            = type           ;
			this.mappingFunction = mappingFunction;
		}
		
		@Override
		public Class<T> type()
		{
			return this.type;
		}
		
		@Override
		public T map(
			final Configuration config,
			final String        key   ,
			final String        value
		)
		{
			return this.mappingFunction.map(config, key, value);
		}
				
	}
	
}
