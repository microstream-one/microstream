package net.jadoth.storage.types;

import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.exceptions.MissingFoundationPartException;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.Persistence;
import net.jadoth.persistence.types.PersistenceObjectIdProvider;
import net.jadoth.persistence.types.PersistenceRefactoringMappingProvider;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceRootsProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.persistence.types.PersistenceTypeManager;
import net.jadoth.reference.Reference;

public interface EmbeddedStorageFoundation<F extends EmbeddedStorageFoundation<?>> extends StorageFoundation<F>
{
	public EmbeddedStorageConnectionFoundation<?> getConnectionFoundation();
	
	// next level method chaining 8-)
	public F onConnectionFoundation(Consumer<? super EmbeddedStorageConnectionFoundation<?>> logic);

	public default EmbeddedStorageManager createEmbeddedStorageManager()
	{
		// no explicit root by default
		return this.createEmbeddedStorageManager(null);
	}
	
	public EmbeddedStorageManager createEmbeddedStorageManager(Object explicitRoot);
	
	public default EmbeddedStorageManager start()
	{
		return this.start(null);
	}
	
	public default EmbeddedStorageManager start(final Object explicitRoot)
	{
		final EmbeddedStorageManager esm = this.createEmbeddedStorageManager(explicitRoot);
		esm.start();
		return esm;
	}

	public F setConnectionFoundation(EmbeddedStorageConnectionFoundation<?> connectionFoundation);

	public F setRootResolver(PersistenceRootResolver rootResolver);
	
	public F setRoot(Object root);
	
	public F setRefactoringMappingProvider(PersistenceRefactoringMappingProvider refactoringMappingProvider);

	

	public class Implementation<F extends EmbeddedStorageFoundation.Implementation<?>>
	extends StorageFoundation.Implementation<F>
	implements EmbeddedStorageFoundation<F>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private EmbeddedStorageConnectionFoundation<?> connectionFoundation;

		

		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public F onConnectionFoundation(
			final Consumer<? super EmbeddedStorageConnectionFoundation<?>> logic
		)
		{
			final EmbeddedStorageConnectionFoundation<?> escf = this.getConnectionFoundation();
			logic.accept(escf);
			
			return this.$();
		}
		
		@Override
		public F setRoot(final Object root)
		{
			this.setRootResolver(
				Persistence.RootResolver(root)
			);
			
			return this.$();
		}
		
		protected EmbeddedStorageConnectionFoundation<?> createConnectionFoundation()
		{
			throw new MissingFoundationPartException(EmbeddedStorageConnectionFoundation.class);
//			return new EmbeddedStorageConnectionFoundation.Implementation();
		}

		@Override
		protected EmbeddedStorageRootTypeIdProvider createRootTypeIdProvider()
		{
			final EmbeddedStorageConnectionFoundation<?> escf          = this.getConnectionFoundation();
			final PersistenceRootsProvider<Binary>       rootsProvider = escf.getRootsProvider();
			
			// the genericness of this :D (albeit #provideRoots is implicitly assumed to cache the instance)
			return EmbeddedStorageRootTypeIdProvider.New(
				rootsProvider.provideRoots().getClass()
			);
		}

		@Override
		public EmbeddedStorageConnectionFoundation<?> getConnectionFoundation()
		{
			if(this.connectionFoundation == null)
			{
				this.connectionFoundation = this.dispatch(this.createConnectionFoundation());
			}
			
			return this.connectionFoundation;
		}

		/* (02.03.2014)TODO: Storage Configuration more dynamic
		 *  The configuration must be provided in the creation process, not set idependantly.
		 *  Example: cache evaluator might have to know all the channel caches.
		 *  To avoid initializer loops (configuration must exist for the channels to be created),
		 *  an initialize(...) pattern must be used.
		 *
		 *  Speaking of which:
		 *  It may generally be a good idea to retrofit numerous types with initialize(...) methods
		 *  (mostly for backreferencing channels). If an implementation doesn't need the backreference,
		 *  the initialize method(s) can simply be no-op.
		 *  An initialization tree diagram should be created to asses the initialization dependancies.
		 */
		@Override
		public F setConfiguration(final StorageConfiguration configuration)
		{
			super.setConfiguration(configuration);
			return this.$();
		}

		@Override
		public F setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			super.setRequestAcceptorCreator(requestAcceptorCreator);
			return this.$();
		}

		@Override
		public F setTaskBrokerCreator(
			final StorageTaskBroker.Creator taskBrokerCreator
		)
		{
			super.setTaskBrokerCreator(taskBrokerCreator);
			return this.$();
		}

		@Override
		public F setDataChunkValidatorProvider(
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			super.setDataChunkValidatorProvider(dataChunkValidatorProvider);
			return this.$();
		}

		@Override
		public F setChannelCreator(final StorageChannel.Creator channelCreator)
		{
			super.setChannelCreator(channelCreator);
			return this.$();
		}

		@Override
		public F setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			super.setTaskCreator(taskCreator);
			return this.$();
		}

		@Override
		public F setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			super.setTypeDictionary(typeDictionary);
			return this.$();
		}

		@Override
		public F setRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider
		)
		{
			super.setRootTypeIdProvider(rootTypeIdProvider);
			return this.$();
		}

		@Override
		public F setConnectionFoundation(
			final EmbeddedStorageConnectionFoundation<?> connectionFoundation
		)
		{
			this.connectionFoundation = connectionFoundation;

			/* Tricky: this instance must be set as a callback StorageManager supplier in case
			 * the getStorageManager method is called before createEmbeddedStorageManager.
			 * E.g.: setting customizing logic
			 */
			this.connectionFoundation.setStorageManagerSupplier(() ->
				this.createStorageManager()
			);
			
			return this.$();
		}
		
		@Override
		public F setRootResolver(final PersistenceRootResolver rootResolver)
		{
			this.getConnectionFoundation().setRootResolver(rootResolver);
			return this.$();
		}
		
		@Override
		public F setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			this.getConnectionFoundation().setRefactoringMappingProvider(refactoringMappingProvider);
			return this.$();
		}

		@Override
		public F setTimestampProvider(
			final StorageTimestampProvider timestampProvider
		)
		{
			super.setTimestampProvider(timestampProvider);
			return this.$();
		}

		private void initializeEmbeddedStorageRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider,
			final PersistenceTypeManager        typeIdLookup
		)
		{
			// a little hacky instanceof here, maybe refactor to cleaner structure in the future
			if(rootTypeIdProvider instanceof EmbeddedStorageRootTypeIdProvider)
			{
				((EmbeddedStorageRootTypeIdProvider)rootTypeIdProvider).initialize(typeIdLookup);
			}
			// on the other hand, this is a very flexible way of still allowing another rootTypeId source
		}

		@Override
		protected StorageObjectIdRangeEvaluator createObjectIdRangeEvaluator()
		{
			final PersistenceObjectIdProvider oip = this.getConnectionFoundation().getObjectIdProvider();
			return (min, max) ->
			{
				// update OID provider if necessary to avoid OID inconsistencies.
				if(max > oip.currentObjectId())
				{
					oip.updateCurrentObjectId(max);
				}
			};
		}
		
		@SuppressWarnings("unchecked")
		private static Reference<Object> ensureRootReference(final Object explicitRoot)
		{
			return explicitRoot instanceof Reference
				? (Reference<Object>)explicitRoot
				: X.Reference(explicitRoot)
			;
		}
		
		private Reference<Object> createRoot(final Object explicitRoot)
		{
			// if an explicit root is provided, it is used (set), no matter what
			if(explicitRoot != null)
			{
				final Reference<Object> root = ensureRootReference(explicitRoot);
				this.setRoot(root);
				return root;
			}
			
			// if there is no explicit root but an already set root resolver, no generic root is created
			final PersistenceRootResolver rootResolver = this.getConnectionFoundation().rootResolver();
			if(rootResolver != null)
			{
				return null;
			}
			
			// if there is no root at all, yet, an empty generic one is created for later use.
			final Reference<Object> root = X.Reference(null);
			this.setRoot(root);
			return root;
		}
		
		@Override
		public synchronized EmbeddedStorageManager createEmbeddedStorageManager(final Object explicitRoot)
		{
			// this is all a bit of clumsy detour due to conflicted initialization order. Maybe overhaul.
			
			final Reference<Object> root = this.createRoot(explicitRoot);

			final EmbeddedStorageConnectionFoundation<?> ecf = this.getConnectionFoundation();
			
			// must be created BEFORE the type handler manager is initilized to register its custom type handler
			final PersistenceRootsProvider<Binary> prp = ecf.getRootsProvider();

			// initialize persistence (=binary) type handler manager (validate and ensure type handlers)
			final PersistenceTypeHandlerManager<?> thm = ecf.getTypeHandlerManager();
			thm.initialize();
			
			// the registered supplier callback leads back to this class' createStorageManager method
			final StorageManager stm = ecf.getStorageManager();
			
			// type storage dictionary updating moved here as well to keep all nasty parts at one place ^^.
			final StorageTypeDictionary std = stm.typeDictionary();
			std
			.initialize(ecf.getTypeDictionaryProvider().provideTypeDictionary())
			.setTypeDescriptionRegistrationObserver(std)
			;

			// resolve root types to root type ids after types have been initialized
			this.initializeEmbeddedStorageRootTypeIdProvider(this.getRootTypeIdProvider(), thm);

			// the roots instance to be used
			final PersistenceRoots roots = prp.provideRoots();
				
			// everything bundled together in the actual manager instance
			return EmbeddedStorageManager.New(stm.configuration(), ecf, roots, root);
		}

	}

}
