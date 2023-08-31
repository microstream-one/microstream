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

import javax.net.ssl.SSLParameters;

public interface TLSParametersProvider
{
	/**
	 * Provides the SSLParameters Object for the SSLEngine
	 * 
	 * @return SSLParameters
	 */
	SSLParameters getSSLParameters();
	
	/**
	 * provide the SSL protocol as defined in <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">Standard Algorithm Name Documentation</a>
	 * 
	 * @return SSL protocol
	 */
	String getSSLProtocol();

	/**
	 * Timeout for read operations during the TLS handshake in milliseconds
	 * 
	 * @return returns the timeout for the TLS handshake
	 */
	int getHandshakeReadTimeOut();
	
	/**
	 * 
	 * Provides a nearly empty SSLParameters object.
	 * <p>
	 * all configuration values are null except
	 * <p>
	 * needClientAuth = true
	 *
	 */
	public final class Default implements TLSParametersProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// constants //
		//////////////
		
		private static final String TLS_PROTOCOL_STRING        = "TLSv1.2";
		private static final int    SSL_HANDSHAKE_READ_TIMEOUT = 1000;
		
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
		public SSLParameters getSSLParameters()
		{
			final SSLParameters sslParameters = new SSLParameters();
			sslParameters.setNeedClientAuth(true);
						
			return sslParameters;
		}
		
		@Override
		public String getSSLProtocol()
		{
			return Default.TLS_PROTOCOL_STRING;
		}


		@Override
		public int getHandshakeReadTimeOut()
		{
			return SSL_HANDSHAKE_READ_TIMEOUT;
		}
		
	}

}
