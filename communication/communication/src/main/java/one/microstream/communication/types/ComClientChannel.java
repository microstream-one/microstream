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

import one.microstream.persistence.types.PersistenceManager;

/**
 * 
 * @param <C> the communication layer type
 */
public interface ComClientChannel<C> extends ComChannel
{
	public C connection();
	
	public ComProtocol protocol();
	
	public ComClient<C> parent();
	
	
	
	public static <C> ComClientChannel<C> New(
		final PersistenceManager<?> persistenceManager,
		final C                     connection        ,
		final ComProtocol           protocol          ,
		final ComClient<C>          parent
	)
	{
		return new ComClientChannel.Default<>(
			notNull(persistenceManager),
			notNull(connection)        ,
			notNull(protocol)          ,
			notNull(parent)
		);
	}
	
	public final class Default<C> extends ComChannel.Default implements ComClientChannel<C>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final C            connection;
		private final ComProtocol  protocol  ;
		private final ComClient<C> parent    ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final PersistenceManager<?> persistenceManager,
			final C                     connection        ,
			final ComProtocol           protocol          ,
			final ComClient<C>          parent
		)
		{
			super(persistenceManager);
			this.connection = connection;
			this.protocol   = protocol  ;
			this.parent     = parent    ;
		}

		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public final C connection()
		{
			return this.connection;
		}

		@Override
		public final ComProtocol protocol()
		{
			return this.protocol;
		}

		@Override
		public final ComClient<C> parent()
		{
			return this.parent;
		}
		
	}
	
}
