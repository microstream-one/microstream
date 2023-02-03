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

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public abstract class AbstractBinaryHandlerInetAddress<A extends InetAddress>
extends AbstractBinaryHandlerCustomValueVariableLength<A, String>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// Inet4Address#INADDRSZ ... "
	static final int
		IPV4_BYTE_SIZE =  4,
		IPV6_BYTE_SIZE = 16,
		HEX_RADIX      = 16  // this 16 is not the other 16. It's 10+6 instead of 8*2 :-D.
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static byte[] parseIpAddress(final String ipAddress)
	{
		if(XChars.contains(ipAddress, '.'))
		{
			if(!XChars.contains(ipAddress, ':'))
			{
				return parseIpV4Address(ipAddress);
			}
			// otherwise fall through to exception
		}
		else if(XChars.contains(ipAddress, ':'))
		{
			return parseIpV6Address(ipAddress);
		}
		
		throw new BinaryPersistenceException("Unknown IP address pattern: \"" + ipAddress + '"');
	}
	
	public static byte[] parseIpV4Address(final String ipV4Address)
	{
		final String[] parts = ipV4Address.split("\\.");
		try
		{
			if(parts.length != IPV4_BYTE_SIZE)
			{
				throw new IllegalArgumentException(
					"An IPv4 address must be formed of " + IPV4_BYTE_SIZE + " values separated by '.'."
				);
			}
			
			final byte[] address = new byte[parts.length];
			for(int i = 0; i < parts.length; i++)
			{
				address[i] = (byte)(Integer.parseInt(parts[i]) & 0xFF);
			}
			return address;
		}
		catch(final NumberFormatException e)
		{
			throw new IllegalArgumentException("Invalid IP V4 address: \"" + ipV4Address + '"');
		}
	}
	
	/**
	 * Based on <a href="https://en.wikipedia.org/wiki/IPv6">https://en.wikipedia.org/wiki/IPv6</a>.
	 * 
	 * @param ipV6Address the address to parse
	 * @return the parts of the ip address
	 */
	public static byte[] parseIpV6Address(final String ipV6Address)
	{
		final String[] parts = ipV6Address.split("\\:");
		final byte[] address = new byte[IPV6_BYTE_SIZE];
		
		for(int i = 0, a = 0; i < parts.length; i++)
		{
			final String p = parts[i];
			if(p.isEmpty())
			{
				// empty part means "::" in the input string, means omitted zeroes.
				return parseRestOfIpV6AddressAfterOmittedZeroes(ipV6Address, parts, i + 1, address);
			}
			
			parsePart(p, address, a);
			a += 2;
		}
		
		return address;
	}
	
	private static void parsePart(final String p, final byte[] address, final int a)
	{
		if(p.length() < 3)
		{
			// single byte case
			address[a    ] = (byte)0;
			address[a + 1] = (byte)(Integer.parseInt(p, HEX_RADIX) & 0xFF);
		}
		else
		{
			// dual byte case
			address[a    ] = (byte)(Integer.parseInt(p.substring(0, p.length() - 2), HEX_RADIX) & 0xFF);
			address[a + 1] = (byte)(Integer.parseInt(p.substring(p.length() - 2), HEX_RADIX) & 0xFF);
		}
	}
	
	private static byte[] parseRestOfIpV6AddressAfterOmittedZeroes(
		final String   ipV6Address, // just for the exception, lol
		final String[] parts      ,
		final int      partsIndex ,
		final byte[]   address
	)
	{
		/* Rationale:
		 * There can be only one "::", so after it, the remaining bytes MUST be in synch with the remaining parts.
		 */
		
		// re-synchronize a counting from the end.
		int a = IPV6_BYTE_SIZE - (parts.length - partsIndex) * 2;
		for(int i = partsIndex; i < parts.length; i++)
		{
			final String p = parts[i];
			if(p.isEmpty())
			{
				throw new IllegalArgumentException("Invalid IP V6 address: \"" + ipV6Address + '"');
			}
			
			parsePart(p, address, a);
			a += 2;
		}
		
		return address;
	}
		
		
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	AbstractBinaryHandlerInetAddress(final Class<A> type)
	{
		super(
			type,
			CustomFields(
				chars("address")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final InetAddress instance)
	{
		/*
		 * Since #toString is usually not a productively usable representation of an instance's data, since it is usually
		 * incomplete, inefficient, ambiguous, etc. So it is normally a horrible idea to rely on it to persist
		 * an instance's state. However, wih InetAddress, the designer in charge decided to make EVERY getter
		 * try to resolve the host instead of just returning the data that was used to specify the address.
		 * Every except one: #toString.
		 */
		return instance.toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final A                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public A create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		final String persistedString = binaryState(data);
		
		// According to the logic in InetAddress#toString, there is always a '/'.
		final int    slashIndex   = persistedString.indexOf('/');
		final String hostNamePart = slashIndex == 0 ? null : persistedString.substring(0, slashIndex);
		final String addressPart  = persistedString.substring(slashIndex + 1);

		// overridden to specify either only v4 or only v6.
		return this.createInstance(hostNamePart, addressPart);
	}
	
	protected abstract A createInstance(String hostNamePart, String addressPart);
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final InetAddress instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
