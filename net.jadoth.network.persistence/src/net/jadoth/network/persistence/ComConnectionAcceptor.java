package net.jadoth.network.persistence;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import net.jadoth.files.XFiles;
import net.jadoth.low.XVM;


/**
 * Logic to greet/authenticate the client, exchange metadata, create a {@link ComChannel} instance.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComConnectionAcceptor
{
	public ComProtocol protocol();
	
	public void acceptConnection(SocketChannel socketChannel);
	
	
	
	public static Creator Creator(final ComProtocolStringConverter protocolStringConverter)
	{
		return new Creator.Implementation(protocolStringConverter);
	}
	
	public interface Creator
	{
		public ComConnectionAcceptor createConnectionAcceptor(
			ComProtocol        protocol       ,
			ComChannel.Creator channelCreator ,
			ComChannelAcceptor channelAcceptor
		);
		
		public final class Implementation implements ComConnectionAcceptor.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final ComProtocolStringConverter protocolStringConverter;
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			Implementation(final ComProtocolStringConverter protocolStringConverter)
			{
				super();
				this.protocolStringConverter = protocolStringConverter;
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public ComConnectionAcceptor createConnectionAcceptor(
				final ComProtocol        protocol       ,
				final ComChannel.Creator channelCreator ,
				final ComChannelAcceptor channelAcceptor
			)
			{
				return New(protocol, this.protocolStringConverter, channelCreator, channelAcceptor);
			}
			
		}
		
	}
	
	
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		final String assembledProtocol     = protocolStringConverter.assemble(protocol);
		final byte[] utf8AssembledProtocol = assembledProtocol.getBytes(XFiles.charSetUtf8());
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(utf8AssembledProtocol.length + Long.BYTES);
		final long dbbAddress = XVM.getDirectByteBufferAddress(dbb);
		
		// exchanged/stored length values are always long for compatibility throughout all layers and frameworks.
		XVM.set_long(dbbAddress, utf8AssembledProtocol.length);
		XVM.copyArray(utf8AssembledProtocol, dbbAddress + Long.BYTES);
		// note: position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	public static ComConnectionAcceptor New(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final ComChannel.Creator         channelCreator         ,
		final ComChannelAcceptor         channelAcceptor
	)
	{
		final ByteBuffer bufferedUtf8Protocol = bufferProtocol(protocol, protocolStringConverter);
		
		return new ComConnectionAcceptor.Implementation(
			notNull(protocol)       ,
			notNull(channelCreator) ,
			notNull(channelAcceptor),
			bufferedUtf8Protocol
		);
	}
	
	public final class Implementation implements ComConnectionAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocol        protocol            ;
		private final ComChannel.Creator channelCreator      ;
		private final ComChannelAcceptor channelAcceptor     ;
		private final ByteBuffer         bufferedUtf8Protocol;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComProtocol        protocol            ,
			final ComChannel.Creator channelCreator      ,
			final ComChannelAcceptor channelAcceptor     ,
			final ByteBuffer         bufferedUtf8Protocol
		)
		{
			super();
			this.protocol             = protocol            ;
			this.channelCreator       = channelCreator      ;
			this.channelAcceptor      = channelAcceptor     ;
			this.bufferedUtf8Protocol = bufferedUtf8Protocol;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComProtocol protocol()
		{
			return this.protocol;
		}
		
		@Override
		public void acceptConnection(final SocketChannel socketChannel)
		{
			// note: things like authentication could be done here in a wrapping implementation.
			
			/*
			 * "clear" is of course totally wrong. Only the index values are cleared, not the data. Morons, every time.
			 * Also, the method's return type is not respecified. Always funny to see how incredibly incompetent
			 * JDK developers are in using their own language.
			 */
			this.bufferedUtf8Protocol.clear();
			Com.writeComplete(socketChannel, this.bufferedUtf8Protocol);
			
			final ComChannel comChannel = this.channelCreator.createChannel(socketChannel);
			this.channelAcceptor.acceptChannel(comChannel);
		}
		
	}
	
}
