package net.jadoth.com;

import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryCompiler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;
import net.jadoth.swizzling.types.SwizzleIdStrategy;


public interface ComPersistenceAdaptor<C> extends PersistenceTypeDictionaryViewProvider
{
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		/* (19.11.2018 TM)FIXME: default provideTypeDictionary
		 * must do something along the line of the following code, but different:
		 * - create a new PersistenceFoundation of appropriate type (e.g. binary)
		 * - set a transient type dictionary manager
		 * - set a transient typeid provider
		 * - get its TypeHandlerManager and initialize (filling it with default type handlers and TypeIds)
		 * - register an arbitrary set of classes
		 * - get the completed TypeDictionary from that
		 * - construct a TypeDictionaryView (immure the TypeDictionary, important!)
		 * - return it, discarding the foundation with its mutable TypeDictionary and TypeId Provider
		 */
//		final BinaryPersistenceFoundation<?> pf = BinaryPersistence.Foundation()
//			.setTypeDictionaryIoHandler(PersistenceTypeDictionaryFileHandler.NewInDirecoty(
//				XFiles.ensureDirectory(new File("TypeDictionary"))
//			))
//			.setObjectIdProvider(SwizzleObjectIdProvider.Transient())
//			.setTypeIdProvider(SwizzleTypeIdProvider.Transient())
//		;
		
		// initialization is checked to be done only once.
		return this.provideHostPersistenceFoundation(null).getTypeHandlerManager()
			.initialize()
			.typeDictionary()
			.view()
		;
	}
	
	public default SwizzleIdStrategy provideHostIdStrategy()
	{
		return Com.DefaultIdStrategyHost();
	}
	
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
	
	public default ComPersistenceAdaptor<C> initializePersistenceFoundation(
		final PersistenceTypeDictionaryViewProvider typeDictionaryProvider,
		final SwizzleIdStrategy                     idStrategy
	)
	{
		final PersistenceTypeDictionaryManager typeDictionaryManager =
			PersistenceTypeDictionaryManager.Immutable(typeDictionaryProvider)
		;
		
		final PersistenceFoundation<?, ?> foundation = this.persistenceFoundation();
		foundation.setTypeDictionaryManager(typeDictionaryManager);
		foundation.setObjectIdProvider     (idStrategy.createObjectIdProvider());
		foundation.setTypeIdProvider       (idStrategy.createTypeIdProvider());
		
		return this;
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
	
	public default ComPersistenceAdaptor<C> initializeClientPersistenceFoundation(
		final ComProtocol protocol
	)
	{
		this.initializePersistenceFoundation(protocol, protocol.idStrategy());
		return this;
	}
	
	public default ComPersistenceAdaptor<C> initializeHostPersistenceFoundation()
	{
		// (19.11.2018 TM)FIXME: set IdStrategies and TypeDictionaryViewProvider
		throw new net.jadoth.meta.NotImplementedYetError();
	}
	
	
	
		
}
