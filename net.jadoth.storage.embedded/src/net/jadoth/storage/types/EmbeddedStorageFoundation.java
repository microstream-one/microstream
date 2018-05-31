package net.jadoth.storage.types;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceRoots;
import net.jadoth.persistence.types.PersistenceRootsProvider;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface EmbeddedStorageFoundation extends StorageFoundation
{
	public EmbeddedStorageConnectionFoundation getConnectionFoundation();

	public EmbeddedStorageManager createEmbeddedStorageManager();

	
	
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

	public EmbeddedStorageFoundation setConnectionFoundation(EmbeddedStorageConnectionFoundation connectionFoundation);

	public EmbeddedStorageFoundation setRootResolver(PersistenceRootResolver rootResolver);



	public class Implementation extends StorageFoundation.Implementation implements EmbeddedStorageFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private EmbeddedStorageConnectionFoundation connectionFoundation;

		

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
			final EmbeddedStorageConnectionFoundation escf          = this.getConnectionFoundation();
			final PersistenceRootsProvider<Binary>    rootsProvider = escf.getRootsProvider();
			
			// the genericness of this :D (albeit #provideRoots is implicitly assumed to cache the instance)
			return EmbeddedStorageRootTypeIdProvider.New(
				rootsProvider.provideRoots().getClass()
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
		public EmbeddedStorageFoundation setRootResolver(final PersistenceRootResolver rootResolver)
		{
			this.connectionFoundation.setRootResolver(rootResolver);
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
		
		@Override
		public synchronized EmbeddedStorageManager createEmbeddedStorageManager()
		{
			// this is all a bit of clumsy detour due to conflicted initialization order. Maybe overhaul.

			final EmbeddedStorageConnectionFoundation ecf = this.getConnectionFoundation();
			final PersistenceTypeHandlerManager<?>    thm = ecf.getTypeHandlerManager();
			
			final StorageManager stm = this.createStorageManager();
			ecf.setStorageManager(stm);

			// initialize persistence (=binary) type handler manager (validate and ensure type handlers)
			thm.initialize();
			
			// type storage dictionary updating moved here as well to keep all nasty parts at one place ^^.
			final StorageTypeDictionary std = stm.typeDictionary();
			std
			.initialize(ecf.getTypeDictionaryProvider().provideTypeDictionary())
			.setTypeDescriptionRegistrationObserver(std)
			;

			// resolve root types to root type ids after types have been initialized
			this.initializeEmbeddedStorageRootTypeIdProvider(this.getRootTypeIdProvider(), thm);

			// the roots instance to be used
			final PersistenceRootsProvider<Binary> prp = ecf.getRootsProvider();
			final PersistenceRoots roots = prp.provideRoots();
				
			// everything bundled together in the actual manager instance
			return EmbeddedStorageManager.New(stm.configuration(), ecf, roots);
		}

	}

}
