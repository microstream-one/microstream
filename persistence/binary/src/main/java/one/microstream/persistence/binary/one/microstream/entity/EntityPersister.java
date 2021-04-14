package one.microstream.persistence.binary.one.microstream.entity;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceStoreHandler;

public interface EntityPersister extends PersistenceFunction
{
	public static EntityPersister New(
		final EntityTypeHandlerManager        entityTypeHandlerManager,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		return new Default(
			notNull(entityTypeHandlerManager),
			notNull(handler)
		);
	}
	
	
	public static class Default implements EntityPersister
	{
		private final EntityTypeHandlerManager        entityTypeHandlerManager;
		private final PersistenceStoreHandler<Binary> handler                 ;
		
		Default(
			final EntityTypeHandlerManager        entityTypeHandlerManager,
			final PersistenceStoreHandler<Binary> handler
		)
		{
			super();
			this.entityTypeHandlerManager = entityTypeHandlerManager;
			this.handler                  = handler                 ;
		}

		@Override
		public <T> long apply(final T instance)
		{
			return this.handler.applyEager(
				instance,
				this.entityTypeHandlerManager.ensureInternalEntityTypeHandler(instance)
			);
		}
		
	}
	
}
