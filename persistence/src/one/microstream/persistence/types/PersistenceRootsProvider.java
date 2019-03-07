package one.microstream.persistence.types;

public interface PersistenceRootsProvider<M>
{
	public PersistenceRoots provideRoots();
	
	/**
	 * Only the {@link PersistenceRootsProvider} implementation can ensure that the handler fits the instance,
	 * so it has to do the registering as well.
	 *
	 * @param registry
	 */
	public void registerRootsTypeHandlerCreator(
		PersistenceCustomTypeHandlerRegistry<M> typeHandlerRegistry,
		PersistenceObjectRegistry                         objectRegistry
	);

}
