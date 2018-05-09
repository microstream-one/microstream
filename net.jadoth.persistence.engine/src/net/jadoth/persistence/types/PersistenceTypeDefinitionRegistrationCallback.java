package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDefinitionRegistrationCallback
{
	public void registerTypeDescription(PersistenceTypeDefinition<?> typeDefinition);
		
}
