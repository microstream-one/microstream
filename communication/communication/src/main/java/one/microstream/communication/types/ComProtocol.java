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

import java.nio.ByteOrder;

import one.microstream.persistence.types.PersistenceIdStrategy;
import one.microstream.persistence.types.PersistenceTypeDictionaryView;
import one.microstream.typing.Immutable;

public interface ComProtocol extends ComProtocolData
{
	public static String protocolName()
	{
		return "MICROSTREAM-COMCHANNEL";
	}
	
	public static String protocolVersion()
	{
		// (31.10.2018 TM)TODO: Maybe create a "Version" type with multiple sub version numbers?
		return "1.0";
	}
				
	public static ComProtocolCreator Creator()
	{
		return ComProtocolCreator.New();
	}
	
	public static ComProtocol New(
		final String                        name             ,
		final String                        version          ,
		final ByteOrder                     byteOrder        ,
		final int                           inactivityTimeout,
		final PersistenceIdStrategy         idStrategy       ,
		final PersistenceTypeDictionaryView persistenceTypeDictionaryView
	)
	{
		return new ComProtocol.Default(
			notNull(name)      ,
			notNull(version)   ,
			notNull(byteOrder) ,
			inactivityTimeout  ,
			notNull(idStrategy),
			persistenceTypeDictionaryView
		);
	}
	
	public final class Default implements ComProtocol, Immutable
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final String                        name             ;
		private final String                        version          ;
		private final ByteOrder                     byteOrder        ;
		private final int                           inactivityTimeOut;
		private final PersistenceIdStrategy         idStrategy       ;
		private final PersistenceTypeDictionaryView typeDictionary   ;
		
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Default(
			final String                        name             ,
			final String                        version          ,
			final ByteOrder                     byteOrder        ,
			final int                           inactivityTimeOut,
			final PersistenceIdStrategy         idStrategy       ,
			final PersistenceTypeDictionaryView typeDictionary
			
		)
		{
			super();
			this.name              = name             ;
			this.version           = version          ;
			this.byteOrder         = byteOrder        ;
			this.inactivityTimeOut = inactivityTimeOut;
			this.idStrategy        = idStrategy       ;
			this.typeDictionary    = typeDictionary   ;
			
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final String name()
		{
			return this.name;
		}

		@Override
		public final String version()
		{
			return this.version;
		}

		@Override
		public final ByteOrder byteOrder()
		{
			return this.byteOrder;
		}

		@Override
		public final PersistenceIdStrategy idStrategy()
		{
			return this.idStrategy;
		}
		
		@Override
		public final PersistenceTypeDictionaryView typeDictionary()
		{
			return this.typeDictionary;
		}
		
		@Override
		public final int inactivityTimeout()
		{
			return this.inactivityTimeOut;
		}
		
	}
		
}
