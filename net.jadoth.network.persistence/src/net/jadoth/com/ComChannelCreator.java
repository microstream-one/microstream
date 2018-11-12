package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComChannelCreator<C>
{
	public ComChannel createChannel(C connection);
	
	
	
	public static <C> ComChannelCreator<C> New(final ComPersistenceAdaptor<C> persistenceAdaptor)
	{
		return new ComChannelCreator.Implementation<>(
			notNull(persistenceAdaptor)
		);
	}
	
	public final class Implementation<C> implements ComChannelCreator<C>
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
		public ComChannel createChannel(final C connection)
		{
			final PersistenceManager<?> pm = this.persistenceAdaptor.providePersistenceManager(connection);
			
			return ComChannel.New(pm);
		}
		
	}
		
}
