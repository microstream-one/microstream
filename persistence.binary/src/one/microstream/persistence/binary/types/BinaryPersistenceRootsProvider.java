package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.internal.BinaryHandlerPersistenceRootsDefault;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceRootsProvider;


public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
	public static BinaryPersistenceRootsProvider New(final PersistenceRootResolver rootResolver)
	{
		return new BinaryPersistenceRootsProvider.Default(
			notNull(rootResolver)
		);
	}
	
	public final class Default implements BinaryPersistenceRootsProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final     PersistenceRootResolver rootResolver;
		transient PersistenceRoots        roots       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceRootResolver rootResolver)
		{
			super();
			this.rootResolver = rootResolver;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final PersistenceRoots provideRoots()
		{
			if(this.roots == null)
			{
				// must always be consistent with #provideRootsClass
				this.roots = PersistenceRoots.New(this.rootResolver);
			}
			
			return this.roots;
		}
		
		@Override
		public final PersistenceRoots peekRoots()
		{
			return this.roots;
		}
		
		@Override
		public final void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
			final PersistenceObjectRegistry                    objectRegistry
		)
		{
			final BinaryHandlerPersistenceRootsDefault handler =
				BinaryHandlerPersistenceRootsDefault.New(this.rootResolver, objectRegistry)
			;
			typeHandlerRegistry.registerTypeHandler(handler);
		}

	}

}
