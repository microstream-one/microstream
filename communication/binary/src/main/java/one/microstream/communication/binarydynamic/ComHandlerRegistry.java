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

import org.slf4j.Logger;

import one.microstream.collections.MiniMap;
import one.microstream.util.logging.Logging;

public interface ComHandlerRegistry
{
	public <T> boolean registerSendHandler    (final Class<T> type, final ComHandlerSend<?> handler);
	public <T> boolean registerReceiveHandler (final Class<T> type, final ComHandlerReceive<?> handler);
	
	public <T extends ComMessage> ComHandlerSend<ComMessage>    lookupSend(final Class<?> type);
	public <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type);

	
	
	public final class Default implements ComHandlerRegistry
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private final static Logger logger = Logging.getLogger(Default.class);
		
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final MiniMap<Class<?>, ComHandlerSend<?>>    sendHandlers    = new MiniMap<>();
		private final MiniMap<Class<?>, ComHandlerReceive<?>> receiveHandlers = new MiniMap<>();
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public Default()
		{
			super();
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final <T> boolean registerSendHandler(final Class<T> type, final ComHandlerSend<?> handler)
		{
			logger.debug("registered sending handler {} for type {}", handler.getClass(), type);
			return this.sendHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerSend<ComMessage> lookupSend(final Class<?> type)
		{
			return (ComHandlerSend<ComMessage>) this.sendHandlers.get(type);
		}
		
		@Override
		public final <T> boolean registerReceiveHandler(final Class<T> type, final ComHandlerReceive<?> handler)
		{
			logger.debug("registered receiving handler {} for type {}", handler.getClass(), type);
			return this.receiveHandlers.put(type, handler) != null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public final <T extends ComMessage> ComHandlerReceive<ComMessage> lookupReceive(final Class<?> type)
		{
			return (ComHandlerReceive<ComMessage>) this.receiveHandlers.get(type);
		}
	}
	
	
}
