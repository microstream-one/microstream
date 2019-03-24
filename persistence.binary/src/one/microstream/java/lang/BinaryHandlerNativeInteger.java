package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeInteger extends AbstractBinaryHandlerCustomValueFixedLength<Integer>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

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
		bytes.storeInteger(this.typeId(), oid, instance.intValue());
	}

	@Override
	public Integer create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildInteger();
	}

}
