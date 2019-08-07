package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerFloat extends AbstractBinaryHandlerCustomValueFixedLength<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerFloat New()
	{
		return new BinaryHandlerFloat();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerFloat()
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
	public Float create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.buildFloat();
	}

}
