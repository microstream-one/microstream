package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerString extends AbstractBinaryHandlerCustomValueVariableLength<String>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerString New()
	{
		return new BinaryHandlerString();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerString()
	{
		super(
			String.class,
			CustomFields(
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(
		final Binary                  data    ,
		final String                  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		data.storeStringSingleValue(this.typeId(), objectId, instance);
	}

	@Override
	public String create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.buildString();
	}
	
	@Override
	public void validateState(
		final Binary                 data    ,
		final String                 instance,
		final PersistenceLoadHandler handler
	)
	{
		final String binaryState = data.buildString();
		
		if(instance.equals(binaryState))
		{
			return;
		}
		
		throwInconsistentStateException(instance, instance, binaryState);
	}

}
