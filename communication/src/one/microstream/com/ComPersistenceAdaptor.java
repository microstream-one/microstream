package one.microstream.com;

import java.nio.ByteOrder;
import java.util.function.Consumer;

import one.microstream.collections.types.XGettingEnum;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFoundation;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.persistence.types.PersistenceTypeDictionary;
import one.microstream.persistence.types.PersistenceTypeDictionaryCompiler;
import one.microstream.persistence.types.PersistenceTypeDictionaryManager;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.persistence.types.PersistenceTypeDictionaryViewProvider;
import one.microstream.persistence.types.PersistenceTypeHandlerManager;


public interface ComPersistenceAdaptor<C> extends PersistenceTypeDictionaryViewProvider
{
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		final PersistenceFoundation<?, ?> initFoundation = this.createInitializationFoundation();
		
		initFoundation.setTypeDictionaryManager(
			PersistenceTypeDictionaryManager.Transient(
				initFoundation.getTypeDictionaryCreator()
			)
		);
		
		final PersistenceIdStrategy idStrategy = this.hostInitializationIdStrategy();
		initFoundation.setObjectIdProvider(idStrategy.createObjectIdProvider());
		initFoundation.setTypeIdProvider(idStrategy.createTypeIdProvider());

		final PersistenceTypeHandlerManager<?> thm = initFoundation.getTypeHandlerManager();
		thm.initialize();
		
		this.iterateEntityTypes(c ->
			thm.ensureTypeHandler(c)
		);
		
		final PersistenceTypeDictionary typeDictionary = thm.typeDictionary();
		
		final PersistenceTypeDictionaryView typeDictionaryView = typeDictionary.view();
		
		return typeDictionaryView;
	}
	
	public PersistenceFoundation<?, ?> createInitializationFoundation();
	
	public void iterateEntityTypes(final Consumer<? super Class<?>> iterator);
	
	public PersistenceIdStrategy hostInitializationIdStrategy();
	
	public PersistenceIdStrategy hostIdStrategy();
	
	public ByteOrder hostByteOrder();
	
	public default PersistenceTypeDictionaryCompiler provideTypeDictionaryCompiler()
	{
		return this.provideHostPersistenceFoundation(null)
			.getTypeDictionaryCompiler()
		;
	}
	
	/**
	 * Might return the same instance for all connections or the same for every unique client or a new instance on
	 * every call. Depends on the use-case.<br>
	 * The persistence medium type used by the persistence manager is irrelevant on the com-level, hence the "?".
	 * 
	 * @param connection
	 * @return
	 */
	public default PersistenceManager<?> provideHostPersistenceManager(
		final C connection
	)
	{
		return this.provideHostPersistenceFoundation(connection)
			.createPersistenceManager()
		;
	}
	
	public default PersistenceManager<?> provideClientPersistenceManager(
		final C           connection,
		final ComProtocol protocol
	)
	{
		return this.provideClientPersistenceFoundation(connection, protocol)
			.createPersistenceManager()
		;
	}
	
	/**
	 * Provides a {@link PersistenceFoundation} instance prepared for the passed connection instance.
	 * The passed connection instance might be null, in which case the returned foundation instance
	 * can only be used for general, non-communication-related operations.<p>
	 * See {@link #providePersistenceManager(C)} with a passed non-null connection instance.<br>
	 * See {@link #provideTypeDictionaryCompiler(C)} with a passed null connection instance.
	 * 
	 * @param connection
	 * @return
	 * 
	 * @see #providePersistenceManager(C)
	 * @see #provideTypeDictionaryCompiler()
	 */
	public PersistenceFoundation<?, ?> provideHostPersistenceFoundation(C connection);

	public PersistenceFoundation<?, ?> provideClientPersistenceFoundation(C connection, ComProtocol protocol);
	
	public default PersistenceFoundation<?, ?> initializePersistenceFoundation(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final ByteOrder                             targetByteOrder       ,
		final PersistenceIdStrategy                 idStrategy
	)
	{
		final PersistenceTypeDictionaryManager typeDictionaryManager =
			PersistenceTypeDictionaryManager.Immutable(typeDictionaryProvider)
		;
		
		final PersistenceFoundation<?, ?> foundation = this.persistenceFoundation();
		foundation.setTypeDictionaryManager(typeDictionaryManager);
		foundation.setTargetByteOrder      (targetByteOrder);
		foundation.setObjectIdProvider     (idStrategy.createObjectIdProvider());
		foundation.setTypeIdProvider       (idStrategy.createTypeIdProvider());
		
		/*
		 * Communication differs from Storing in some essential details, so the OGS Legacy Type Mapping
		 * is not applicable here.
		 * Also see descriptions in Issue MS-46. At some point in the future, a OGC-suitable type mapping
		 * will probably become necessary. Until then, type mismatches are invalid.
		 * The rationale behind this decission is that properly matching types on both sides must be established
		 * at the time the connction is established, so BEFORE any data has been transmitted. On the fly type
		 * mapping, be it dynamically new or legacy, can cause unresolvable problems if the other peer does not
		 * have corresponding types (classes). Such a problem better be recognized sooner rather than later.
		 */
		foundation.setTypeMismatchValidator(Persistence.typeMismatchValidatorFailing());
		
		return foundation;
	}
	
	public default ComHostChannel<C> createHostChannel(
		final C           connection,
		final ComProtocol protocol  ,
		final ComHost<C>  parent
	)
	{
		final PersistenceManager<?> pm = this.provideHostPersistenceManager(connection);
		
		return ComHostChannel.New(pm, connection, protocol, parent);
	}
	
	public default ComClientChannel<C> createClientChannel(
		final C            connection,
		final ComProtocol  protocol  ,
		final ComClient<C> parent
	)
	{
		final PersistenceManager<?> pm = this.provideClientPersistenceManager(connection, protocol);
		
		return ComClientChannel.New(pm, connection, protocol, parent);
	}
	
	public PersistenceFoundation<?, ?> persistenceFoundation();
	
	public default PersistenceFoundation<?, ?> initializeClientPersistenceFoundation(
		final ComProtocol protocol
	)
	{
		return this.initializePersistenceFoundation(protocol, protocol.byteOrder(), protocol.idStrategy());
	}
	
	public default ComPersistenceAdaptor<C> initializeHostPersistenceFoundation()
	{
		final PersistenceTypeDictionaryView typeDictionary = this.provideTypeDictionary();
		
		this.initializePersistenceFoundation(
			PersistenceTypeDictionaryViewProvider.Wrapper(typeDictionary),
			this.hostByteOrder(),
			this.hostIdStrategy()
		);
		
		return this;
	}
	
	
	public abstract class Abstract<C> implements ComPersistenceAdaptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceIdStrategy  hostInitIdStrategy;
		private final XGettingEnum<Class<?>> entityTypes       ;
		private final ByteOrder              hostByteOrder     ;
		private final PersistenceIdStrategy  hostIdStrategy    ;
		
		private transient PersistenceTypeDictionaryView cachedTypeDictionary     ;
		private transient boolean                       initializedHostFoundation;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Abstract(
			final PersistenceIdStrategy  hostInitIdStrategy,
			final XGettingEnum<Class<?>> entityTypes       ,
			final ByteOrder              hostByteOrder     ,
			final PersistenceIdStrategy  hostIdStrategy
		)
		{
			super();
			this.hostInitIdStrategy = hostInitIdStrategy;
			this.entityTypes        = entityTypes       ;
			this.hostByteOrder      = hostByteOrder     ;
			this.hostIdStrategy     = hostIdStrategy    ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public PersistenceIdStrategy hostIdStrategy()
		{
			return this.hostIdStrategy;
		}
		
		@Override
		public ByteOrder hostByteOrder()
		{
			return this.hostByteOrder;
		}
		
		@Override
		public PersistenceIdStrategy hostInitializationIdStrategy()
		{
			return this.hostInitIdStrategy;
		}
		
		@Override
		public void iterateEntityTypes(final Consumer<? super Class<?>> iterator)
		{
			this.entityTypes.iterate(iterator);
		}
		
		@Override
		public PersistenceTypeDictionaryView provideTypeDictionary()
		{
			if(this.cachedTypeDictionary == null)
			{
				synchronized(this)
				{
					// recheck after synch
					if(this.cachedTypeDictionary == null)
					{
						this.cachedTypeDictionary = ComPersistenceAdaptor.super.provideTypeDictionary();
					}
				}
			}
			
			return this.cachedTypeDictionary;
		}
		
		@Override
		public ComPersistenceAdaptor.Abstract<C> initializeHostPersistenceFoundation()
		{
			if(!this.initializedHostFoundation)
			{
				synchronized(this)
				{
					// recheck after synch
					if(!this.initializedHostFoundation)
					{
						ComPersistenceAdaptor.super.initializeHostPersistenceFoundation();
						this.initializedHostFoundation = true;
					}
				}
			}
			
			return this;
		}
		
	}
		
}
