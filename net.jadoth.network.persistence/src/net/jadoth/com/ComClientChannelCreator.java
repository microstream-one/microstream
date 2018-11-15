package net.jadoth.com;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComClientChannelCreator<C>
{
	public ComClientChannel createChannel(
		C           connection,
		ComProtocol protocol  ,
		ComClient   parent
	);
		
	
	
	public abstract class Abstract<C> implements ComClientChannelCreator<C>
	{
		protected abstract PersistenceManager<?> createPersistenceManager(
			C           connection,
			ComProtocol protocol
		);
		
		
		@Override
		public ComClientChannel createChannel(
			final C           connection,
			final ComProtocol protocol  ,
			final ComClient   parent
		)
		{
			final PersistenceManager<?> persistenceManager = this.createPersistenceManager(connection, protocol);
			
			return ComClientChannel.New(persistenceManager, protocol, parent);
		}
		
	}
	
}
