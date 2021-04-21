package one.microstream.persistence.types;

import static one.microstream.X.notNull;

import java.util.function.Consumer;

import one.microstream.persistence.exceptions.PersistenceExceptionConsistency;

public interface PersistenceTypeHandlerRegistryLogging<D>
	extends PersistenceTypeHandlerRegistry<D>, PersistenceLoggingWrapper<PersistenceTypeHandlerRegistry<D>>
{
	public static <D> PersistenceTypeHandlerRegistryLogging<D> New(
		final PersistenceTypeHandlerRegistry<D> wrapped
	)
	{
		return new Default<>(notNull(wrapped));
	}


	public static class Default<D>
		extends PersistenceLoggingWrapper.Abstract<PersistenceTypeHandlerRegistry<D>>
		implements PersistenceTypeHandlerRegistryLogging<D>
	{
		protected Default(
			final PersistenceTypeHandlerRegistry<D> wrapped
		)
		{
			super(wrapped);
		}

		<T> PersistenceTypeHandler<D, ? super T> ensureLoggingTypeHandler(
			final PersistenceTypeHandler<D, ? super T> handler
		)
		{
			return handler instanceof PersistenceTypeHandlerLogging
				? handler
				: PersistenceTypeHandlerLogging.New(handler)
			;
		}

		@Override
		public <T> boolean registerTypeHandler(final Class<T> type, final PersistenceTypeHandler<D, ? super T> typeHandler)
		{
			return this.wrapped().registerTypeHandler(
				type,
				this.ensureLoggingTypeHandler(typeHandler)
			);
		}

		@Override
		public <T> boolean registerTypeHandler(final PersistenceTypeHandler<D, T> typeHandler)
		{
			this.logger().persistenceTypeHandlerRegistry_beforeRegisterTypeHandler(typeHandler);
			
			return this.wrapped().registerTypeHandler(
				this.ensureLoggingTypeHandler(typeHandler)
			);
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateTypeHandlers(final C iterator)
		{
			return this.wrapped().iterateTypeHandlers(iterator);
		}

		@Override
		public long lookupTypeId(final Class<?> type)
		{
			return this.wrapped().lookupTypeId(type);
		}

		@Override
		public <C extends Consumer<? super PersistenceLegacyTypeHandler<D, ?>>> C iterateLegacyTypeHandlers(final C iterator)
		{
			return this.wrapped().iterateLegacyTypeHandlers(iterator);
		}

		@Override
		public <T> Class<T> lookupType(final long typeId)
		{
			return this.wrapped().lookupType(typeId);
		}

		@Override
		public boolean validateTypeMapping(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.wrapped().validateTypeMapping(typeId, type);
		}

		@Override
		public <C extends Consumer<? super PersistenceTypeHandler<D, ?>>> C iterateAllTypeHandlers(final C iterator)
		{
			return this.wrapped().iterateAllTypeHandlers(iterator);
		}

		@Override
		public boolean validateTypeMappings(final Iterable<? extends PersistenceTypeLink> mappings)
			throws PersistenceExceptionConsistency
		{
			return this.wrapped().validateTypeMappings(mappings);
		}

		@Override
		public boolean registerType(final long typeId, final Class<?> type) throws PersistenceExceptionConsistency
		{
			return this.wrapped().registerType(typeId, type);
		}

		@Override
		public boolean registerTypes(final Iterable<? extends PersistenceTypeLink> types)
			throws PersistenceExceptionConsistency
		{
			return this.wrapped().registerTypes(types);
		}

		@Override
		public boolean registerLegacyTypeHandler(final PersistenceLegacyTypeHandler<D, ?> legacyTypeHandler)
		{
			return this.wrapped().registerLegacyTypeHandler(legacyTypeHandler);
		}

		@Override
		public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(final T instance)
		{
			return this.wrapped().lookupTypeHandler(instance);
		}

		@Override
		public <T> PersistenceTypeHandler<D, ? super T> lookupTypeHandler(final Class<T> type)
		{
			return this.wrapped().lookupTypeHandler(type);
		}

		@Override
		public PersistenceTypeHandler<D, ?> lookupTypeHandler(final long typeId)
		{
			return this.wrapped().lookupTypeHandler(typeId);
		}

		@Override
		public <T> long registerTypeHandlers(final Iterable<? extends PersistenceTypeHandler<D, T>> typeHandlers)
		{
			this.logger().persistenceTypeHandlerRegistry_beforeRegisterTypeHandlers(typeHandlers);
			
			final long handlerCount = this.wrapped().registerTypeHandlers(typeHandlers);
			
			this.logger().persistenceTypeHandlerRegistry_afterRegisterTypeHandlers(handlerCount);
			
			return handlerCount;
		}

	}

}
