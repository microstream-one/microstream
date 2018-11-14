package net.jadoth.com;

import static net.jadoth.X.notNull;

import net.jadoth.persistence.types.PersistenceManager;

public interface ComClientChannel extends ComChannel
{
	public ComProtocol protocol();
	
	public ComClient parent();
	
	
	
	public static ComClientChannel New(
		final PersistenceManager<?> persistenceManager,
		final ComProtocol           protocol          ,
		final ComClient             parent
	)
	{
		return new ComClientChannel.Implementation(
			notNull(persistenceManager),
			notNull(protocol)          ,
			notNull(parent)
		);
	}
	
	public final class Implementation extends ComChannel.Implementation implements ComClientChannel
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocol protocol;
		private final ComClient   parent  ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final PersistenceManager<?> persistenceManager,
			final ComProtocol           protocol          ,
			final ComClient             parent
		)
		{
			super(persistenceManager);
			this.protocol = protocol;
			this.parent   = parent  ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final ComProtocol protocol()
		{
			return this.protocol;
		}

		@Override
		public final ComClient parent()
		{
			return this.parent;
		}
		
	}
	
}
