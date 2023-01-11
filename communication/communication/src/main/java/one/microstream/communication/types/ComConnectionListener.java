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

import static one.microstream.X.notNull;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;

import one.microstream.com.XSockets;
import one.microstream.util.logging.Logging;

/**
 * 
 * @param <C> the communication layer type
 */
public interface ComConnectionListener<C>
{
	public ComConnection createConnection(SocketChannel channel);
	
	public C listenForConnection();
	
	public void close();
	
	public boolean isAlive();
	
	public static ComConnectionListener.Default Default(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public class Default implements ComConnectionListener<ComConnection>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(ComConnectionListener.class);
		
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
						
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		protected Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
				
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public ComConnection createConnection(final SocketChannel channel)
		{
			final ComConnection connection =  new ComConnection.Default(channel);
			logger.debug("created new ComConnection {}", connection);
			return connection;
		}
		
		@Override
		public final ComConnection listenForConnection()
		{
			logger.debug("listening for incoming connections at {} ", this.serverSocketChannel);
			final SocketChannel channel = XSockets.acceptSocketChannel(this.serverSocketChannel);
			logger.debug("incoming connection {}", channel);
			return this.createConnection(channel);
		}

		@Override
		public final void close()
		{
			logger.debug("closing serverSocket Channel {}", this.serverSocketChannel);
			XSockets.closeChannel(this.serverSocketChannel);
		}

		@Override
		public boolean isAlive()
		{
			return this.serverSocketChannel.isOpen();
		}
		
	}
	
}
