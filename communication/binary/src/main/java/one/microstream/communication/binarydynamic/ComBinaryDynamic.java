package one.microstream.communication.binarydynamic;

/*-
 * #%L
 * MicroStream Communication Binary
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

import one.microstream.communication.types.Com;
import one.microstream.communication.types.ComClient;
import one.microstream.communication.types.ComClientChannel;
import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComFoundation;
import one.microstream.communication.types.ComHost;
import one.microstream.communication.types.ComHostChannelAcceptor;
import one.microstream.communication.types.ComPersistenceAdaptorCreator;

public class ComBinaryDynamic
{

	public static ComFoundation.Default<?> Foundation()
	{
		return ComFoundation.New()
			.setPersistenceAdaptorCreator(DefaultPersistenceAdaptorCreator())
			.setHostIdStrategy(ComDynamicIdStrategy.New(1_000_000_000_000_000_000L))
			.setClientIdStrategy(ComDynamicIdStrategy.New(4_100_000_000_000_000_000L))
			.registerEntityTypes(ComMessageNewType.class, ComMessageClientTypeMismatch.class, ComMessageStatus.class, ComMessageData.class)
		;
	}

	private static ComPersistenceAdaptorCreator<ComConnection> DefaultPersistenceAdaptorCreator()
	{
		return ComPersistenceAdaptorBinaryDynamic.Creator();
	}

	///////////////////////////////////////////////////////////////////////////
	// convenience methods //
	////////////////////////
		
	
	/////
	// host convenience methods
	////
	
	public static final ComHost<ComConnection> Host()
	{
		return Host(DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final int localHostPort
	)
	{
		return Host(localHostPort, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress  targetAddress
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), null);
	}
	
	public static final ComHost<ComConnection> Host(
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(
			DefaultPersistenceAdaptorCreator(),
			channelAcceptor
		);
	}
	
	public static final ComHost<ComConnection> Host(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		return Host(targetAddress, DefaultPersistenceAdaptorCreator(), channelAcceptor);
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
		final InetSocketAddress                           targetAddress            ,
		final ComPersistenceAdaptorCreator<ComConnection> persistenceAdaptorCreator,
		final ComHostChannelAcceptor<ComConnection>       channelAcceptor
	)
	{
		final ComHost<ComConnection> host =
			Foundation()
			.setHostBindingAddress       (targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.setHostChannelAcceptor      (channelAcceptor)
			.createHost()
		;
		
		return host;
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
		
	public static final void runHost()
	{
		runHost(null, null);
	}
	
	public static final void runHost(
		final int localHostPort
	)
	{
		runHost(localHostPort, null);
	}
	
	public static final void runHost(
		final InetSocketAddress targetAddress
	)
	{
		runHost(targetAddress, null);
	}
	
	public static final void runHost(
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final int                                   localHostPort  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		runHost(
			Com.localHostSocketAddress(localHostPort),
			channelAcceptor
		);
	}
	
	public static final void runHost(
		final InetSocketAddress                     targetAddress  ,
		final ComHostChannelAcceptor<ComConnection> channelAcceptor
	)
	{
		final ComHost<ComConnection> host = Host(targetAddress, channelAcceptor);
		host.run();
	}
	
	/////
	// client convenience methods
	////
	
	public static final ComClient<ComConnection> Client()
	{
		return Client(
			DefaultPersistenceAdaptorCreator()
		);
	}
	
	public static final ComClient<ComConnection> Client(final int localHostPort)
	{
		return Client(
			localHostPort                     ,
			DefaultPersistenceAdaptorCreator()
		);
	}
		
	public static final ComClient<ComConnection> Client(
		final InetSocketAddress targetAddress
	)
	{
		return Client(
			targetAddress,
			DefaultPersistenceAdaptorCreator()
		);
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
		final ComClient<ComConnection> client = Foundation()
			.setClientTargetAddress(targetAddress)
			.setPersistenceAdaptorCreator(persistenceAdaptorCreator)
			.createClient()
		;
		
		return client;
	}
	
	
	public static final ComClientChannel<ComConnection> connect()
	{
		return Client()
			.connect()
		;
	}
	
	public static final ComClientChannel<ComConnection> connect(
		final int localHostPort
	)
	{
		return Client(localHostPort)
			.connect()
		;
	}
		
	public static final ComClientChannel<ComConnection> connect(
		final InetSocketAddress targetAddress
	)
	{
		return Client(targetAddress)
			.connect()
		;
	}

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	/**
	 * Dummy constructor to prevent instantiation of this static-only utility class.
	 * 
	 * @throws UnsupportedOperationException
	 */
	private ComBinaryDynamic()
	{
		// static only
		throw new UnsupportedOperationException();
	}
}

