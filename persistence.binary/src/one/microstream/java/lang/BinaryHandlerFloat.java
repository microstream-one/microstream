package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerFloat extends AbstractBinaryHandlerCustomValueFixedLength<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerFloat()
	{
		super(Float.class, defineValueType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Float instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeFloat(this.typeId(), objectId, instance.floatValue());
	}

	@Override
	public Float create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildFloat();
	}

}
