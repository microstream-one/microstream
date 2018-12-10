package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeString extends AbstractBinaryHandlerNativeCustomValueVariableLength<String>
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
		BinaryPersistence.storeStringValue(bytes, this.typeId(), oid, instance);
	}

	@Override
	public String create(final Binary bytes)
	{
		return BinaryPersistence.buildString(bytes);
	}

}
