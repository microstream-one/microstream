package net.jadoth.com;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import net.jadoth.chars.VarString;
import net.jadoth.chars.XChars;
import net.jadoth.low.XVM;

public class Com
{
	// (19.11.2018 TM)NOTE: TEH EVIL
//	private static Supplier<? extends ComPersistenceAdaptor<SocketChannel>> persistenceAdaptorSupplier;
//
//	public static final synchronized void setPersistenceAdaptorSupplier(
//		final Supplier<? extends ComPersistenceAdaptor<SocketChannel>> persistenceAdaptorSupplier
//	)
//	{
//		Com.persistenceAdaptorSupplier = mayNull(persistenceAdaptorSupplier);
//	}
//
//	public static final synchronized Supplier<? extends ComPersistenceAdaptor<SocketChannel>> persistenceAdaptorSupplier()
//	{
//		return Com.persistenceAdaptorSupplier;
//	}
//
//	public static final synchronized ComPersistenceAdaptor<SocketChannel> DefaultPersistenceAdaptor()
//	{
//		if(Com.persistenceAdaptorSupplier == null)
//		{
//			// (19.11.2018 TM)EXCP: proper exception
//			throw new NullPointerException(
//				"No default " + ComPersistenceAdaptor.class.getSimpleName() + " supplier registered."
//			);
//		}
//
//		return Com.persistenceAdaptorSupplier.get();
//	}
	
	
	public static long defaultObjectIdBaseServer()
	{
		return 9_200_000_000_000_000_000L;
	}
	
	public static long defaultObjectIdBaseClient()
	{
		return 9_100_000_000_000_000_000L;
	}
	
	// (18.11.2018 TM)FIXME: use or remove default id strategies
	public static ComDefaultIdStrategy DefaultIdStrategy(final long startingObjectId)
	{
		return ComDefaultIdStrategy.New(startingObjectId);
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyServer()
	{
		return DefaultIdStrategy(defaultObjectIdBaseServer());
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyClient()
	{
		return DefaultIdStrategy(defaultObjectIdBaseClient());
	}
	
	public static ByteOrder byteOrder()
	{
		return XVM.nativeByteOrder();
	}
	
	/**
	 * An arbitrary default port, mostly only viable for uber-simplicity demonstration purposes.
	 * 
	 * @return the framework's default port.
	 */
	public static int defaultPort()
	{
		// arbitrary, totally random port. Totally random, really.
		return 1099;
	}
	
	public static InetSocketAddress localHostSocketAddress() throws ComException
	{
		return localHostSocketAddress(defaultPort());
	}
	
	public static InetSocketAddress localHostSocketAddress(final int port) throws ComException
	{
		return new InetSocketAddress(XSockets.localHostAddress(), port);
	}
	
	
	
	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New();
	}
	
	
	public static int defaultProtocolLengthDigitCount()
	{
		return 8;
	}
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return bufferProtocol(protocol, protocolStringConverter, defaultProtocolLengthDigitCount());
	}
	
	public static ByteBuffer bufferProtocol(
		final ComProtocol                protocol                ,
		final ComProtocolStringConverter protocolStringConverter ,
		final int                        protocolLengthDigitCount
	)
	{
		final byte[] assembledProtocolBytes = Com.assembleSendableProtocolBytes(
			protocol               ,
			protocolStringConverter,
			protocolLengthDigitCount
		);
		
		// the ByteBuffer#put(byte[]) is, of course, a catastrophe, as usual in JDK code. Hence the direct way.
		final ByteBuffer dbb = ByteBuffer.allocateDirect(assembledProtocolBytes.length);
		final long dbbAddress = XVM.getDirectByteBufferAddress(dbb);
		XVM.copyArray(assembledProtocolBytes, dbbAddress);
		// the bytebuffer's position remains at 0, limit at capacity. Both are correct for the first reading call.
		
		return dbb;
	}
	
	public static byte[] assembleSendableProtocolBytes(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter
	)
	{
		return assembleSendableProtocolBytes(protocol, protocolStringConverter, defaultProtocolLengthDigitCount());
	}
	
	public static byte[] assembleSendableProtocolBytes(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		// encode uses by default UTF-8
		return assembleSendableProtocolString(protocol, protocolStringConverter, lengthCharCount).encode();
	}
	
	public static VarString assembleSendableProtocolString(
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		final VarString vs = VarString.New(10_000);
		
		return assembleSendableProtocolString(vs, protocol, protocolStringConverter, lengthCharCount);
	}
	
	public static VarString assembleSendableProtocolString(
		final VarString                  vs                     ,
		final ComProtocol                protocol               ,
		final ComProtocolStringConverter protocolStringConverter,
		final int                        lengthCharCount
	)
	{
		vs
		.reset()
		.repeat(lengthCharCount, '0')
		.add(protocolStringConverter.protocolItemSeparator())
		;
		protocolStringConverter.assemble(vs, protocol);
		
		final char[] lengthString = XChars.toString(vs.length()).toCharArray();
		vs.setChars(lengthCharCount - lengthString.length, lengthString);
		
		return vs;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
	
	public static final ComHost<SocketChannel> Host(
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return Host(persistenceAdaptor, null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final int                                  localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return Host(localHostPort, persistenceAdaptor, null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress                    targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return Host(targetAddress, persistenceAdaptor, null);
	}
	
	public static final ComHost<SocketChannel> Host(
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(),
			persistenceAdaptor,
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final int                                   localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptor,
			channelAcceptor
		);
	}
	
	public static final ComHost<SocketChannel> Host(
		final InetSocketAddress                     targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		final ComHost<SocketChannel> host = Com.Foundation()
			.setHostBindingAddress (targetAddress)
			.setPersistenceAdaptor (persistenceAdaptor)
			.setHostChannelAcceptor(channelAcceptor)
			.createHost()
		;
		
		return host;
	}
	
	
	public static final void runHost(
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		runHost(persistenceAdaptor, null);
	}
	
	public static final void runHost(
		final int                                  localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		runHost(localHostPort, persistenceAdaptor, null);
	}
	
	public static final void runHost(
		final InetSocketAddress                    targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		runHost(targetAddress, persistenceAdaptor, null);
	}
	
	public static final void runHost(
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			persistenceAdaptor,
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                   localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptor,
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                     targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel>  persistenceAdaptor,
		final ComHostChannelAcceptor<SocketChannel> channelAcceptor
	)
	{
		final ComHost<SocketChannel> host = Com.Host(targetAddress, persistenceAdaptor, channelAcceptor);
		host.run();
	}

	
	public static final ComClient<SocketChannel> Client(
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return Client(
			Com.localHostSocketAddress(),
			persistenceAdaptor
		);
	}
	
	public static final ComClient<SocketChannel> Client(
		final int                                  localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return Client(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptor
		);
	}
	
	public static final ComClient<SocketChannel> Client(
		final InetSocketAddress                    targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		final ComClient<SocketChannel> client = Com.Foundation()
			.setClientTargetAddress(targetAddress)
			.setPersistenceAdaptor(persistenceAdaptor)
			.createClient()
		;
		
		return client;
	}
	
	
	public static final ComClientChannel<SocketChannel> connect(
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return connect(
			Com.localHostSocketAddress(),
			persistenceAdaptor
		);
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final int                                  localHostPort     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		return connect(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptor
		);
	}
	
	public static final ComClientChannel<SocketChannel> connect(
		final InetSocketAddress                    targetAddress     ,
		final ComPersistenceAdaptor<SocketChannel> persistenceAdaptor
	)
	{
		final ComClientChannel<SocketChannel> channel = Com.Client(targetAddress, persistenceAdaptor)
			.connect()
		;
		
		return channel;
	}
	
	/**
	 * This method is catastrophically naive. And by design. Its only purpose and viability is to serve
	 * as an uber-simplicity default implementation for {@link ComHostChannelAcceptor} for framework demonstration
	 * purposes.<p>
	 * Used logic:
	 * <ul>
	 * <li>uses {@link SocketChannel}.</li>
	 * <li>calls {@link ComHostChannel#receive()} on the passed channel.</li>
	 * <li>calls {@link Object#toString()} on the received instance to assemble its {@link String} representation.</li>
	 * <li>sends a new {@link String} echoing the string representation back to the sender.
	 * <li>calls {@link ComHostChannel#close()} on the passed channel.
	 * </ul>
	 * So this method depends on the mercy of whatever the received instance's Class {@link Object#toString()}
	 * implementation is and works only with strings and closes the channel after one sent message.
	 * It is absolutely not recommended to use this method for anything except basic demonstration purposes and
	 * as an API usage/learning example.<p>
	 * 
	 * /!\ DO NOT USE THIS METHOD FOR PRODUCTION Purposes!<p>
	 * 
	 * You have been warned.
	 * 
	 * @param channel A one-shot {@link ComHostChannel} to receive and send exactely one message.
	 */
	public static void bounce(final ComHostChannel<SocketChannel> channel) throws ComException
	{
		channel.send("You said: \"" + channel.receive().toString() + "\". Goodbye.");
		channel.close();
	}
			
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	private Com()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
