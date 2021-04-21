package one.microstream.persistence.types;

@FunctionalInterface
public interface PersistenceLoggerProvider
{
	public PersistenceLogger providePersistenceLogger();
}
