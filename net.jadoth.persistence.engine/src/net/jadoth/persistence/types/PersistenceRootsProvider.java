package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleRegistry;


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
		SwizzleRegistry                         objectRegistry
	);

}
