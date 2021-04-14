package one.microstream.persistence.binary.java.net;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import one.microstream.persistence.exceptions.PersistenceException;

public class BinaryHandlerInet4Address extends AbstractBinaryHandlerInetAddress<Inet4Address>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInet4Address New()
	{
		return new BinaryHandlerInet4Address();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInet4Address()
	{
		super(Inet4Address.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	protected Inet4Address createInstance(final String hostNamePart, final String addressPart)
	{
		final byte[] address = parseIpV4Address(addressPart);
				
		// sadly, they did not provide a method that _just_ creates an unresolved instance.
		try
		{
			// they never got how to design stuff so that no dangerous casts are necessary.
			return (Inet4Address)InetAddress.getByAddress(hostNamePart, address);
		}
		catch(final UnknownHostException e)
		{
			throw new PersistenceException(e);
		}
	}
	
}
