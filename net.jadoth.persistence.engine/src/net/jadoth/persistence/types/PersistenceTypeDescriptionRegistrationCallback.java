package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionRegistrationCallback
{
	public void registerTypeDescription(PersistenceTypeDefinition<?> typeDescription);
		
}
