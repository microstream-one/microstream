package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDictionaryViewProvider extends PersistenceTypeDictionaryProvider
{
	@Override
	public PersistenceTypeDictionaryView provideTypeDictionary();
}
