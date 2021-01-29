package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import one.microstream.configuration.types.Configuration.Builder;

@FunctionalInterface
public interface ConfigurationProvider
{
	public default Configuration provideConfiguration()
	{
		return this.provideConfiguration(Configuration.Builder());
	}
	
	public Configuration provideConfiguration(Configuration.Builder builder);
	
	
	public static <T> ConfigurationProvider New(
		final ConfigurationLoader loader,
		final ConfigurationParser parser
	)
	{
		return new ConfigurationProvider.Default(
			notNull(loader),
			notNull(parser)
		);
	}
	
	
	public static class Default implements ConfigurationProvider
	{
		private final ConfigurationLoader loader;
		private final ConfigurationParser parser;
		
		Default(
			final ConfigurationLoader loader,
			final ConfigurationParser parser
		)
		{
			super();
			this.loader = loader;
			this.parser = parser;
		}
		
		@Override
		public Configuration provideConfiguration(
			final Builder builder
		)
		{
			return this.parser.parseConfiguration(
				builder,
				this.loader.loadConfiguration()
			)
			.buildConfiguration();
		}
		
	}
	
}
