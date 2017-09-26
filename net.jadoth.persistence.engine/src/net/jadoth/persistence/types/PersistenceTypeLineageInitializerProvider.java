package net.jadoth.persistence.types;

public interface PersistenceTypeLineageInitializerProvider
{
	public <T> PersistenceTypeLineageBuilder<T> provideTypeLineageInitializer(String typeName);
}
