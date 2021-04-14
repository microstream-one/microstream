package one.microstream.persistence.types;

public interface PersistenceUnreachableTypeHandlerCreator<D>
{
	public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <D> PersistenceUnreachableTypeHandlerCreator<D> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Default<>();
	}
	
	public final class Default<D> implements PersistenceUnreachableTypeHandlerCreator<D>
	{
		@Override
		public <T> PersistenceUnreachableTypeHandler<D, T> createUnreachableTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
//			XDebug.println("Creating unreachable type handler for " + typeDefinition.toTypeIdentifier());
			return PersistenceUnreachableTypeHandler.New(typeDefinition);
		}
		
	}
	
}
