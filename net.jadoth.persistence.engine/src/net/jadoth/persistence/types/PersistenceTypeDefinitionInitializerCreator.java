package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeDefinitionInitializerCreator<M>
{
	public <T> PersistenceTypeDefinitionInitializer<T> createTypeDefinitionInitializer(Class<T> type);
	
	
	
	public static <M> PersistenceTypeDefinitionInitializerCreator.Implementation<M> New(
		final PersistenceTypeHandlerEnsurer<M> typeHandlerEnsurer,
		final PersistenceTypeHandlerManager<M> typeHandlerManager
	)
	{
		return new PersistenceTypeDefinitionInitializerCreator.Implementation<>(
			notNull(typeHandlerEnsurer),
			notNull(typeHandlerManager)
		);
	}
	
	public final class Implementation<M> implements PersistenceTypeDefinitionInitializerCreator<M>
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
		public <T> PersistenceTypeDefinitionInitializer<T> createTypeDefinitionInitializer(final Class<T> type)
		{
			final PersistenceTypeHandler<M, T> typeHandler = this.typeHandlerEnsurer.ensureTypeHandler(type);
			
			return PersistenceTypeDefinitionInitializer.New(this.typeHandlerManager, typeHandler);
		}
		
	}
	
}
