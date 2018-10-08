package net.jadoth.persistence.types;

public interface PersistenceDeletedTypeHandlerCreator<M>
{
	public <T> PersistenceDeletedTypeHandler<M, T> createDeletedTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <M> PersistenceDeletedTypeHandlerCreator<M> New()
	{
		return new PersistenceDeletedTypeHandlerCreator.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceDeletedTypeHandlerCreator<M>
	{

		@Override
		public <T> PersistenceDeletedTypeHandler<M, T> createDeletedTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			return PersistenceDeletedTypeHandler.New(typeDefinition);
		}
		
	}
	
}
