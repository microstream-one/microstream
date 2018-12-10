package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeInteger extends AbstractBinaryHandlerNativeCustomValueFixedLength<Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeInteger()
	{
		super(Integer.class, defineValueType(int.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Integer instance, final long oid, final PersistenceStoreHandler handler)
	{
		BinaryPersistence.storeInteger(bytes, this.typeId(), oid, instance.intValue());
	}

	@Override
	public Integer create(final Binary bytes)
	{
		return BinaryPersistence.buildInteger(bytes);
	}

}
