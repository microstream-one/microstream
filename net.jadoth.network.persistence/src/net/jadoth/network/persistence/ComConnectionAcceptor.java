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
	public ComConfiguration configuration();
	
	public void acceptConnection(SocketChannel socketChannel);
	
	
	
	public static Creator Creator(final ComConfiguration.Assembler configurationAssembler)
	{
		return new Creator.Implementation(configurationAssembler);
	}
	
	public interface Creator
	{
		public ComConnectionAcceptor createConnectionAcceptor(
			ComConfiguration   configuration  ,
			ComChannel.Creator channelCreator ,
			ComChannelAcceptor channelAcceptor
		);
		
		public final class Implementation implements ComConnectionAcceptor.Creator
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final ComConfiguration.Assembler configurationAssembler;
			
			Implementation(final ComConfiguration.Assembler configurationAssembler)
			{
				super();
				this.configurationAssembler = configurationAssembler;
			}

			@Override
			public ComConnectionAcceptor createConnectionAcceptor(
				final ComConfiguration   configuration  ,
				final ComChannel.Creator channelCreator ,
				final ComChannelAcceptor channelAcceptor
			)
			{
				return New(configuration, this.configurationAssembler, channelCreator, channelAcceptor);
			}
			
		}
		
	}
	
	
	
	public static ByteBuffer bufferConfiguration(
		final ComConfiguration           configuration         ,
		final ComConfiguration.Assembler configurationAssembler
	)
	{
		final String assembledConfiguration     = configurationAssembler.assembleConfiguration(configuration);
		final byte[] utf8AssembledConfiguration = assembledConfiguration.getBytes(XFiles.charSetUtf8());
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(utf8AssembledConfiguration.length);
		XVM.copyArray(utf8AssembledConfiguration, XVM.getDirectByteBufferAddress(dbb));
		// note: position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	public static ComConnectionAcceptor New(
		final ComConfiguration           configuration         ,
		final ComConfiguration.Assembler configurationAssembler,
		final ComChannel.Creator         channelCreator        ,
		final ComChannelAcceptor         channelAcceptor
	)
	{
		final ByteBuffer bufferedUtf8Configuration = bufferConfiguration(configuration, configurationAssembler);
		
		return new ComConnectionAcceptor.Implementation(
			notNull(configuration),
			notNull(channelCreator),
			notNull(channelAcceptor),
			bufferedUtf8Configuration
		);
	}
	
	public final class Implementation implements ComConnectionAcceptor
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComConfiguration   configuration  ;
		private final ComChannel.Creator channelCreator ;
		private final ComChannelAcceptor channelAcceptor;
		private final ByteBuffer         bufferedUtf8Configuration;
				
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final ComConfiguration   configuration            ,
			final ComChannel.Creator channelCreator           ,
			final ComChannelAcceptor channelAcceptor          ,
			final ByteBuffer         bufferedUtf8Configuration
		)
		{
			super();
			this.configuration             = configuration            ;
			this.channelCreator            = channelCreator           ;
			this.channelAcceptor           = channelAcceptor          ;
			this.bufferedUtf8Configuration = bufferedUtf8Configuration;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final ComConfiguration configuration()
		{
			return this.configuration;
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
			this.bufferedUtf8Configuration.clear();
			Com.writeComplete(socketChannel, this.bufferedUtf8Configuration);
			
			final ComChannel comChannel = this.channelCreator.createChannel(socketChannel);
			this.channelAcceptor.acceptChannel(comChannel);
		}
		
	}
	
}
