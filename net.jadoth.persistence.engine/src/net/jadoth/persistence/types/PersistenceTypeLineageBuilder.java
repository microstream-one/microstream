package net.jadoth.persistence.types;

import static net.jadoth.Jadoth.notNull;

public interface PersistenceTypeLineageBuilder
{
	public <T> PersistenceTypeLineage<T> buildTypeLineage(String typeName);
	
	public <T> PersistenceTypeLineage<T> buildTypeLineage(Class<T> type);
	
	
	
	public static PersistenceTypeLineageBuilder.Implementation New(
		final PersistenceTypeDefinitionBuilder typeDefinitionBuilder
	)
	{
		return new PersistenceTypeLineageBuilder.Implementation(
			notNull(typeDefinitionBuilder)
		);
	}
	
	public final class Implementation implements PersistenceTypeLineageBuilder
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final PersistenceTypeDefinitionBuilder typeDefinitionBuilder;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final PersistenceTypeDefinitionBuilder typeDefinitionBuilder)
		{
			super();
			this.typeDefinitionBuilder = typeDefinitionBuilder;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
		private <T> PersistenceTypeLineage<T> internalBuildTypeLineage(final String typeName, final Class<T> type)
		{
			return PersistenceTypeLineage.New(typeName, type, this.typeDefinitionBuilder);
		}
		
		@Override
		public <T> PersistenceTypeLineage<T> buildTypeLineage(final String typeName)
		{
			final Class<T> type = resolveTypeOptional(typeName); // might be null
			return internalBuildTypeLineage(typeName, type);
		}
		
		@Override
		public <T> PersistenceTypeLineage<T> buildTypeLineage(final Class<T> type)
		{
			return internalBuildTypeLineage(type.getName(), type);
		}
		
	}
}
