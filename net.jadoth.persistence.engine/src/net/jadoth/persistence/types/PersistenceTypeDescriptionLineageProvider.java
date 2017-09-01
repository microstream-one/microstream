package net.jadoth.persistence.types;

public interface PersistenceTypeDescriptionLineageProvider
{
	// (01.09.2017 TM)TODO: link with PersistenceTypeDescriptionInitializerLookup
	public PersistenceTypeDescriptionLineage<?> provideTypeDescriptionLineage(String typeName);
}
