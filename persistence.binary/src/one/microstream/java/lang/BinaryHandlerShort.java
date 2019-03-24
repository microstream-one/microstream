package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerShort extends AbstractBinaryHandlerCustomValueFixedLength<Short>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerShort()
	{
		super(Short.class, defineValueType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Short instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeShort(this.typeId(), oid, instance.shortValue());
	}

	@Override
	public Short create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildShort();
	}

}
