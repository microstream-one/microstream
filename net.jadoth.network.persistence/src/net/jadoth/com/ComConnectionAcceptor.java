package net.jadoth.com;

import static net.jadoth.X.notNull;

import java.nio.ByteBuffer;

import net.jadoth.files.XFiles;
import net.jadoth.low.XVM;


/**
 * Logic to greet/authenticate the client, exchange metadata, create a {@link ComChannel} instance.
 * Potentially in another, maybe even dedicated thread.
 * 
 * @author TM
 *
 */
public interface ComConnectionAcceptor<C>
{
	public ComProtocolProvider protocolProvider();
	
	public void acceptConnection(C socketChannel);
	
	
	
	public static <C> ComConnectionAcceptorCreator<C> Creator()
	{
		return ComConnectionAcceptorCreator.New();
	}
	
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
	
	public static <C> ComConnectionAcceptor<C> New(
		final ComProtocolProvider        protocolProvider       ,
		final ComProtocolSender<C>       protocolSender         ,
		final ComProtocolStringConverter protocolStringConverter,
		final ComChannelCreator<C>       channelCreator         ,
		final ComChannelAcceptor         channelAcceptor
	)
	{
		
		return new ComConnectionAcceptor.Implementation<>(
			notNull(protocolProvider)       ,
			notNull(protocolStringConverter),
			notNull(protocolSender)         ,
			notNull(channelCreator)         ,
			notNull(channelAcceptor)
		);
	}
	
	public final class Implementation<C> implements ComConnectionAcceptor<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComProtocolProvider        protocolProvider       ;
		private final ComProtocolStringConverter protocolStringConverter;
		private final ComProtocolSender<C>       protocolSender         ;
		private final ComChannelCreator<C>       channelCreator         ;
		private final ComChannelAcceptor         channelAcceptor        ;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComProtocolProvider        protocolProvider       ,
			final ComProtocolStringConverter protocolStringConverter,
			final ComProtocolSender<C>       protocolSender         ,
			final ComChannelCreator<C>       channelCreator         ,
			final ComChannelAcceptor         channelAcceptor
		)
		{
			super();
			this.protocolProvider        = protocolProvider       ;
			this.protocolStringConverter = protocolStringConverter;
			this.protocolSender          = protocolSender         ;
			this.channelCreator          = channelCreator         ;
			this.channelAcceptor         = channelAcceptor        ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComProtocolProvider protocolProvider()
		{
			return this.protocolProvider;
		}
		
		@Override
		public void acceptConnection(final C socketChannel)
		{
			// note: things like authentication could be done here in a wrapping implementation.
						
			final ComProtocol protocol = this.protocolProvider.provideProtocol();
			final ByteBuffer bufferedUtf8Protocol = bufferProtocol(protocol, this.protocolStringConverter);
			this.protocolSender.sendProtocol(socketChannel, bufferedUtf8Protocol);
			
			final ComChannel comChannel = this.channelCreator.createChannel(socketChannel);
			this.channelAcceptor.acceptChannel(comChannel);
		}
		
	}
	
}
