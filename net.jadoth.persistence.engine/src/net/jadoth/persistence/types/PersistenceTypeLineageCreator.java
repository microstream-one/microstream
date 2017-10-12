package net.jadoth.persistence.types;

public interface PersistenceTypeLineageCreator
{
	public <T> PersistenceTypeLineage<T> createTypeLineage(String typeName, Class<T> type);
	
	
	
	public static PersistenceTypeLineageCreator.Implementation New(
//		final PersistenceTypeDefinitionCreator typeDefinitionCreator
	)
	{
		return new PersistenceTypeLineageCreator.Implementation(
//			notNull(typeDefinitionCreator)
		);
	}
	
	public final class Implementation implements PersistenceTypeLineageCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
//		final PersistenceTypeDefinitionCreator typeDefinitionCreator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(/*final PersistenceTypeDefinitionCreator typeDefinitionCreator*/)
		{
			super();
//			this.typeDefinitionCreator = typeDefinitionCreator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
				
//		private <T> PersistenceTypeLineage<T> internalBuildTypeLineage(final String typeName, final Class<T> type)
//		{
//			return PersistenceTypeLineage.New(typeName, type/*, this.typeDefinitionCreator*/);
//		}
		
		@Override
		public <T> PersistenceTypeLineage<T> createTypeLineage(final String typeName, final Class<T> type)
		{
			return PersistenceTypeLineage.New(typeName, type/*, this.typeDefinitionCreator*/);
		}
				
	}
}
