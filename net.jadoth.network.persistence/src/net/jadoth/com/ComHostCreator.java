package net.jadoth.com;

import java.net.InetSocketAddress;

public interface ComHostCreator<C>
{
	public ComHost<C> createComHost(
		InetSocketAddress               address                  ,
		ComConnectionListenerCreator<C> connectionListenerCreator,
		ComConnectionAcceptor<C>        connectionAcceptor
	);

	
	
	public static <C> ComHostCreator<C> New()
	{
		return new ComHostCreator.Implementation<>();
	}
	
	public final class Implementation<C> implements ComHostCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComHost<C> createComHost(
			final InetSocketAddress               address                  ,
			final ComConnectionListenerCreator<C> connectionListenerCreator,
			final ComConnectionAcceptor<C>        connectionAcceptor
		)
		{
			return ComHost.New(address, connectionListenerCreator, connectionAcceptor);
		}
		
	}
	
}
