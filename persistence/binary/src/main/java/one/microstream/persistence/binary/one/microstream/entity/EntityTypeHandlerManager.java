package one.microstream.persistence.binary.one.microstream.entity;

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
		T instance
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
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final Referencing<PersistenceTypeHandlerManager<Binary>>   typeHandlerManager                 ;
		private final MiniMap<Class<?>, PersistenceTypeHandler<Binary, ?>> internalHandlers  = new MiniMap<>();
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
	
		Default(final Referencing<PersistenceTypeHandlerManager<Binary>> typeHandlerManager)
		{
			super();
			this.typeHandlerManager = typeHandlerManager;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		@SuppressWarnings({"unchecked"}) // generics safety guaranteed by registration logic
		public <T> PersistenceTypeHandler<Binary, T> ensureInternalEntityTypeHandler(
			final T instance
		)
		{
			final Class<?> type = instance.getClass();
			
			PersistenceTypeHandler<Binary, ?> handler;
			synchronized(this.internalHandlers)
			{
				if((handler = this.internalHandlers.get(type)) == null)
				{
					handler = this.typeHandlerManager.get().ensureTypeHandler(type);
					if(handler instanceof BinaryHandlerEntityLoading)
					{
						handler = ((BinaryHandlerEntityLoading<?>)handler).createStoringEntityHandler();
						this.internalHandlers.put(type, handler);
					}
				}
			}
			
			return (PersistenceTypeHandler<Binary, T>)handler;
		}
	
	}
	
}
