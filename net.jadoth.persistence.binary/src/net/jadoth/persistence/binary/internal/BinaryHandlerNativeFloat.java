package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

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
	public void store(final Binary bytes, final Float instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeFloat(bytes, this.typeId(), oid, instance.floatValue());
	}

	@Override
	public Float create(final Binary bytes)
	{
		return BinaryPersistence.buildFloat(bytes);
	}

}
