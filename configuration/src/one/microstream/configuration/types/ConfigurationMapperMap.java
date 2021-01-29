package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.Map;
import java.util.function.Function;

import one.microstream.configuration.types.Configuration.Builder;

public interface ConfigurationMapperMap<V> extends ConfigurationMapper<Map<String, V>>
{
	public static <V> ConfigurationMapperMap<V> New()
	{
		return new ConfigurationMapperMap.Default<>(Object::toString);
	}
	
	public static <V> ConfigurationMapperMap<V> New(
		final Function<Object, String> toStringMapper
	)
	{
		return new ConfigurationMapperMap.Default<>(
			notNull(toStringMapper)
		);
	}
	
	
	public static class Default<V> implements ConfigurationMapperMap<V>
	{
		private final Function<Object, String> toStringMapper;
		
		Default(
			final Function<Object, String> toStringMapper
		)
		{
			super();
			this.toStringMapper = toStringMapper;
		}
		
		@Override
		public Builder mapConfiguration(
			final Builder        builder,
			final Map<String, V> source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		@SuppressWarnings("unchecked")
		private void mapConfiguration(
			final Builder        builder,
			final Map<String, V> source ,
			final String         prefix
		)
		{
			source.entrySet().forEach(e ->
			{
				final String key   = prefix.concat(e.getKey());
				final Object value = e.getValue();
				if(value instanceof Map)
				{
					this.mapConfiguration(
						builder,
						(Map<String, V>)value,
						key + Configuration.KEY_SEPARATOR
					);
				}
				else if(value != null)
				{
					builder.set(
						key,
						this.toStringMapper.apply(value)
					);
				}
			});
		}
		
	}
	
}
