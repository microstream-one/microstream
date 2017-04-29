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
		public final PersistenceRoots.Implementation provideRoots()
		{
			if(this.roots == null)
			{
				this.roots = new PersistenceRoots.Implementation();
			}
			return this.roots;
		}

		@Override
		public final void registerTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
			final SwizzleRegistry                              objectRegistry     ,
			final PersistenceRootResolver                      rootResolver
		)
		{
			final BinaryHandlerPersistenceRootsImplementation handler =
				BinaryHandlerPersistenceRootsImplementation.New(rootResolver, objectRegistry)
			;
			typeHandlerRegistry.registerTypeHandler(handler);
		}

	}

}
