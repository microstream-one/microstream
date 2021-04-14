package one.microstream.persistence.binary.java.net;

import java.net.MalformedURLException;
import java.net.URL;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerURL extends AbstractBinaryHandlerCustomValueVariableLength<URL, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerURL New()
	{
		return new BinaryHandlerURL();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerURL()
	{
		super(
			URL.class,
			CustomFields(
				chars("address")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final URL instance)
	{
		return instance.toExternalForm();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final URL                             instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public URL create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		try
		{
			return new URL(binaryState(data));
		}
		catch(final MalformedURLException e)
		{
			throw new PersistenceException(
				"Invalid data for " + this.toTypeIdentifier()
				+ ", ObjectId " + data.getBuildItemObjectId()
				+ ". Register a custom handler based on "
				+ this.getClass() + " to investigate / compensate.",
				e
			);
		}
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final URL instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
