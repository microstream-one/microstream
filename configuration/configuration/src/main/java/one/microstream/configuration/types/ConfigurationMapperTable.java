package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.function.Function;

import one.microstream.collections.types.XGettingTable;
import one.microstream.configuration.types.Configuration.Builder;

/**
 * Mapper which maps entries from a {@link XGettingTable} to a {@link Configuration#Builder()}.
 *
 */
public interface ConfigurationMapperTable<V> extends ConfigurationMapper<XGettingTable<String, V>>
{
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New()
	{
		return new ConfigurationMapperTable.Default<>(Object::toString);
	}
	
	/**
	 * Pseudo-constructor to create a new mapper.
	 * 
	 * @param toStringMapper function which converts values from the table to String values
	 * @return a new mapper
	 */
	public static <V> ConfigurationMapperTable<V> New(
		final Function<Object, String> toStringMapper
	)
	{
		return new ConfigurationMapperTable.Default<>(
			notNull(toStringMapper)
		);
	}
	
	
	public static class Default<V> implements ConfigurationMapperTable<V>
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
			final Builder                  builder,
			final XGettingTable<String, V> source
		)
		{
			this.mapConfiguration(builder, source, "");
			
			return builder;
		}
		
		@SuppressWarnings("unchecked")
		private void mapConfiguration(
			final Builder                  builder,
			final XGettingTable<String, V> source ,
			final String                   prefix
		)
		{
			source.iterate(kv ->
			{
				final String key   = prefix.concat(kv.key());
				final Object value = kv.value();
				if(value instanceof XGettingTable)
				{
					this.mapConfiguration(
						builder,
						(XGettingTable<String, V>)value,
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
