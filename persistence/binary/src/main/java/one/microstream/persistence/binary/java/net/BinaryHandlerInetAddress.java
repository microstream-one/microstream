package one.microstream.persistence.binary.java.net;

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
