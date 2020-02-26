package one.microstream.java.net;

import java.net.URI;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerURI extends AbstractBinaryHandlerCustomValueVariableLength<URI>
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
		final Binary                  data    ,
		final URI                     instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
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
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final URI                    instance,
		final PersistenceLoadHandler handler
	)
	{
		compareSimpleState(instance, instanceState(instance), binaryState(data));
	}

}
