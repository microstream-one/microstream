package one.microstream.persistence.types;

public interface PersistenceTypeLineageCreator
{
	public PersistenceTypeLineage createTypeLineage(Class<?> type);
	
	public PersistenceTypeLineage createTypeLineage(String typeName, Class<?> type);
	
		
	
	public static PersistenceTypeLineageCreator.Default New()
	{
		return new PersistenceTypeLineageCreator.Default();
	}
	
	public final class Default implements PersistenceTypeLineageCreator
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default()
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
