package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.nio.channels.SocketChannel;

import net.jadoth.persistence.types.PersistenceManager;


/**
 * Fancily named usability wrapper for a {@link PersistenceManager} in the context of a network connection.
 * 
 * @author TM
 */
public interface ComChannel
{
	public Object receive();
	
	public void send(Object graphRoot);
	
	
	
	
	public static ComChannelCreator<SocketChannel> CreatorForSocketChannel()
	{
		return ComChannelCreator.NewForSocketChannel();
	}
	
	public static ComChannel New(final PersistenceManager<?> persistenceManager)
	{
		return new Implementation(
			notNull(persistenceManager)
		);
	}
	
	public final class Implementation implements ComChannel
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final PersistenceManager<?> persistenceManager;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final PersistenceManager<?> persistenceManager)
		{
			super();
			this.persistenceManager = persistenceManager;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final void send(final Object graphRoot)
		{
			/*
			 * "store" is a little unfitting here.
			 * However, technically, it is correct. The graph is "stored" (written) to the network connection.
			 */
			this.persistenceManager.store(graphRoot);
		}
		
		@Override
		public final Object receive()
		{
			/*
			 * in the context of a network connection, the generic get() means
			 * receive whatever the other side is sending.
			 */
			return this.persistenceManager.get();
		}
		
	}
	
}
