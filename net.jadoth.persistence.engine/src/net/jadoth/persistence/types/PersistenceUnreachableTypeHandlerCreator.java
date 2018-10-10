package net.jadoth.persistence.types;

import net.jadoth.meta.XDebug;

public interface PersistenceUnreachableTypeHandlerCreator<M>
{
	public <T> PersistenceUnreachableTypeHandler<M, T> createUnreachableTypeHandler(
		PersistenceTypeDefinition typeDefinition
	);
	
	
	
	public static <M> PersistenceUnreachableTypeHandlerCreator<M> New()
	{
		return new PersistenceUnreachableTypeHandlerCreator.Implementation<>();
	}
	
	public final class Implementation<M> implements PersistenceUnreachableTypeHandlerCreator<M>
	{
		@Override
		public <T> PersistenceUnreachableTypeHandler<M, T> createUnreachableTypeHandler(
			final PersistenceTypeDefinition typeDefinition
		)
		{
			// (10.10.2018 TM)FIXME: OGS-3: DEBUG
			XDebug.println("Unreachable handler for " + typeDefinition.toTypeIdentifier());
			return PersistenceUnreachableTypeHandler.New(typeDefinition);
		}
		
	}
	
}
