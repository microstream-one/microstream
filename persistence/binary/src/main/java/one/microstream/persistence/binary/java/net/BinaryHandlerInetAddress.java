package one.microstream.persistence.binary.java.net;

/*-
 * #%L
 * microstream-persistence-binary
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import one.microstream.persistence.exceptions.PersistenceException;

public class BinaryHandlerInetAddress extends AbstractBinaryHandlerInetAddress<InetAddress>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInetAddress New()
	{
		return new BinaryHandlerInetAddress();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInetAddress()
	{
		super(InetAddress.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	protected InetAddress createInstance(final String hostNamePart, final String addressPart)
	{
		final byte[] address = parseIpAddress(addressPart);
				
		// sadly, they did not provide a method that _just_ creates an unresolved instance.
		try
		{
			return InetAddress.getByAddress(hostNamePart, address);
		}
		catch(final UnknownHostException e)
		{
			throw new PersistenceException(e);
		}
	}
	
}
