package communication.channel.simplesample;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import one.microstream.memory.PlatformInternals;


public class ChannelCommon
{
	private static final int     PORT    = 1337;
	private static final Charset CHARSET = StandardCharsets.UTF_8;



	static SocketChannel openRemoteChannel(final InetAddress address) throws IOException
	{
		final SocketChannel socketChannel = SocketChannel.open();
		socketChannel.connect(new InetSocketAddress(address, PORT));
		return socketChannel;
	}

	static ServerSocketChannel openServerChannelLocalhost() throws IOException
	{
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.socket().bind(new InetSocketAddress(PORT));
		return serverChannel;
	}

	static void sendString(final SocketChannel targetChannel, final String message) throws IOException
	{
		for(final ByteBuffer buffer = ByteBuffer.wrap(message.getBytes(CHARSET));
			buffer.hasRemaining();
		){
			targetChannel.write(buffer);
		}
	}

	static String readString(final SocketChannel sourceChannel) throws IOException
	{
		final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
		sourceChannel.read(buffer);
		buffer.flip();
		final byte[] bytes;
		buffer.get(bytes = new byte[buffer.limit()]);
		PlatformInternals.deallocateDirectBuffer(buffer);
		return new String(bytes, CHARSET);
	}

	static String communicate(final SocketChannel targetChannel, final String message) throws IOException
	{
		sendString(targetChannel, message);
		return readString(targetChannel);
	}

}
