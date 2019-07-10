package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerString extends AbstractBinaryHandlerCustomValueVariableLength<String>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerString()
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
		final Binary                  bytes   ,
		final String                  instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		bytes.storeStringValue(this.typeId(), objectId, instance);
	}

	@Override
	public String create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildString();
	}

}
