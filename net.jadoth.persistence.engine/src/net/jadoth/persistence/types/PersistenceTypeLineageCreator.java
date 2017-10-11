package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeLineageCreator
{
	public <T> PersistenceTypeLineage<T> createTypeLineage(String typeName);
	
	public <T> PersistenceTypeLineage<T> createTypeLineage(Class<T> type);
	
	
	
	public static PersistenceTypeLineageCreator.Implementation New(
		final PersistenceTypeDefinitionCreator typeDefinitionCreator
	)
	{
		return new PersistenceTypeLineageCreator.Implementation(
			notNull(typeDefinitionCreator)
		);
	}
	
	public final class Implementation implements PersistenceTypeLineageCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinitionCreator typeDefinitionCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final PersistenceTypeDefinitionCreator typeDefinitionCreator)
		{
			super();
			this.typeDefinitionCreator = typeDefinitionCreator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		private <T> PersistenceTypeLineage<T> internalBuildTypeLineage(final String typeName, final Class<T> type)
		{
			return PersistenceTypeLineage.New(typeName, type, this.typeDefinitionCreator);
		}
		
		@Override
		public <T> PersistenceTypeLineage<T> createTypeLineage(final String typeName)
		{
			final Class<T> type = Persistence.resolveTypeOptional(typeName); // might be null
			return internalBuildTypeLineage(typeName, type);
		}
		
		@Override
		public <T> PersistenceTypeLineage<T> createTypeLineage(final Class<T> type)
		{
			return internalBuildTypeLineage(type.getName(), type);
		}
		
	}
}
