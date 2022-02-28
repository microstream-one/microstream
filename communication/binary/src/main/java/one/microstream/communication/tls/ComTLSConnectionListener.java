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

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;

import one.microstream.communication.types.ComConnection;
import one.microstream.communication.types.ComConnectionListener;
import one.microstream.util.logging.Logging;

public class ComTLSConnectionListener extends ComConnectionListener.Default
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(ComConnectionListener.class);


	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
		
	private final SSLContext sslContext;
	private final TLSParametersProvider sslParameters;

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComTLSConnectionListener(
		final ServerSocketChannel serverSocketChannel,
		final SSLContext context,
		final TLSParametersProvider tlsParameterProvider)
	{
		super(serverSocketChannel);
		this.sslContext = context;
		this.sslParameters = tlsParameterProvider;
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ComConnection createConnection(final SocketChannel channel)
	{
		final ComConnection connection = new ComTLSConnection(channel, this.sslContext, this.sslParameters, false);
		logger.debug("created new ComConnection {}", connection);
		return connection;
	}
}
