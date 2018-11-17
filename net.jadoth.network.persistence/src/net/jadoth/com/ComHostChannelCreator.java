package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComHostChannelCreator<C>
{
	public ComHostChannel<C> createChannel(C connection);
	
	
	
	public static <C> ComHostChannelCreator<C> New(final ComPersistenceAdaptor<C> persistenceAdaptor)
	{
		return new ComHostChannelCreator.Implementation<>(
			notNull(persistenceAdaptor)
		);
	}
	
	public final class Implementation<C> implements ComHostChannelCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComPersistenceAdaptor<C> persistenceAdaptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(final ComPersistenceAdaptor<C> persistenceAdaptor)
		{
			super();
			this.persistenceAdaptor = persistenceAdaptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComHostChannel<C> createChannel(final C connection)
		{
			final PersistenceManager<?> pm = this.persistenceAdaptor.providePersistenceManager(connection);
			
			return ComHostChannel.New(connection, pm);
		}
		
	}
		
}
