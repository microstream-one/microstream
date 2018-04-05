package net.jadoth.storage.types;

import java.lang.reflect.Field;
import java.util.function.Predicate;

import net.jadoth.collections.X;
import net.jadoth.hash.JadothHash;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.types.PersistenceRootResolver;
import net.jadoth.persistence.types.PersistenceTypeHandlerManager;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.SwizzleObjectIdProvider;
import net.jadoth.swizzling.types.SwizzleTypeManager;

public interface EmbeddedStorageFoundation extends StorageFoundation
{
	///////////////////////////////////////////////////////////////////////////
	// getters         //
	/////////////////////

	public EmbeddedStorageConnectionFoundation getConnectionFoundation();

	public EmbeddedStorageFoundation registerRootInstance(String identifier, Object instance);

	public EmbeddedStorageFoundation registerRootConstants(Class<?>... types);

	public EmbeddedStorageFoundation registerRootConstants(Class<?>[] types, Predicate<? super Field> predicate);

	public PersistenceRootResolver getRootResolver();

	public BinaryPersistenceRootsProvider getRootsProvider();

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
	public EmbeddedStorageFoundation setStorageTimestampProvider(StorageTimestampProvider storageTimestampProvider);

	@Override
	public EmbeddedStorageFoundation setRootTypeIdProvider(StorageRootTypeIdProvider rootTypeIdProvider);

	public EmbeddedStorageFoundation setRootResolver(PersistenceRootResolver rootResolver);

	public EmbeddedStorageFoundation setRootsProvider(BinaryPersistenceRootsProvider rootsProvider);

	public EmbeddedStorageFoundation setConnectionFoundation(EmbeddedStorageConnectionFoundation connectionFoundation);





	public class Implementation extends StorageFoundation.Implementation implements EmbeddedStorageFoundation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields  //
		/////////////////////

		private PersistenceRootResolver             rootResolver        ;
		private BinaryPersistenceRootsProvider      rootsProvider       ;
		private EmbeddedStorageConnectionFoundation connectionFoundation;



		///////////////////////////////////////////////////////////////////////////
		// declared methods //
		/////////////////////

		protected PersistenceRootResolver createRootResolver()
		{
			return PersistenceRootResolver.New();
		}

		protected BinaryPersistenceRootsProvider createRootsProvider()
		{
			return new BinaryPersistenceRootsProvider.Implementation();
		}

		protected final void internalSetConnectionFoundation(
			final EmbeddedStorageConnectionFoundation connectionFoundation
		)
		{
			this.connectionFoundation = connectionFoundation;
		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		protected EmbeddedStorageConnectionFoundation createConnectionFoundation()
		{
			return new EmbeddedStorageConnectionFoundation.Implementation();
		}

		@Override
		protected EmbeddedStorageRootTypeIdProvider createRootTypeIdProvider()
		{
			// the genericity of this knocks me out :D
			return EmbeddedStorageRootTypeIdProvider.New(
				this.getRootsProvider().provideRoots().getClass()
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
		public final EmbeddedStorageFoundation registerRootInstance(final String identifier, final Object instance)
		{
			this.getRootsProvider().provideRoots().entries().put(identifier, instance);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation registerRootConstants(final Class<?>... types)
		{
			// default predicate: all non-transient reference constants
			return this.registerRootConstants(
				types,
				((Predicate<Field>)JadothReflect::isFinalField)
				.and(JadothReflect::isNotTransient)
			);
		}

		@Override
		public EmbeddedStorageFoundation.Implementation registerRootConstants(
			final Class<?>[]               types    ,
			final Predicate<? super Field> predicate
		)
		{
			/* better not trust custom predicates:
			 * - field MUST be static, otherwise no instance can be safely retrieved in a static way
			 * - field MUST be a reference field, because registering primitives is not possible and reasonable
			 */
			final Predicate<? super Field> safePredicate =
				((Predicate<Field>)JadothReflect::isStatic)
				.and(JadothReflect::isReferenceField)
				.and(predicate)
			;

			// cache resolver instance locally
			final PersistenceRootResolver rootResolver = this.getRootResolver();

			// loop through all the stuff and register root instances of applicable fields
			for(final Class<?> type : types)
			{
				for(final Field field : type.getDeclaredFields())
				{
					if(!safePredicate.test(field))
					{
						continue;
					}
					this.registerRootInstance(
						rootResolver.deriveIdentifier(field),
						Memory.getStaticReference(field)
					);
				}
			}
			return this;
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
		public BinaryPersistenceRootsProvider getRootsProvider()
		{
			if(this.rootsProvider == null)
			{
				this.rootsProvider = this.dispatch(this.createRootsProvider());
			}
			return this.rootsProvider;
		}

		protected void internalSetRootResolver(final PersistenceRootResolver rootResolver)
		{
			this.rootResolver = rootResolver;
		}

		protected final void internalSetRootsProvider(final BinaryPersistenceRootsProvider rootsProvider)
		{
			this.rootsProvider = rootsProvider;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRootResolver(final PersistenceRootResolver rootResolver)
		{
			this.internalSetRootResolver(rootResolver);
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
			this.internalSetConfiguration(configuration);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRequestAcceptorCreator(
			final StorageRequestAcceptor.Creator requestAcceptorCreator
		)
		{
			this.internalSetRequestAcceptorCreator(requestAcceptorCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTaskBrokerCreator(
			final StorageTaskBroker.Creator taskBrokerCreator
		)
		{
			this.internalSetTaskBrokerCreator(taskBrokerCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setDataChunkValidatorProvider(
			final StorageValidatorDataChunk.Provider dataChunkValidatorProvider
		)
		{
			this.internalSetDataChunkValidatorProvider(dataChunkValidatorProvider);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setChannelCreator(final StorageChannel.Creator channelCreator)
		{
			this.internalSetChannelCreator(channelCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTaskCreator(final StorageRequestTaskCreator taskCreator)
		{
			this.internalSetTaskCreator(taskCreator);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setTypeDictionary(final StorageTypeDictionary typeDictionary)
		{
			this.internalSetTypeDictionary(typeDictionary);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setRootTypeIdProvider(
			final StorageRootTypeIdProvider rootTypeIdProvider
		)
		{
			this.internalSetRootTypeIdProvider(rootTypeIdProvider);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation.Implementation setConnectionFoundation(
			final EmbeddedStorageConnectionFoundation connectionFoundation
		)
		{
			this.internalSetConnectionFoundation(connectionFoundation);
			return this;
		}

		@Override
		public EmbeddedStorageFoundation setRootsProvider(final BinaryPersistenceRootsProvider rootsProvider)
		{
			this.internalSetRootsProvider(rootsProvider);
			return this;
		}


		@Override
		public EmbeddedStorageFoundation.Implementation setStorageTimestampProvider(
			final StorageTimestampProvider storageTimestampProvider
		)
		{
			this.internalSetTimestampProvider(storageTimestampProvider);
			return this;
		}

		/**
		 * Registers all necessary system constants to guarantee referential integrity and up-do-date data
		 * when initializing the storage (instance data from the storage).
		 * <p>
		 * There are two typical use-cases:
		 * <ul>
		 * <li>Registration of global stateless function instances (e.g. {@link JadothHash#hashEqualityValue()})
		 * for purposes of referential integrity.</li>
		 * <li>Registration of application-specific entity graph root instances that get updated (loaded) via
		 * storage initialization</li>
		 * </ul>
		 */
		protected void registerRootSystemConstants()
		{
			/* (19.01.2015 TM)TODO: register JDK constants
			 * JDK constants like Integer range etc. must be registered and stored as well.
			 * They are not necessary for correct use with a Java application as the constants
			 * are associated correctly in the OID registry on every start, however they are necessary
			 * to guarantee a complete and consistent persistent form.
			 * For example, currently the cached Integer constant instance with value 0 never gets stored
			 * as it is already known to the BinaryStorer (being a constant).
			 */

			this.registerRootConstants(
				JadothHash.class, // hash equalators
				X         .class  // empties
			);

			/* if the resolver knows any explicit roots, register them right away.
			 * Not absolutely mandatory but saves redundant code in use sites (application root logic)
			 */
			this.getRootResolver().iterateEntries(e ->
				this.registerRootInstance(e.key(), e.value())
			);
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

			/* (13.09.2015)TODO: StorageEntityTypeHandlerCreator for storage-side lazy ref handling
			 * link PersistenceTypeHandlerManager and to-be-created StorageEntityTypeHandlerCreator
			 * in order to have a way for the entity type handler creation recognize lazy references.
			 * Required for storage-side graph deep-reference loading.
			 */
			final StorageManager                      stm = this.createStorageManager();
			ecf.setStorageManager(stm);

			final BinaryPersistenceRootsProvider      prp = this.getRootsProvider();

			// register root system constants like equalators etc. to guarantee referential integrity
			this.registerRootSystemConstants();

			// register special case type handler for roots instance
			prp.registerRootsTypeHandlerCreator(
				ecf.getCustomTypeHandlerRegistry(),
				ecf.getSwizzleRegistry(),
				this.getRootResolver()
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
			return new EmbeddedStorageManager.Implementation(stm.configuration(), ecf, prp.provideRoots());
		}

	}

}
