package one.microstream.communication.types;

/*-
 * #%L
 * microstream-communication
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import one.microstream.com.XSockets;

public interface ComConnectionListener<C>
{
	public C listenForConnection();
	
	public void close();
	
	
	
	public static ComConnectionListener.Default Default(final ServerSocketChannel serverSocketChannel)
	{
		return new ComConnectionListener.Default(
			notNull(serverSocketChannel)
		);
	}
	
	public final class Default implements ComConnectionListener<SocketChannel>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ServerSocketChannel serverSocketChannel;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ServerSocketChannel serverSocketChannel)
		{
			super();
			this.serverSocketChannel = serverSocketChannel;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final SocketChannel listenForConnection()
		{
			return XSockets.acceptSocketChannel(this.serverSocketChannel);
		}

		@Override
		public final void close()
		{
			XSockets.closeChannel(this.serverSocketChannel);
		}
		
	}
	
}
