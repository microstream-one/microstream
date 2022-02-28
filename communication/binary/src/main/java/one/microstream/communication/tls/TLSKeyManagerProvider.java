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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import one.microstream.com.ComException;

public interface TLSKeyManagerProvider
{
	KeyManager[] get();
	
	/**
	 * uses system default key manager
	 */
	public class Default implements TLSKeyManagerProvider
	{
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
		public KeyManager[] get()
		{
			return null;
		}
	}
	
	/**
	 * 
	 * Provide a PKCS12 KeyManger
	 *
	 */
	public class PKCS12 implements TLSKeyManagerProvider
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final KeyManagerFactory keyManagerFactory;
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		public PKCS12(final Path path, final char[] password)
		{
			final KeyStore keyStore;
			
			try
			{
				keyStore = KeyStore.getInstance("pkcs12");
			}
			catch (final KeyStoreException e)
			{
				throw new ComException("failed to create KeyStore instance", e);
			}
						
			try
			{
				keyStore.load(new FileInputStream(path.toString()), password);
				
				this.keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
				
				try
				{
					this.keyManagerFactory.init(keyStore, password);
				}
				catch (UnrecoverableKeyException | KeyStoreException e)
				{
					throw new ComException("failed to initializeKey ManagerFactory", e);
				}
				
			}
			catch (NoSuchAlgorithmException | CertificateException | IOException e)
			{
				throw new ComException("failed to load keys from file", e);
			}
		}

		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public KeyManager[] get()
		{
			return this.keyManagerFactory.getKeyManagers();
		}
		
	}
}
