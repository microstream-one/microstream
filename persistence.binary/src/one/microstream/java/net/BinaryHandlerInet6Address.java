package one.microstream.java.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import one.microstream.persistence.exceptions.PersistenceException;

public class BinaryHandlerInet6Address extends AbstractBinaryHandlerInetAddress<Inet6Address>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerInet6Address New()
	{
		return new BinaryHandlerInet6Address();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerInet6Address()
	{
		super(Inet6Address.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	@Override
	protected Inet6Address createInstance(final String hostNamePart, final String addressPart)
	{
		final byte[] address = parseIpV6Address(addressPart);
				
		// sadly, the morons did not provide a method that _just_ creates an unresolved instance.
		try
		{
			// they never got how to design stuff so that no dangerous casts are necessary. Idiots.
			return (Inet6Address)InetAddress.getByAddress(hostNamePart, address);
		}
		catch(final UnknownHostException e)
		{
			throw new PersistenceException(e);
		}
	}
	
}
