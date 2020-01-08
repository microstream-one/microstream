package one.microstream.persistence.types;

public interface PersistenceRootReferenceProvider<D>
{
	public PersistenceRootReference provideRootReference();
	
	public PersistenceTypeHandler<D, ? extends PersistenceRootReference> provideTypeHandler(
		PersistenceObjectRegistry globalRegistry
	);
}
