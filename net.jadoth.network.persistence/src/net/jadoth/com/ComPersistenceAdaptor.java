package net.jadoth.com;

import net.jadoth.persistence.types.PersistenceFoundation;
import net.jadoth.persistence.types.PersistenceManager;
import net.jadoth.persistence.types.PersistenceTypeDictionaryCompiler;
import net.jadoth.persistence.types.PersistenceTypeDictionaryView;
import net.jadoth.persistence.types.PersistenceTypeDictionaryViewProvider;


public interface ComPersistenceAdaptor<C> extends PersistenceTypeDictionaryViewProvider
{
	@Override
	public default PersistenceTypeDictionaryView provideTypeDictionary()
	{
		// initialization is checked to be done only once.
		return this.provideHostPersistenceFoundation(null).getTypeHandlerManager()
			.initialize()
			.typeDictionary()
			.view()
		;
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
		
}
