package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

public final class BinaryHandlerNativeArray_byte extends AbstractBinaryHandlerNativeArrayPrimitive<byte[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_byte()
	{
		super(byte[].class, defineElementsType(byte.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final byte[] array, final long oid, final PersistenceHandler handler)
	{
		BinaryPersistence.storeArray_byte(bytes, this.typeId(), oid, array);
	}

	@Override
	public byte[] create(final Binary bytes)
	{
		return BinaryPersistence.createArray_byte(bytes);
	}

	@Override
	public void update(final Binary bytes, final byte[] instance, final PersistenceBuildLinker builder)
	{
		BinaryPersistence.updateArray_byte(instance, bytes);
	}

}
