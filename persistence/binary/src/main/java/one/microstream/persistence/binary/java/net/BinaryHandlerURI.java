package one.microstream.persistence.binary.java.net;

import java.net.URI;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerURI extends AbstractBinaryHandlerCustomValueVariableLength<URI, String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerURI New()
	{
		return new BinaryHandlerURI();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerURI()
	{
		super(
			URI.class,
			CustomFields(
				chars("address")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	private static String instanceState(final URI instance)
	{
		return instance.toString();
	}
	
	private static String binaryState(final Binary data)
	{
		return data.buildString();
	}

	@Override
	public final void store(
		final Binary                          data    ,
		final URI                             instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instanceState(instance));
	}

	@Override
	public URI create(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return URI.create(binaryState(data));
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// validation //
	///////////////
	
	@Override
	public String getValidationStateFromInstance(final URI instance)
	{
		return instanceState(instance);
	}
	
	@Override
	public String getValidationStateFromBinary(final Binary data)
	{
		return binaryState(data);
	}

}
