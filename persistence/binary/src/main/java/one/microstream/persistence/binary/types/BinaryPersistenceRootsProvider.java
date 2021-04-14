package one.microstream.persistence.binary.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.one.microstream.persistence.types.BinaryHandlerPersistenceRootsDefault;
import one.microstream.persistence.types.PersistenceCustomTypeHandlerRegistry;
import one.microstream.persistence.types.PersistenceObjectRegistry;
import one.microstream.persistence.types.PersistenceRootReference;
import one.microstream.persistence.types.PersistenceRootReferenceProvider;
import one.microstream.persistence.types.PersistenceRootResolver;
import one.microstream.persistence.types.PersistenceRootResolverProvider;
import one.microstream.persistence.types.PersistenceRoots;
import one.microstream.persistence.types.PersistenceRootsProvider;
import one.microstream.persistence.types.PersistenceTypeHandler;


public interface BinaryPersistenceRootsProvider extends PersistenceRootsProvider<Binary>
{
	public static BinaryPersistenceRootsProvider New(
		final PersistenceRootResolverProvider          rootResolverProvider ,
		final PersistenceRootReferenceProvider<Binary> rootReferenceProvider
	)
	{
		return new BinaryPersistenceRootsProvider.Default(
			notNull(rootResolverProvider) ,
			notNull(rootReferenceProvider)
		);
	}
	
	public final class Default implements BinaryPersistenceRootsProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final PersistenceRootResolverProvider          rootResolverProvider ;
		final PersistenceRootReferenceProvider<Binary> rootReferenceProvider;
		
		transient PersistenceRootResolver rootResolver;
		transient PersistenceRoots        roots       ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceRootResolverProvider          rootResolverProvider ,
			final PersistenceRootReferenceProvider<Binary> rootReferenceProvider
		)
		{
			super();
			this.rootResolverProvider  = rootResolverProvider ;
			this.rootReferenceProvider = rootReferenceProvider;
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
		public final void updateRuntimeRoots(final PersistenceRoots runtimeRoots)
		{
			this.roots = runtimeRoots;
		}
		
		@Override
		public final void registerRootsTypeHandlerCreator(
			final PersistenceCustomTypeHandlerRegistry<Binary> typeHandlerRegistry,
			final PersistenceObjectRegistry                    objectRegistry
		)
		{
			final BinaryHandlerPersistenceRootsDefault rootsHandler = BinaryHandlerPersistenceRootsDefault.New(
				this.rootResolverProvider,
				objectRegistry
			);
			
			final PersistenceTypeHandler<Binary, ? extends PersistenceRootReference> rootReferenceHandler =
				this.rootReferenceProvider.provideTypeHandler(objectRegistry)
			;
			
			typeHandlerRegistry.registerTypeHandler(rootsHandler);
			typeHandlerRegistry.registerTypeHandler(rootReferenceHandler);
		}

	}

}
