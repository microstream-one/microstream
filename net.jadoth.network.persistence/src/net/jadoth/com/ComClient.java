package net.jadoth.com;

import java.net.InetSocketAddress;

import net.jadoth.persistence.types.PersistenceFoundation;

public interface ComClient
{
	public ComClientChannel connect() throws ComException;
	
	
	
	public abstract class Abstract<C> implements ComClient
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress           hostAddress          ;
		private final PersistenceFoundation<?, ?> persistenceFoundation;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Abstract(
			final InetSocketAddress           hostAddress          ,
			final PersistenceFoundation<?, ?> persistenceFoundation
		)
		{
			super();
			this.hostAddress           = hostAddress          ;
			this.persistenceFoundation = persistenceFoundation;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComClientChannel connect() throws ComException
		{
			XSockets.
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComClient#connect()
		}
		
	}
}
