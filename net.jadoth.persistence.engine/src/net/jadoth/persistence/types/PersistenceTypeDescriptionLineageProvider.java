package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionLineageProvider
{
	// (01.09.2017 TM)TODO: link with PersistenceTypeDescriptionInitializerLookup
	public <T> PersistenceTypeDescriptionLineage<T> provideTypeDescriptionLineage(String typeName);
}
