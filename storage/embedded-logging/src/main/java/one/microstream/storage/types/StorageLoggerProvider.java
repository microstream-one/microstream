package one.microstream.storage.types;

@FunctionalInterface
public interface StorageLoggerProvider
{
	public StorageLogger provideStorageLogger();
}
