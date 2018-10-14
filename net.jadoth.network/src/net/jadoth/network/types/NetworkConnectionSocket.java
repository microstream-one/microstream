package net.jadoth.network.types;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.exceptions.IORuntimeException;

public interface NetworkConnectionSocket
{
	public SocketChannel acceptConnection();



	public final class Implementation implements NetworkConnectionSocket
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ServerSocketChannel socketChannel;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Implementation(final ServerSocketChannel socketChannel)
		{
			super();
			if(!socketChannel.isOpen())
			{
				throw new IllegalArgumentException("Channel not open");
			}
			if(!socketChannel.isBlocking())
			{
				throw new IllegalArgumentException("Non-blocking server socket channel not supported");
			}
			this.socketChannel = socketChannel;
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public SocketChannel acceptConnection()
		{
			try
			{
				return this.socketChannel.accept();
			}
			// not sure if this is needed if calling context is just checking interrupted()
//			catch(final ClosedByInterruptException e)
//			{
//				throw new InterruptedRuntimeException(new InterruptedException());
//			}
			catch(final IOException e)
			{
				// sorry but clean architecture and werid checked exception aren't really compatible.
				throw new IORuntimeException(e);
			}
		}

	}

}
