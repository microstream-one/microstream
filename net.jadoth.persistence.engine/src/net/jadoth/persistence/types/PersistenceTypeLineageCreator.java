package net.jadoth.persistence.types;

public interface PersistenceTypeLineageCreator
{
	public <T> PersistenceTypeLineage<T> createTypeLineage(String typeName, Class<T> type);
	
	
	
	public static PersistenceTypeLineageCreator.Implementation New()
	{
		return new PersistenceTypeLineageCreator.Implementation();
	}
	
	public final class Implementation implements PersistenceTypeLineageCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public <T> PersistenceTypeLineage<T> createTypeLineage(final String typeName, final Class<T> type)
		{
			return PersistenceTypeLineage.New(typeName, type);
		}
				
	}
}
