package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceHandler;

public final class BinaryHandlerNativeDouble extends AbstractBinaryHandlerNativeCustomValueFixedLength<Double>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeDouble()
	{
		super(Double.class, defineValueType(double.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Double instance, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeDouble(bytes, this.typeId(), oid, instance.doubleValue());
	}

	@Override
	public Double create(final Binary bytes)
	{
		return BinaryPersistence.buildDouble(bytes);
	}

}
