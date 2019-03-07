package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeFloat extends AbstractBinaryHandlerNativeCustomValueFixedLength<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeFloat()
	{
		super(Float.class, defineValueType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Float instance, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeFloat(this.typeId(), oid, instance.floatValue());
	}

	@Override
	public Float create(final Binary bytes)
	{
		return bytes.buildFloat();
	}

}
