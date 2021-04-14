package one.microstream.communication.types;

import java.net.InetSocketAddress;

@FunctionalInterface
public interface ComHostCreator<C>
{
	public ComHost<C> createComHost(
		InetSocketAddress        address           ,
		ComConnectionHandler<C>  connectionHandler ,
		ComConnectionAcceptor<C> connectionAcceptor
	);

	
	
	public static <C> ComHostCreator<C> New()
	{
		return new ComHostCreator.Default<>();
	}
	
	public final class Default<C> implements ComHostCreator<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default()
		{
			super();
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComHost<C> createComHost(
			final InetSocketAddress        address           ,
			final ComConnectionHandler<C>  connectionHandler ,
			final ComConnectionAcceptor<C> connectionAcceptor
		)
		{
			return ComHost.New(address, connectionHandler, connectionAcceptor);
		}
		
	}
	
}
