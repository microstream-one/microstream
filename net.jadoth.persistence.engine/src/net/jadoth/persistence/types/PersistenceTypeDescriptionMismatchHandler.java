package net.jadoth.persistence.types;

@FunctionalInterface
public interface PersistenceTypeDescriptionMismatchHandler<T>
{
	public void reportTypeMismatch(
		PersistenceTypeDescription<T> currentTypeDescriptionMembers,
		PersistenceTypeDescription<T> latestObsoleteTypeDescription
	);
}
