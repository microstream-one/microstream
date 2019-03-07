package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import net.jadoth.chars._charArrayRange;
import net.jadoth.files.XFiles;

public interface ComConnectionHandler<C>
{
	public ComConnectionListener<C> createConnectionListener(InetSocketAddress address);
	
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
		
	
	
	
	public static ComConnectionHandler.Default Default()
	{
		return new ComConnectionHandler.Default();
	}
	
	public final class Default implements ComConnectionHandler<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		final int protocolLengthDigitCount = Com.defaultProtocolLengthDigitCount();
				
		
		
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
		public ComConnectionListener<SocketChannel> createConnectionListener(
			final InetSocketAddress address
		)
		{
			final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
			
			return ComConnectionListener.Default(serverSocketChannel);
		}
		
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
			// (17.11.2018 TM)TODO: SocketChannel#shutdownInput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void closeWriting(final SocketChannel connection)
		{
			// (17.11.2018 TM)TODO: SocketChannel#shutdownOutput ?
			
			// SocketChannel#close is idempotent
			this.close(connection);
		}

		@Override
		public void read(final SocketChannel connction, final ByteBuffer buffer)
		{
			XSockets.readCompletely(connction, buffer);
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
			final ByteBuffer bufferedProtocol = Com.bufferProtocol(
				protocol                     ,
				stringConverter              ,
				this.protocolLengthDigitCount
			);
			
			this.write(connection, bufferedProtocol);
		}
		
		@Override
		public ComProtocol receiveProtocol(
			final SocketChannel              connection     ,
			final ComProtocolStringConverter stringConverter
		)
		{
			final ByteBuffer lengthBuffer = ByteBuffer.allocateDirect(this.protocolLengthDigitCount);
			this.read(connection, lengthBuffer);
			
//			XDebug.printDirectByteBuffer(lengthBuffer);
			
			// buffer position must be reset for the decoder to see the bytes
			lengthBuffer.position(0);
			final String lengthDigits = XFiles.standardCharset().decode(lengthBuffer).toString();
			final int    length       = Integer.parseInt(lengthDigits);
			
			final ByteBuffer protocolBuffer = ByteBuffer.allocateDirect(length - this.protocolLengthDigitCount);
			this.read(connection, protocolBuffer);
			
			// buffer position must be reset to after the separator for the decoder to see the bytes
			protocolBuffer.position(1);
			final char[] protocolChars = XFiles.standardCharset().decode(protocolBuffer).array();
			
			return stringConverter.parse(_charArrayRange.New(protocolChars));
		}
		
	}
	
}
