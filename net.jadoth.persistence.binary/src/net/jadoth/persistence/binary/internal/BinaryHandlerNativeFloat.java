package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;

public final class BinaryHandlerNativeFloat extends AbstractBinaryHandlerNativeCustomValueFixedLength<Float>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeFloat(final long tid)
	{
		super(tid, Float.class, defineValueType(float.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Float instance, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeFloat(bytes, this.typeId(), oid, instance.floatValue());
	}

	@Override
	public Float create(final Binary bytes)
	{
		return BinaryPersistence.buildFloat(bytes);
	}

}
