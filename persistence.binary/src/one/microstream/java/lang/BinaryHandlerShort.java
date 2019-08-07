package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerShort extends AbstractBinaryHandlerCustomValueFixedLength<Short>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerShort New()
	{
		return new BinaryHandlerShort();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerShort()
	{
		super(Short.class, defineValueType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Short instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeShort(this.typeId(), objectId, instance.shortValue());
	}

	@Override
	public Short create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.buildShort();
	}

}
