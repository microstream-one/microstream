package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionMismatchCallback<T>
{
	public void reportMismatch(
		PersistenceTypeDescription<T> latestPersistedDescription,
		PersistenceTypeDescription<T> runtimeDescription
	);
}
