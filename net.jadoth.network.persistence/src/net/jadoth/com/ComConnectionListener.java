package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
	
	
	public static ComConnectionListenerCreator.Default Creator()
	{
		return ComConnectionListenerCreator.New();
	}
	
	public static ComConnectionListener.Default New(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public final class Default implements ComConnectionListener<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public SocketChannel listenForConnection()
		{
			final SocketChannel socketChannel;
			try
			{
				socketChannel = this.serverSocketChannel.accept();
			}
			catch(final Exception e)
			{
				// (12.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
			
			return socketChannel;
		}

		@Override
		public void close()
		{
			try
			{
				this.serverSocketChannel.close();
			}
			catch(final Exception e)
			{
				// (12.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
