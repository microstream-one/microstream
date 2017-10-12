package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDefinitionRegistrationCallback
{
	public void registerTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);
		
}
