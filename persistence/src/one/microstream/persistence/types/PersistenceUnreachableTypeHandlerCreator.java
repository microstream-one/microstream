package one.microstream.persistence.types;

public interface PersistenceUnreachableTypeHandlerCreator<M>
{
	public <T> PersistenceUnreachableTypeHandler<M, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <M> PersistenceUnreachableTypeHandlerCreator<M> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Default<>();
	}
	
	public final class Default<M> implements PersistenceUnreachableTypeHandlerCreator<M>
	{
		@Override
		public <T> PersistenceUnreachableTypeHandler<M, T> createUnreachableTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
//			XDebug.println("Creating unreachable type handler for " + typeDefinition.toTypeIdentifier());
			return PersistenceUnreachableTypeHandler.New(typeDefinition);
		}
		
	}
	
}
