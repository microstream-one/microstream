package one.microstream.persistence.types;

public interface PersistenceRootsProvider<D>
{
	public PersistenceRoots provideRoots();
	
	public PersistenceRoots peekRoots();
	
	public void updateRuntimeRoots(PersistenceRoots runtimeRoots);
	
	/**
	 * Only the {@link PersistenceRootsProvider} implementation can ensure that the handler fits the instance,
	 * so it has to do the registering as well.
	 *
	 * @param typeHandlerRegistry
	 * @param objectRegistry
	 */
	public void registerRootsTypeHandlerCreator(
		PersistenceCustomTypeHandlerRegistry<D> typeHandlerRegistry,
		PersistenceObjectRegistry               objectRegistry
	);

}
