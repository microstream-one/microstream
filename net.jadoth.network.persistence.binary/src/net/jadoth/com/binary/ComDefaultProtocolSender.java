package net.jadoth.com.binary;

import static net.jadoth.X.notNull;
import static net.jadoth.com.binary.ComDefaultProtocolSender.bufferProtocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.com.ComProtocol;
import net.jadoth.com.ComProtocolSender;
import net.jadoth.com.ComProtocolStringConverter;
import net.jadoth.files.XFiles;
import net.jadoth.low.XVM;

public interface ComDefaultProtocolSender extends ComProtocolSender<SocketChannel>
{
	@Override
	public void sendProtocol(SocketChannel socketChannel, ComProtocol protocol);
	
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		final String assembledProtocol      = protocolStringConverter.assemble(protocol);
		final byte[] assembledProtocolBytes = assembledProtocol.getBytes(XFiles.standardCharset());
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(assembledProtocolBytes.length + Long.BYTES);
		final long dbbAddress = XVM.getDirectByteBufferAddress(dbb);
		
		// exchanged/stored length values are always long for compatibility throughout all layers and frameworks.
		XVM.set_long(dbbAddress, assembledProtocolBytes.length);
		XVM.copyArray(assembledProtocolBytes, dbbAddress + Long.BYTES);
		// note: position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	
	public static ComDefaultProtocolSender.Implementation New(
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return new ComDefaultProtocolSender.Implementation(
			notNull(protocolStringConverter)
		);
	}
	
	public final class Implementation implements ComDefaultProtocolSender
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final ComProtocolStringConverter protocolStringConverter;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComProtocolStringConverter protocolStringConverter
		)
		{
			super();
			this.protocolStringConverter = protocolStringConverter;
		}
		
		@Override
		public void sendProtocol(final SocketChannel socketChannel, final ComProtocol protocol)
		{
			final ByteBuffer bufferedUtf8Protocol = bufferProtocol(protocol, this.protocolStringConverter);
			try
			{
				// (10.11.2018 TM)TODO: What about timeouts and retries and all that?
				ComDefault.write(socketChannel, bufferedUtf8Protocol);
			}
			catch(final IOException e)
			{
				// (10.11.2018 TM)EXCP: proper exception
				throw new RuntimeException(e);
			}
		}
		
	}
	
}
