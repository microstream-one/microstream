package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueVariableLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeString extends AbstractBinaryHandlerCustomValueVariableLength<String>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeString()
	{
		super(
			String.class,
			pseudoFields(
				chars("value")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final String instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeStringValue(this.typeId(), oid, instance);
	}

	@Override
	public String create(final Binary bytes)
	{
		return bytes.buildString();
	}

}
