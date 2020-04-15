package one.microstream.entity;

import static one.microstream.X.notNull;

import one.microstream.collections.MiniMap;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeHandler;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;
import one.microstream.reference.Referencing;

@FunctionalInterface
public interface EntityTypeHandlerManager
{
	public <T> PersistenceTypeHandler<Binary, T> ensureInternalEntityTypeHandler(
		final T instance
	);
	
	
	public static EntityTypeHandlerManager New(
		final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager
	)
	{
		return new Default(
			notNull(typeHandlerManager)
		);
	}
	
	
	public static class Default implements EntityTypeHandlerManager
	{
		private final Referencing<PersistenceTypeHandlerManager<Binary>>   typeHandlerManager                 ;
		private final MiniMap<Class<?>, PersistenceTypeHandler<Binary, ?>> internalHandlers  = new MiniMap<>();
	
		Default(final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager)
		{
			super();
			this.typeHandlerManager = typeHandlerManager;
		}

		@SuppressWarnings({"unchecked", "rawtypes"})
		public <T> PersistenceTypeHandler<Binary, T> ensureInternalEntityTypeHandler(
			final T instance
		)
		{
			final Class<T>                    type   = (Class<T>)instance.getClass();
			PersistenceTypeHandler<Binary, T> handler;
			
			synchronized(this.internalHandlers)
			{
				if((handler = (PersistenceTypeHandler<Binary, T>)this.internalHandlers.get(type)) == null)
				{
					handler = (PersistenceTypeHandler<Binary, T>)this.typeHandlerManager.get()
						.ensureTypeHandler(type);
					if(handler instanceof BinaryHandlerEntityLoading)
					{
						this.internalHandlers.put(
							type, 
							handler = ((BinaryHandlerEntityLoading)handler).createStoringEntityHandler()
						);
					}
				}
			}
			
			return (PersistenceTypeHandler<Binary, T>)handler;		
		}
	
	}
	
}
