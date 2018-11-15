package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public interface ComConnectionHandler<C>
{
	public C openConnection(InetSocketAddress address);
	
	public void prepareReading(C connection);
	
	public void prepareWriting(C connection);
	
	public void close(C connection);
	
	public void closeReading(C connection);
	
	public void closeWriting(C connection);
	
	public void read(C connction, ByteBuffer buffer);
	
	public void write(C connction, ByteBuffer buffer);
	
	public default void writeChunk(
		final C             connection  ,
		final ByteBuffer    headerBuffer,
		final ByteBuffer[]  buffers
	)
	{
		this.write(connection, headerBuffer);
		
		for(final ByteBuffer buffer : buffers)
		{
			this.write(connection, buffer);
		}
	}
	
	public void sendProtocol(C connection, ComProtocol protocol, ComProtocolStringConverter stringConverter);
	
	public ComProtocol receiveProtocol(C connection, ComProtocolStringConverter stringConverter);
	
	public ComHostContext.Builder<C> createHostContextBuilder();
	
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
	
	
	public static ComConnectionHandler.Default Default()
	{
		return new ComConnectionHandler.Default();
	}
	
	public final class Default implements ComConnectionHandler<SocketChannel>
	{
		@Override
		public SocketChannel openConnection(final InetSocketAddress address)
		{
			return XSockets.openChannel(address);
		}

		@Override
		public void prepareReading(final SocketChannel connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void prepareWriting(final SocketChannel connection)
		{
			// no preparation needed for SocketChannel instances
		}

		@Override
		public void close(final SocketChannel connection)
		{
			XSockets.closeChannel(connection);
		}

		@Override
		public void closeReading(final SocketChannel connection)
		{
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void closeWriting(final SocketChannel connection)
		{
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void read(final SocketChannel connction, final ByteBuffer buffer)
		{
			throw new net.jadoth.meta.NotImplementedYetError(); // FIXME ComConnectionLogic<SocketChannel>#read()
		}

		@Override
		public void write(final SocketChannel connction, final ByteBuffer buffer)
		{
			XSockets.writeCompletely(connction, buffer);
		}
		
		@Override
		public void sendProtocol(
			final SocketChannel              connection     ,
			final ComProtocol                protocol       ,
			final ComProtocolStringConverter stringConverter
		)
		{
			final ByteBuffer bufferedProtocol = Com.bufferProtocol(protocol, stringConverter);
			this.write(connection, bufferedProtocol);
		}
		
		@Override
		public ComProtocol receiveProtocol(
			final SocketChannel              connection     ,
			final ComProtocolStringConverter stringConverter
		)
		{
			// FIXME ComConnectionHandler.Default#receiveProtocol()
			throw new net.jadoth.meta.NotImplementedYetError();
		}
		
		@Override
		public ComHostContext.Builder<SocketChannel> createHostContextBuilder()
		{
			return ComHostContext.<SocketChannel>Builder();
		}

		@Override
		public ComConnectionListener<SocketChannel> createConnectionListener(final InetSocketAddress address)
		{
			final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
			
			return new ComConnectionListener<SocketChannel>()
			{
				@Override
				public SocketChannel listenForConnection()
				{
					return XSockets.acceptSocketChannel(serverSocketChannel);
				}

				@Override
				public void close()
				{
					XSockets.closeChannel(serverSocketChannel);
				}
			};
		}
		
	}
	
}
