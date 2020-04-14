package one.microstream.java.net;

import java.net.InetAddress;

import one.microstream.chars.XChars;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public abstract class AbstractBinaryHandlerInetAddress<A extends InetAddress>
extends AbstractBinaryHandlerCustomValueVariableLength<A, String>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	// Inet4Address#INADDRSZ ... "Bcs we too stpd t wrt prpr idntifiours or mk thm pblcly accssble." Damn idiots.
	static final int
		IPV4_BYTE_SIZE =  4,
		IPV6_BYTE_SIZE = 16,
		HEX_RADIX      = 16  // this 16 is not the other 16, you know? It's 10+6 instead of 8*2 :-D.
	;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	// and of course the InetAddress moron did not create a method to parse his own string representation. Why would he?
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
		
		// (02.03.2020 TM)EXCP: proper exception
		throw new PersistenceException("Unknown IP address pattern: \"" + ipAddress + '"');
	}
	
	// (02.03.2020 TM)NOTE: because I have nothing better to do than writing an IP address parser. Yay.
	public static byte[] parseIpV4Address(final String ipV4Address)
	{
		final String[] parts = ipV4Address.split("\\.");
		try
		{
			if(parts.length != IPV4_BYTE_SIZE)
			{
				// (02.03.2020 TM)EXCP: proper exception
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
			// (02.03.2020 TM)EXCP: proper exception
			throw new PersistenceException("Invalid IP V4 address: \"" + ipV4Address + '"');
		}
	}
	
	/**
	 * Based on <a href="https://en.wikipedia.org/wiki/IPv6">https://en.wikipedia.org/wiki/IPv6</a>.
	 * 
	 * @param ipV6Address
	 * @return
	 */
	// (02.03.2020 TM)NOTE: because I have nothing better to do than writing an IP address parser. Yay.
	public static byte[] parseIpV6Address(final String ipV6Address)
	{
		final String[] parts = ipV6Address.split("\\:");
		final byte[] address = new byte[IPV6_BYTE_SIZE];
		
		for(int i = 0, a = 0; i < parts.length; i++)
		{
			final String p = parts[i];
			if(p.isEmpty())
			{
				// empty part means "::" in the input string, means ommitted zeroes.
				return parseRestOfIpV6AddressAfterOmittedZeroesBecauseNothingEverIsEasy(ipV6Address, parts, i + 1, address);
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
	
	private static byte[] parseRestOfIpV6AddressAfterOmittedZeroesBecauseNothingEverIsEasy(
		final String   ipV6Address, // just for the exception, lol
		final String[] parts      ,
		final int      partsIndex ,
		final byte[]   address
	)
	{
		/* Rationale:
		 * There can be only one "::", so after it, the remainig bytes MUST be in synch with the remaining parts.
		 */
		
		// re-synchronize a counting from the end.
		int a = IPV6_BYTE_SIZE - (parts.length - partsIndex) * 2;
		for(int i = partsIndex; i < parts.length; i++)
		{
			final String p = parts[i];
			if(p.isEmpty())
			{
				// (02.03.2020 TM)EXCP: proper exception
				throw new PersistenceException("Invalid IP V6 address: \"" + ipV6Address + '"');
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
		 * Since #toString is usually not a productively usable representation of an instance's data, since it usually
		 * is incomplete, inefficient, ambiguous, etc. So it is normally a horrible idea to rely on it to persist
		 * an instance's state. However, wih InetAddress, the designing moron in charge decided to make EVERY getter
		 * try to resolve the host instead of just returning the data that was used to specify the address.
		 * Every except one: #toString.
		 * Since the class is also horribly bad designed in countless other ways, there's no way to properly
		 * query the specifying data without getting in dependencies to JDK-version-specific implementation.
		 * So the only left option is the very bad one of using #toString.
		 * Once again, for the like 100th time or so: JDK idiocy at work.
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
		// for once, they managed to do a kind of proper de/serialization logic. Amazing.
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
