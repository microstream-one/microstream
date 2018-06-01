package net.jadoth.persistence.types;

import net.jadoth.persistence.internal.PersistenceDeletedTypeHandler;

public interface PersistenceDeletedTypeHandlerCreator<M>
{
	public <T> PersistenceTypeHandler<M, T> createDeletedTypeHandler(
		PersistenceTypeDefinition<T> typeDefinition
	);
	
	
	
	public static <M> PersistenceDeletedTypeHandlerCreator<M> New()
	{
		return new PersistenceDeletedTypeHandlerCreator.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceDeletedTypeHandlerCreator<M>
	{

		@Override
		public <T> PersistenceTypeHandler<M, T> createDeletedTypeHandler(
			final PersistenceTypeDefinition<T> typeDefinition
		)
		{
			return PersistenceDeletedTypeHandler.New(typeDefinition);
		}
		
	}
	
}
