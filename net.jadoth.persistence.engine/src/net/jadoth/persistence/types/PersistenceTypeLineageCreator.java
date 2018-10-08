package net.jadoth.persistence.types;

public interface PersistenceTypeLineageCreator
{
	public <T> PersistenceTypeLineage createTypeLineage(Class<T> type);
	
	public PersistenceTypeLineage createTypeLineage(String typeName);
	
		
	
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
		
		private <T> PersistenceTypeLineage createTypeLineage(final String typeName, final Class<T> type)
		{
			return PersistenceTypeLineage.New(typeName, type);
		}
		
		@Override
		public final <T> PersistenceTypeLineage createTypeLineage(final Class<T> type)
		{
			return this.createTypeLineage(type.getName(), type);
		}
		
		@Override
		public final PersistenceTypeLineage createTypeLineage(final String typeName)
		{
			// type dictionary entries do not necessarily have to be resolvable to the current type model.
			final Class<?> type = Persistence.resolveTypeOptional(typeName);
			
			return this.createTypeLineage(typeName, type);
		}
				
	}
}
