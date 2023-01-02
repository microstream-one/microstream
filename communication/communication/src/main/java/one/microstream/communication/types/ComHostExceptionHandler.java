package one.microstream.communication.types;

/*-
 * #%L
 * MicroStream Communication
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

import org.slf4j.Logger;

import one.microstream.util.logging.Logging;

public interface ComHostExceptionHandler<C>
{
	public void handleException(Throwable exception, ComChannel channel);
	public void handleConnectException(Throwable exception, C connection);
	
	public static <C> ComHostExceptionHandler<C> New(final ComConnectionHandler<C> connectionHandler)
	{
		return new ComHostExceptionHandler.Default<>(connectionHandler);
	}
	
	public final class Default<C> implements ComHostExceptionHandler<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
			
		private final static Logger logger = Logging.getLogger(ComConnectionHandler.class);

		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final ComConnectionHandler<C> connectionHandler;

		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(final ComConnectionHandler<C> connectionHandler)
		{
			super();
			this.connectionHandler = connectionHandler;
		}
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void handleException(final Throwable exception, final ComChannel channel)
		{
			logger.error("Closing connection because: ", exception);
			channel.close();
		}


		@Override
		public void handleConnectException(final Throwable exception, final C connection)
		{
			logger.error("Closing connection because of ", exception);
			try
			{
				this.connectionHandler.close(connection);
			}
			catch(final Exception e)
			{
				logger.error("failed to close connection! ", exception);
			}
		}
		
	}
}
