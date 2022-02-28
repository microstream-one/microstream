package one.microstream.communication.tls;

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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;

import one.microstream.com.ComException;
import one.microstream.com.XSockets;
import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComConnectionHandler;
import one.microstream.communication.types.ComConnectionListener;
import one.microstream.util.logging.Logging;

public class ComTLSConnectionHandler extends ComConnectionHandler.Default
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static boolean TLS_CLIENT_MODE = true;
	
	private final static Logger logger = Logging.getLogger(ComConnectionHandler.class);
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final SSLContext context;
	
	private final TLSKeyManagerProvider   keyManagerProvider;
	private final TLSTrustManagerProvider trustManagerProvider;
	private final TLSParametersProvider   tlsParameterProvider;
	private final SecureRandomProvider    randomProvider;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	private ComTLSConnectionHandler(
		final TLSKeyManagerProvider   keyManagerProvider,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider)
	{
		super();
		
		this.tlsParameterProvider = tlsParameterProvider;
		this.keyManagerProvider   = keyManagerProvider;
		this.trustManagerProvider = trustManagerProvider;
		this.randomProvider       = randomProvider;
				
		try
		{
			this.context = SSLContext.getInstance(tlsParameterProvider.getSSLProtocol());
		}
		catch (final NoSuchAlgorithmException e)
		{
			throw new ComException("Failed get SSLContextInstance for " + tlsParameterProvider.getSSLProtocol(), e);
		}
		
		try
		{
			this.context.init(
				this.keyManagerProvider.get(),
				this.trustManagerProvider.get(),
				this.randomProvider.get()
			);
		}
		catch (final KeyManagementException e)
		{
			throw new ComException("Failed to init SSLContext", e);
		}
	}
	
	public static ComConnectionHandler<ComConnection> New(
		final TLSKeyManagerProvider   keyManagerProvider,
		final TLSTrustManagerProvider trustManagerProvider,
		final TLSParametersProvider   tlsParameterProvider,
		final SecureRandomProvider    randomProvider)
	{
		return new ComTLSConnectionHandler(keyManagerProvider, trustManagerProvider, tlsParameterProvider, randomProvider);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComConnectionListener<ComConnection> createConnectionListener(final InetSocketAddress address)
	{
		final ServerSocketChannel serverSocketChannel = XSockets.openServerSocketChannel(address);
		final ComConnectionListener<ComConnection> connectionListener =  new ComTLSConnectionListener(serverSocketChannel, this.context, this.tlsParameterProvider);
		logger.debug("created new ComConnectionListener {}", connectionListener);
		return connectionListener;
	}

	@Override
	public ComTLSConnection openConnection(final InetSocketAddress address)
	{
		final SocketChannel clientChannel = XSockets.openChannel(address);
		final ComTLSConnection connection =  new ComTLSConnection(clientChannel, this.context, this.tlsParameterProvider, TLS_CLIENT_MODE);
		logger.debug("created new ComConnection {}", connection);
		return connection;
	}

	@Override
	public void enableSecurity(final ComConnection connection)
	{
		connection.enableSecurity();
	}
}
