package net.jadoth.persistence.types;

import net.jadoth.swizzling.types.SwizzleRegistry;


public interface PersistenceRootsProvider<M>
{
	public PersistenceRoots provideRoots(PersistenceRootResolver rootResolver);
	
	/**
	 * This is really the actual concrete class, not abstract interface of the {@link PersistenceRoots} instance
	 * that this provider will create. This information is needed to create the correct TypeIdProvider.
	 * 
	 * @return
	 */
	public Class<?> provideRootsClass();

	/**
	 * Only the {@link PersistenceRootsProvider} implementation can ensure that the handler fits the instance,
	 * so it has to do the registering as well.
	 *
	 * @param registry
	 */
	public void registerRootsTypeHandlerCreator(
		PersistenceCustomTypeHandlerRegistry<M> typeHandlerRegistry,
		SwizzleRegistry                         objectRegistry     ,
		PersistenceRootResolver                 rootResolver
	);

}
