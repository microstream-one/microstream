package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.low.XVM;

public interface ComProtocolSender<C>
{
	public void sendProtocol(C connection, ComProtocol protocol);
		
	
	
	public default int lengthDigitCount()
	{
		return Com.defaultLengthDigitCount();
	}
	
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthDigitCount
	)
	{
		final byte[] assembledProtocolBytes = Com.assembleSendableProtocolBytes(
			protocol               ,
			protocolStringConverter,
			lengthDigitCount
		);
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code. Hence the direct way.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(assembledProtocolBytes.length);
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
			XSockets.writeCompletely(socketChannel, bufferedUtf8Protocol);
		}
		
	}
	
}
