package net.jadoth.storage.types;

import net.jadoth.persistence.binary.internal.BinaryHandlerPersistenceRootsImplementation;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceCustomTypeHandlerRegistry;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceRootsProvider;
import net.jadoth.swizzling.types.SwizzleRegistry;


public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
	public final class Implementation implements BinaryPersistenceRootsProvider
	{
		PersistenceRoots.Implementation roots;

		@Override
		public final PersistenceRoots.Implementation provideRoots(final PersistenceRootResolver rootResolver)
		{
			if(this.roots == null)
			{
				// must always be consistent with #provideRootsClass
				this.roots = PersistenceRoots.Implementation.New(rootResolver.getRootInstances());
			}
			return this.roots;
		}
		
		@Override
		public final Class<?> provideRootsClass()
		{
			// must always be consistent with #provideRoots
			return PersistenceRoots.Implementation.class;
		}

		@Override
		public final void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
			final SwizzleRegistry                              objectRegistry     ,
			final PersistenceRootResolver                      rootResolver
		)
		{
			final BinaryHandlerPersistenceRootsImplementation.Creator handlerCreator =
				new BinaryHandlerPersistenceRootsImplementation.Creator(rootResolver, objectRegistry)
			;
			typeHandlerRegistry.registerTypeHandlerCreator(handlerCreator);
		}

	}

}
