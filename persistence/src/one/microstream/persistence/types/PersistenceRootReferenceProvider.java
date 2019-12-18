package one.microstream.persistence.types;

public interface PersistenceRootReferenceProvider<M>
{
	public PersistenceRootReference provideRootReference();
	
	public PersistenceTypeHandler<M, ? extends PersistenceRootReference> provideTypeHandler();
}
