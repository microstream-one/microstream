package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionRegistrationCallback
{
	public void registerTypeDefinition(PersistenceTypeDefinition<?> typeDefinition);
		
}
