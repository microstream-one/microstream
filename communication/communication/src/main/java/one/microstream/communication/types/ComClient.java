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

import java.net.InetSocketAddress;

import one.microstream.com.ComException;


public interface ComClient<C>
{
	public ComClientChannel<C> connect() throws ComException;
	
	public InetSocketAddress hostAddress();
	
	
	
	public static <C> ComClientCreator<C> Creator()
	{
		return ComClientCreator.New();
	}
	
	public static <C> ComClient.Default<C> New(
		final InetSocketAddress          hostAddress       ,
		final ComConnectionHandler<C>    connectionHandler ,
		final ComProtocolStringConverter protocolParser    ,
		final ComPersistenceAdaptor<C>   persistenceAdaptor
	)
	{
		return new ComClient.Default<>(
			hostAddress       ,
			connectionHandler ,
			protocolParser    ,
			persistenceAdaptor
		);
	}
	
	public final class Default<C> implements ComClient<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final InetSocketAddress          hostAddress       ;
		private final ComConnectionHandler<C>    connectionHandler ;
		private final ComProtocolStringConverter protocolParser    ;
		private final ComPersistenceAdaptor<C>   persistenceAdaptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final InetSocketAddress          hostAddress       ,
			final ComConnectionHandler<C>    connectionHandler ,
			final ComProtocolStringConverter protocolParser    ,
			final ComPersistenceAdaptor<C>   persistenceAdaptor
		)
		{
			super();
			this.hostAddress        = hostAddress       ;
			this.connectionHandler  = connectionHandler ;
			this.protocolParser     = protocolParser    ;
			this.persistenceAdaptor = persistenceAdaptor;
		}


		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final InetSocketAddress hostAddress()
		{
			return this.hostAddress;
		}
		
		@Override
		public ComClientChannel<C> connect() throws ComException
		{
			final C                   conn     = this.connectionHandler.openConnection(this.hostAddress);
			final ComProtocol         protocol = this.connectionHandler.receiveProtocol(conn, this.protocolParser);
			final ComClientChannel<C> channel  = this.persistenceAdaptor.createClientChannel(conn, protocol, this);
			
			return channel;
		}
		
	}
	
}
