package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeDefinitionInitializerProvider<M>
{
	public <T> PersistenceTypeDefinitionInitializer<T> lookupInitializer(Class<T> type);
	
	
	
	public static <M> PersistenceTypeDefinitionInitializerProvider.Implementation<M> New(
		final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer,
		final PersistenceTypeHandlerManager<M> typeHandlerManager
	)
	{
		return new PersistenceTypeDefinitionInitializerProvider.Implementation<>(
			notNull(typeHandlerEnsurer),
			notNull(typeHandlerManager)
		);
	}
	
	public final class Implementation<M> implements PersistenceTypeDefinitionInitializerProvider<M>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer;
		final PersistenceTypeHandlerManager<M> typeHandlerManager;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer,
			final PersistenceTypeHandlerManager<M> typeHandlerManager
		)
		{
			super();
			this.typeHandlerEnsurer = typeHandlerEnsurer;
			this.typeHandlerManager = typeHandlerManager;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public <T> PersistenceTypeDefinitionInitializer<T> lookupInitializer(final Class<T> type)
		{
			final PersistenceTypeHandler<M, T> typeHandler = this.typeHandlerEnsurer.ensureTypeHandler(type);
			
			return PersistenceTypeDefinitionInitializer.New(this.typeHandlerManager, typeHandler);
		}
		
	}
	
}
