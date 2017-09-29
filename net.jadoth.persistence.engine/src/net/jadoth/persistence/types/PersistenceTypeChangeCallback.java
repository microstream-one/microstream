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
}
