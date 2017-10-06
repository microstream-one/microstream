package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeChangeCallback
{
	public default void validateMissingRuntimeType(final PersistenceTypeDefinition<?> typeDefinition)
	{
		// missing runtime types allowed by default
	}
	
	public default void validateTypeChange(final PersistenceTypeDefinition<?> latest, final PersistenceTypeDescription current)
	{
		// type change allowed by default
	}
	
	public <T> void registerTypeChange(PersistenceTypeDefinition<?> latest, PersistenceTypeDefinition<?> current);
	
	
	
	public static PersistenceTypeChangeCallback.ImplementationAborting Aborting()
	{
		return new PersistenceTypeChangeCallback.ImplementationAborting();
	}
	
	public final class ImplementationAborting implements PersistenceTypeChangeCallback
	{
		@Override
		public final void validateMissingRuntimeType(final PersistenceTypeDefinition<?> typeDefinition)
		{
			// (06.10.2017 TM)EXCP: proper exception
			throw new RuntimeException("Missing runtime type: " + typeDefinition.typeName());
		}
		
		@Override
		public final void validateTypeChange(final PersistenceTypeDefinition<?> latest, final PersistenceTypeDescription current)
		{
			// (06.10.2017 TM)EXCP: proper exception
			throw new RuntimeException("Runtime type mismatch: " + current.typeName());
		}

		@Override
		public final <T> void registerTypeChange(final PersistenceTypeDefinition<?> latest, final PersistenceTypeDefinition<?> current)
		{
			this.validateTypeChange(latest, current);
		}
		
	}
}
