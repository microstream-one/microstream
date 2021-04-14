package one.microstream.configuration.types;

import static one.microstream.X.notNull;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ConfigurationBasedCreator<T>
{
	public Class<?> resultType();
	
	public T create(Configuration configuration);
	
	
	public static <T> List<ConfigurationBasedCreator<T>> registeredCreators(
		final Class<T> resultType
	)
	{
		return StreamSupport.stream(
			ServiceLoader.load(ConfigurationBasedCreator.class).spliterator(),
			false
		)
		.filter(creator -> resultType.isAssignableFrom(creator.resultType()))
		.collect(Collectors.toList())
		;
	}
	
	
	public static abstract class Abstract<T> implements ConfigurationBasedCreator<T>
	{
		private final Class<T> resultType;

		protected Abstract(
			final Class<T> resultType
		)
		{
			super();
			this.resultType = notNull(resultType);
		}
		
		@Override
		public Class<?> resultType()
		{
			return this.resultType;
		}
				
	}
	
}
