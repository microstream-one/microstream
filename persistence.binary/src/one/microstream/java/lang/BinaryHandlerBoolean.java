package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomValueFixedLength;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerBoolean extends AbstractBinaryHandlerCustomValueFixedLength<Boolean>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerBoolean New()
	{
		return new BinaryHandlerBoolean();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerBoolean()
	{
		super(Boolean.class, defineValueType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Boolean instance, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.storeBoolean(this.typeId(), objectId, instance.booleanValue());
	}

	@Override
	public Boolean create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return bytes.buildBoolean();
	}

}
