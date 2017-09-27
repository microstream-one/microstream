package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeChangeCallback
{
	public default void validateTypeChange(final PersistenceTypeDescription latest, final PersistenceTypeDescription current)
	{
		// no-op by default
	}
	
	public <T> void registerTypeChange(PersistenceTypeDefinition<T> latest, PersistenceTypeDefinition<T> current);
}
