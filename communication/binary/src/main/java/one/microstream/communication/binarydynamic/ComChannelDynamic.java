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

import one.microstream.communication.types.ComChannel;
import one.microstream.communication.types.ComProtocol;
import one.microstream.persistence.types.PersistenceManager;
import one.microstream.util.logging.Logging;

public abstract class ComChannelDynamic<C> implements ComChannel
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private final static Logger logger = Logging.getLogger(Default.class);

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	protected final PersistenceManager<?> persistenceManager;
	protected final C                     connection;
	protected final ComProtocol           protocol;
	protected final ComHandlerRegistry    handlers = new ComHandlerRegistry.Default();

	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public ComChannelDynamic(
		final PersistenceManager<?> persistenceManager,
		final C                     connection,
		final ComProtocol           protocol
	)
	{
		this.connection         = connection;
		this.persistenceManager = persistenceManager;
		this.protocol           = protocol;
	}

	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	//Bypass Handlers to avoid recursion if called inside an handler ...
	public Object requestUnhandled(final Object object)
	{
		this.persistenceManager.store(object);
		return this.persistenceManager.get();
	}
	
	@Override
	public final void send(final Object graphRoot)
	{
		logger.trace("sending data");
		
		ComHandlerSend<?> handler = null;
		
		if(graphRoot != null)
		{
			handler = this.handlers.lookupSend(graphRoot.getClass());
		}
		
		if(handler != null )
		{
			logger.trace("sending data with handler {}", handler.getClass());
			handler.sendMessage(graphRoot);
		}
		else
		{
			this.persistenceManager.store(new ComMessageData(graphRoot));
		}
		
		logger.trace("sent data successfully");
	}

	@Override
	public final Object receive()
	{
		Object received = null;
		
		while(null == received)
		{
			logger.trace("waiting for data");
			received = this.persistenceManager.get();
			this.persistenceManager.objectRegistry().clear();
	
			final ComHandlerReceive<?> handler = this.handlers.lookupReceive(received.getClass());
			if(handler != null )
			{
				logger.trace("processing received data with handler {}", handler.getClass());
				received = handler.processMessage(received);
			
				if(!handler.continueReceiving())
				{
					break;
				}
			}
		}
		
		logger.trace("data received successfully");
		
		return received;
	}

	@Override
	public final void close()
	{
		logger.trace("closing ComChannel");
		this.persistenceManager.close();
	}
}
