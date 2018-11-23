package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

public final class BinaryHandlerNativeArray_short extends AbstractBinaryHandlerNativeArrayPrimitive<short[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_short()
	{
		super(short[].class, defineElementsType(short.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final short[] array, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeArray_short(bytes, this.typeId(), oid, array);
	}

	@Override
	public short[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_short(bytes);
	}

	@Override
	public void update(final Binary bytes, final short[] instance, final PersistenceBuildLinker builder)
	{
		BinaryPersistence.updateArray_short(instance, bytes);
	}
}
