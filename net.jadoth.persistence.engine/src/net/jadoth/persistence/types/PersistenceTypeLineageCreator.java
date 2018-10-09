package net.jadoth.persistence.types;

public interface PersistenceTypeLineageCreator
{
	public PersistenceTypeLineage createTypeLineage(Class<?> type);
	
	public PersistenceTypeLineage createTypeLineage(String typeName, Class<?> type);
	
		
	
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
		public PersistenceTypeLineage createTypeLineage(final String typeName, final Class<?> type)
		{
			return PersistenceTypeLineage.New(typeName, type);
		}
		
		@Override
		public PersistenceTypeLineage createTypeLineage(final Class<?> type)
		{
			return this.createTypeLineage(type.getName(), type);
		}
				
	}
	
}
