package one.microstream.configuration.types;

import static one.microstream.X.notNull;

@FunctionalInterface
public interface ConfigurationProvider
{
	public default Configuration provideConfiguration()
	{
		return this.provideConfiguration(Configuration.Builder());
	}
	
	public Configuration provideConfiguration(Configuration.Builder builder);
	
	
	public static <T> ConfigurationProvider New(
		final ConfigurationLoader    loader,
		final ConfigurationParser<T> parser,
		final ConfigurationMapper<T> mapper
	)
	{
		return new ConfigurationProvider.Default<>(
			notNull(loader),
			notNull(parser),
			notNull(mapper)
		);
	}
		
	
	public static class Default<T> implements ConfigurationProvider
	{
		private final ConfigurationLoader    loader;
		private final ConfigurationParser<T> parser;
		private final ConfigurationMapper<T> mapper;
		
		Default(
			final ConfigurationLoader    loader,
			final ConfigurationParser<T> parser,
			final ConfigurationMapper<T> mapper
		)
		{
			super();
			this.loader = loader;
			this.parser = parser;
			this.mapper = mapper;
		}
		
		@Override
		public Configuration provideConfiguration(
			final Configuration.Builder builder
		)
		{
			return this.mapper.mapConfiguration(
				builder,
				this.parser.parseConfiguration(
					this.loader.loadConfiguration()
				)
			)
			.buildConfiguration();
		}
		
	}
	
}
