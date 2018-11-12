package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDictionaryViewProvider
{
	public PersistenceTypeDictionaryView provideTypeDictionary();
}
