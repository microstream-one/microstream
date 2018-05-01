package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;

public final class BinaryHandlerNativeShort extends AbstractBinaryHandlerNativeCustomValueFixedLength<Short>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeShort(final long tid)
	{
		super(tid, Short.class, defineValueType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final Short instance, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeShort(bytes, this.typeId(), oid, instance.shortValue());
	}

	@Override
	public Short create(final Binary bytes)
	{
		return BinaryPersistence.buildShort(bytes);
	}

}
