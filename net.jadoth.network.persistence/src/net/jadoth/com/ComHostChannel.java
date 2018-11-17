package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComHostChannel<C> extends ComChannel
{
	public C connection();

	
	
	public static <C> ComHostChannel<C> New(
		final C                     connection        ,
		final PersistenceManager<?> persistenceManager
	)
	{
		return new ComHostChannel.Implementation<>(
			notNull(connection)        ,
			notNull(persistenceManager)
		);
	}
	
	public final class Implementation<C>
	extends ComChannel.Implementation
	implements ComHostChannel<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C connection;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(final C connection, final PersistenceManager<?> persistenceManager)
		{
			super(persistenceManager);
			this.connection = connection;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final C connection()
		{
			return this.connection;
		}
		
	}
	
}
