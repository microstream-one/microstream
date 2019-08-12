package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.types.BinaryHandlerPersistenceRootsDefault;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceRootsProvider;


public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
	public static BinaryPersistenceRootsProvider New(
		final PersistenceRootResolverProvider rootResolverProvider
	)
	{
		return new BinaryPersistenceRootsProvider.Default(
			notNull(rootResolverProvider)
		);
	}
	
	public final class Default implements BinaryPersistenceRootsProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceRootResolverProvider rootResolverProvider;
		
		transient PersistenceRootResolver rootResolver;
		transient PersistenceRoots        roots       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final PersistenceRootResolverProvider rootResolverProvider)
		{
			super();
			this.rootResolverProvider = rootResolverProvider;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private PersistenceRootResolver ensureRootResolver()
		{
			if(this.rootResolver == null)
			{
				this.rootResolver = this.rootResolverProvider.provideRootResolver();
			}
			
			return this.rootResolver;
		}

		@Override
		public final PersistenceRoots provideRoots()
		{
			if(this.roots == null)
			{
				this.roots = PersistenceRoots.New(this.ensureRootResolver());
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
			final BinaryHandlerPersistenceRootsDefault handler = BinaryHandlerPersistenceRootsDefault.New(
				this.rootResolverProvider,
				objectRegistry
			);
			typeHandlerRegistry.registerTypeHandler(handler);
		}

	}

}
