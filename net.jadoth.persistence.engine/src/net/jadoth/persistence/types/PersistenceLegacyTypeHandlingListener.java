package net.jadoth.persistence.types;

public interface PersistenceLegacyTypeHandlingListener<M>
{
	public <T> void reportLegacyTypeHandling(
		long                         objectId             ,
		T                            instance             ,
		PersistenceTypeDefinition<?> legacyTypeDescription,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
}
