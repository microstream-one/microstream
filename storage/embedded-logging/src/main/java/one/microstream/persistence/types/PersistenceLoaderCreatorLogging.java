package one.microstream.persistence.types;

import static one.microstream.X.notNull;

public interface PersistenceLoaderCreatorLogging<D>
	extends PersistenceLoader.Creator<D>, PersistenceLoggingWrapper<PersistenceLoader.Creator<D>>
{
	public static <D> PersistenceLoaderCreatorLogging<D> New(
		final PersistenceLoader.Creator<D> wrapped
	)
	{
		return new Default<>(notNull(wrapped));
	}
	
	public static class Default<D>
	extends PersistenceLoggingWrapper.Abstract<PersistenceLoader.Creator<D>>
	implements PersistenceLoaderCreatorLogging<D>
	{
		protected Default(
			final PersistenceLoader.Creator<D> wrapped
		)
		{
			super(wrapped);
		}

		@Override
		public PersistenceLoader createLoader(final PersistenceTypeHandlerLookup<D> typeLookup,
			final PersistenceObjectRegistry registry, final Persister persister, final PersistenceSourceSupplier<D> source)
		{
			this.logger().persistenceLoaderCreator_beforeCreateLoader(typeLookup, registry, persister, source);
			
			final PersistenceLoaderLogging loader = PersistenceLoaderLogging.New(
				this.wrapped().createLoader(
					typeLookup,
					registry,
					persister,
					source
				)
			);
			
			this.logger().persistenceLoaderCreator_afterCreateLoader(loader);
			
			return loader;
		}
	}
}
