package one.microstream.communication.types;

import static one.microstream.X.notNull;

import one.microstream.persistence.types.PersistenceManager;

public interface ComHostChannel<C> extends ComChannel
{
	public C connection();
	
	public ComProtocol protocol();
	
	public ComHost<C> parent();

	
	
	public static <C> ComHostChannel<C> New(
		final PersistenceManager<?> persistenceManager,
		final C                     connection        ,
		final ComProtocol           protocol          ,
		final ComHost<C>            parent
	)
	{
		return new ComHostChannel.Default<>(
			notNull(persistenceManager),
			notNull(connection)        ,
			notNull(protocol)          ,
			notNull(parent)
		);
	}
	
	public final class Default<C>
	extends ComChannel.Default
	implements ComHostChannel<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C           connection;
		private final ComProtocol protocol  ;
		private final ComHost<C>  parent    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceManager<?> persistenceManager,
			final C                     connection        ,
			final ComProtocol           protocol          ,
			final ComHost<C>            parent
		)
		{
			super(persistenceManager);
			this.connection = connection;
			this.protocol   = protocol  ;
			this.parent     = parent    ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final C connection()
		{
			return this.connection;
		}
		
		@Override
		public final ComProtocol protocol()
		{
			return this.protocol;
		}
		
		@Override
		public final ComHost<C> parent()
		{
			return this.parent;
		}
		
	}
	
}
