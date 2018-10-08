package net.jadoth.persistence.types;

public interface PersistenceLegacyTypeHandlingListener<M>
{
	public <T> void registerLegacyTypeHandlingCreation(
		long                         objectId             ,
		T                            instance             ,
		PersistenceTypeDefinition legacyTypeDescription,
		PersistenceTypeHandler<M, T> currentTypeHandler
	);
	
	// (28.09.2018 TM)TODO: Legacy Type Mapping: What about register~Completion to have completed instances?
}
