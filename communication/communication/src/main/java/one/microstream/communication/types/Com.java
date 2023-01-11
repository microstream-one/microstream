package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;

import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.com.ComException;
import one.microstream.com.XSockets;
import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceObjectIdStrategy;
import one.microstream.persistence.types.PersistenceTypeIdStrategy;


public class Com
{
	public static long defaultObjectIdBaseHost()
	{
		return 9_200_000_000_000_000_000L;
	}
	
	public static long defaultObjectIdBaseClient()
	{
		return 9_100_000_000_000_000_000L;
	}

	public static ComDefaultIdStrategy DefaultIdStrategy(final long startingObjectId)
	{
		return ComDefaultIdStrategy.New(startingObjectId);
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyHost()
	{
		return DefaultIdStrategy(defaultObjectIdBaseHost());
	}
	
	public static ComDefaultIdStrategy DefaultIdStrategyClient()
	{
		return DefaultIdStrategy(defaultObjectIdBaseClient());
	}
	
	public static PersistenceIdStrategy DefaultIdStrategyHostInitialization()
	{
		return PersistenceIdStrategy.New(
			PersistenceObjectIdStrategy.None(),
			PersistenceTypeIdStrategy.Transient()
		);
	}
	
	public static ByteOrder byteOrder()
	{
		return ByteOrder.nativeOrder();
	}
	
	/**
	 * An arbitrary default port, mostly only viable for uber-simplicity demonstration purposes.
	 * 
	 * @return the framework's default port.
	 */
	public static int defaultPort()
	{
		// arbitrary, totally random port.
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
		
		// the ByteBuffer#put(byte[]) has issues, hence the direct way.
		final ByteBuffer dbb = XMemory.allocateDirectNative(assembledProtocolBytes.length);
		final long dbbAddress = XMemory.getDirectByteBufferAddress(dbb);
		XMemory.copyArrayToAddress(assembledProtocolBytes, dbbAddress);
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
		
		final char[] lengthString = XChars.readChars(XChars.String(vs.length()));
		vs.setChars(lengthCharCount - lengthString.length, lengthString);
		
		return vs;
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
	 * @param channel A one-shot {@link ComHostChannel} to receive and send exactly one message.
	 */
	public static void bounce(final ComHostChannel<ComConnection> channel) throws ComException
	{
		channel.send("You said: \"" + channel.receive().toString() + "\". Goodbye.");
		channel.close();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
	
	public static final ComHost<ComConnection> Host(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Host(persistenceAdaptorCreator, null);
	}
	
	public static final ComHost<ComConnection> Host(
		final int                                         localHostPort            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Host(localHostPort, persistenceAdaptorCreator, null);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Host(targetAddress, persistenceAdaptorCreator, null);
	}
	
	public static final ComHost<ComConnection> Host(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator   ,
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final int                                         localHostPort            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		return Host(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator                ,
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		final ComHost<ComConnection> host =
			Com.Foundation()
			.setHostBindingAddress       (targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.setHostChannelAcceptor      (channelAcceptor)
			.createHost()
		;
		
		return host;
	}
	
	
	public static final void runHost(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		runHost(persistenceAdaptorCreator, null);
	}
	
	public static final void runHost(
		final int                                         localHostPort            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		runHost(localHostPort, persistenceAdaptorCreator, null);
	}
	
	public static final void runHost(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		runHost(targetAddress, persistenceAdaptorCreator, null);
	}
	
	public static final void runHost(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator   ,
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                         localHostPort            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator,
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		final ComHost<ComConnection> host = Com.Host(targetAddress, persistenceAdaptorCreator, channelAcceptor);
		host.run();
	}

	
	public static final ComClient<ComConnection> Client(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<ComConnection> Client(
		final int                                         localHostPort     ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return Client(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClient<ComConnection> Client(
		final InetSocketAddress                           targetAddress     ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		final ComClient<ComConnection> client = Com.Foundation()
			.setClientTargetAddress(targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.createClient()
		;
		
		return client;
	}
	
	
	public static final ComClientChannel<ComConnection> connect(
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return connect(
			Com.localHostSocketAddress(),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClientChannel<ComConnection> connect(
		final int                                         localHostPort            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		return connect(
			Com.localHostSocketAddress(localHostPort),
			persistenceAdaptorCreator
		);
	}
	
	public static final ComClientChannel<ComConnection> connect(
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator
	)
	{
		final ComClientChannel<ComConnection> channel = Com.Client(targetAddress, persistenceAdaptorCreator)
			.connect()
		;
		
		return channel;
	}
			
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException when called
	 */
	private Com()
	{
		// static only
		throw new UnsupportedOperationException();
	}
	
}
