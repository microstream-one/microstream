package net.jadoth.storage.types;

import net.jadoth.persistence.types.PersistenceRefactoringMappingProvider;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface EmbeddedStorageFoundation extends StorageFoundation
{
	///////////////////////////////////////////////////////////////////////////
	// getters         //
	/////////////////////

	public EmbeddedStorageConnectionFoundation getConnectionFoundation();

	public PersistenceRootResolver getRootResolver();

	public BinaryPersistenceRootsProvider getRootsProvider();

	public PersistenceRefactoringMappingProvider getRefactoringMappingProvider();

	public EmbeddedStorageManager createEmbeddedStorageManager();



	///////////////////////////////////////////////////////////////////////////
	// setters          //
	/////////////////////

	@Override
	public EmbeddedStorageFoundation setRequestAcceptorCreator(StorageRequestAcceptor.Creator requestAcceptorCreator);

	@Override
	public EmbeddedStorageFoundation setTaskBrokerCreator(StorageTaskBroker.Creator taskBrokerCreator);

	@Override
	public EmbeddedStorageFoundation setDataChunkValidatorProvider(
		StorageValidatorDataChunk.Provider dataChunkValidatorProvider
	);

	@Override
	public EmbeddedStorageFoundation setChannelCreator(StorageChannel.Creator channelCreator);

	@Override
	public EmbeddedStorageFoundation setTaskCreator(StorageRequestTaskCreator taskCreator);

	@Override
	public EmbeddedStorageFoundation setTypeDictionary(StorageTypeDictionary typeDictionary);

	@Override
	public EmbeddedStorageFoundation setConfiguration(StorageConfiguration configuration);

	@Override
	public EmbeddedStorageFoundation setTimestampProvider(StorageTimestampProvider storageTimestampProvider);

	@Override
	public EmbeddedStorageFoundation setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);

	public EmbeddedStorageFoundation setRootResolver(PersistenceRootResolver rootResolver);

	public EmbeddedStorageFoundation setRootsProvider(BinaryPersistenceRootsProvider rootsProvider);
	
	public EmbeddedStorageFoundation setRefactoringMappingProvider(PersistenceRefactoringMappingProvider refactoringMappingProvider);

	public EmbeddedStorageFoundation setConnectionFoundation(EmbeddedStorageConnectionFoundation connectionFoundation);





	public class Implementation extends StorageFoundation.Implementation implements EmbeddedStorageFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private PersistenceRootResolver               rootResolver              ;
		private BinaryPersistenceRootsProvider        rootsProvider             ;
		private PersistenceRefactoringMappingProvider refactoringMappingProvider;
		private EmbeddedStorageConnectionFoundation   connectionFoundation      ;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected PersistenceRootResolver createRootResolver()
		{
			return Storage.RootResolver(EmbeddedStorage::root);
		}

		protected BinaryPersistenceRootsProvider createRootsProvider()
		{
			return new BinaryPersistenceRootsProvider.Implementation();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		protected EmbeddedStorageConnectionFoundation createConnectionFoundation()
		{
			return new EmbeddedStorageConnectionFoundation.Implementation();
		}

		@Override
		protected EmbeddedStorageRootTypeIdProvider createRootTypeIdProvider()
		{
			return EmbeddedStorageRootTypeIdProvider.New(
				this.getRootsProvider().provideRootsClass()
			);
		}

		@Override
		public EmbeddedStorageConnectionFoundation getConnectionFoundation()
		{
			if(this.connectionFoundation == null)
			{
				this.connectionFoundation = this.dispatch(this.createConnectionFoundation());
			}
			return this.connectionFoundation;
		}

		@Override
		public PersistenceRootResolver getRootResolver()
		{
			if(this.rootResolver == null)
			{
				this.rootResolver = this.dispatch(this.createRootResolver());
			}
			return this.rootResolver;
		}
		
		@Override
		public PersistenceRefactoringMappingProvider getRefactoringMappingProvider()
		{
			// no lazy creation. This is actually just a getter for an optional element.
			return this.refactoringMappingProvider;
		}

		@Override
		public BinaryPersistenceRootsProvider getRootsProvider()
		{
			if(this.rootsProvider == null)
			{
				this.rootsProvider = this.dispatch(this.createRootsProvider());
			}
			return this.rootsProvider;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRootResolver(final PersistenceRootResolver rootResolver)
		{
			this.rootResolver = rootResolver;
			return this;
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
		public EmbeddedStorageFoundation.Implementation setConfiguration(final StorageConfiguration configuration)
		{
			super.setConfiguration(configuration);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			super.setRequestAcceptorCreator(requestAcceptorCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTaskBrokerCreator(
			final StorageTaskBroker.Creator taskBrokerCreator
		)
		{
			super.setTaskBrokerCreator(taskBrokerCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setDataChunkValidatorProvider(
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			super.setDataChunkValidatorProvider(dataChunkValidatorProvider);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setChannelCreator(final StorageChannel.Creator channelCreator)
		{
			super.setChannelCreator(channelCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			super.setTaskCreator(taskCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			super.setTypeDictionary(typeDictionary);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider
		)
		{
			super.setRootTypeIdProvider(rootTypeIdProvider);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setConnectionFoundation(
			final EmbeddedStorageConnectionFoundation connectionFoundation
		)
		{
			this.connectionFoundation = connectionFoundation;
			return this;
		}

		@Override
		public EmbeddedStorageFoundation setRootsProvider(final BinaryPersistenceRootsProvider rootsProvider)
		{
			this.rootsProvider = rootsProvider;
			return this;
		}
		
		@Override
		public EmbeddedStorageFoundation setRefactoringMappingProvider(
			final PersistenceRefactoringMappingProvider refactoringMappingProvider
		)
		{
			this.refactoringMappingProvider = refactoringMappingProvider;
			return this;
		}


		@Override
		public EmbeddedStorageFoundation.Implementation setTimestampProvider(
			final StorageTimestampProvider timestampProvider
		)
		{
			super.setTimestampProvider(timestampProvider);
			return this;
		}

		private void initializeEmbeddedStorageRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider,
			final SwizzleTypeManager        typeIdLookup
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
			final SwizzleObjectIdProvider oip = this.getConnectionFoundation().getObjectIdProvider();
			return (min, max) ->
			{
				// update OID provider if necessary to avoid OID inconsistencies.
				if(max > oip.currentObjectId())
				{
					oip.updateCurrentObjectId(max);
				}
			};
		}

		protected PersistenceRootResolver provideRootResolver()
		{
			final PersistenceRootResolver               definedRootResolver = this.getRootResolver();
			final PersistenceRefactoringMappingProvider mappingProvider     = this.getRefactoringMappingProvider();
			
			return mappingProvider == null
				? definedRootResolver
				: PersistenceRootResolver.Wrap(definedRootResolver, mappingProvider)
			;
		}
		
		@Override
		public synchronized EmbeddedStorageManager createEmbeddedStorageManager()
		{
			// this is all a bit of clumsy detour due to conflicted initialization order. Maybe overhaul.

			final EmbeddedStorageConnectionFoundation ecf = this.getConnectionFoundation();
			final PersistenceTypeHandlerManager<?>    thm = ecf.getTypeHandlerManager();

			/* (13.09.2015)TODO: StorageEntityTypeHandlerCreator for storage-side lazy ref handling
			 * link PersistenceTypeHandlerManager and to-be-created StorageEntityTypeHandlerCreator
			 * in order to have a way for the entity type handler creation recognize lazy references.
			 * Required for storage-side graph deep-reference loading.
			 */
			final StorageManager stm = this.createStorageManager();
			ecf.setStorageManager(stm);

			final BinaryPersistenceRootsProvider prp = this.getRootsProvider();

			final PersistenceRootResolver rootResolver = this.provideRootResolver();
			
			// register special case type handler for roots instance
			prp.registerRootsTypeHandlerCreator(
				ecf.getCustomTypeHandlerRegistry(),
				ecf.getSwizzleRegistry(),
				rootResolver
			);

			// (04.05.2015)TODO: /!\ Entry point for improved type description validation
			// initialize persistence (=binary) type handler manager (validate and ensure type handlers)
			thm.initialize();

			// (22.01.2015 TM)TODO: prevent unnecessary writing of type dictionary

			// type storage dictionary updating moved here as well to keep all nasty parts at one place ^^.
			final StorageTypeDictionary std = stm.typeDictionary();
			std
			.initialize(ecf.getTypeDictionaryProvider().provideDictionary())
			.setTypeDescriptionRegistrationCallback(std)
			;

			// resolve root types to root type ids after types have been initialized
			this.initializeEmbeddedStorageRootTypeIdProvider(this.getRootTypeIdProvider(), thm);

			// finally bundle everything together in the actual instance
			return new EmbeddedStorageManager.Implementation(stm.configuration(), ecf, prp.provideRoots(rootResolver));
		}

	}

}
