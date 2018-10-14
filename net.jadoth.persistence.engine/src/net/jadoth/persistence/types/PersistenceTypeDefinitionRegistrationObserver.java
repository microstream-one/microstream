package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDefinitionRegistrationObserver
{
	public void observeTypeDefinitionRegistration(PersistenceTypeDefinition typeDefinition);
		
}
