package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.chars.VarString;
import net.jadoth.chars.XChars;
import net.jadoth.low.XVM;

public interface ComProtocolSender<C>
{
	public void sendProtocol(C connection, ComProtocol protocol);
	
	
	public static int defaultLengthDigitCount()
	{
		return 8;
	}
	
	public default int lengthDigitCount()
	{
		return defaultLengthDigitCount();
	}
	
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		final VarString vs = VarString.New(10_000)
			.repeat(lengthCharCount, '0')
			.add(protocolStringConverter.protocolItemSeparator())
		;
		protocolStringConverter.assemble(vs, protocol);
		
		final char[] lengthString = XChars.toString(vs.length()).toCharArray();
		vs.setChars(lengthCharCount - lengthString.length, lengthString);
		
		final byte[] assembledProtocolBytes = vs.encode(); // UTF-8
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code. Hence the direct way.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(assembledProtocolBytes.length + Long.BYTES);
		final long dbbAddress = XVM.getDirectByteBufferAddress(dbb);
		XVM.copyArray(assembledProtocolBytes, dbbAddress);
		// the bytebuffer's position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	public abstract class AbstractImplementation<C> implements ComProtocolSender<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocolStringConverter protocolStringConverter;

		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected AbstractImplementation(final ComProtocolStringConverter protocolStringConverter)
		{
			super();
			this.protocolStringConverter = protocolStringConverter;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void sendProtocol(final C socketChannel, final ComProtocol protocol)
		{
			final ByteBuffer bufferedUtf8Protocol = ComProtocolSender.bufferProtocol(
				protocol,
				this.protocolStringConverter,
				this.lengthDigitCount()
			);
			
			this.writeBufferedUtf8Protocol(socketChannel, bufferedUtf8Protocol);
		}
		
		protected abstract void writeBufferedUtf8Protocol(C socketChannel, ByteBuffer bufferedUtf8Protocol);
		
	}
	
	
	public static ComProtocolSender.Default New(
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return new Default(
			notNull(protocolStringConverter)
		);
	}
	
	public final class Default extends ComProtocolSender.AbstractImplementation<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final ComProtocolStringConverter protocolStringConverter
		)
		{
			super(protocolStringConverter);
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		protected void writeBufferedUtf8Protocol(
			final SocketChannel socketChannel       ,
			final ByteBuffer    bufferedUtf8Protocol
		)
		{
			try
			{
				// (10.11.2018 TM)TODO: What about timeouts and retries and all that?
				socketChannel.write(bufferedUtf8Protocol);
			}
			catch(final IOException e)
			{
				// (10.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
