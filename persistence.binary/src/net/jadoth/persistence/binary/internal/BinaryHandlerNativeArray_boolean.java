package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_boolean extends AbstractBinaryHandlerNativeArrayPrimitive<boolean[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_boolean()
	{
		super(boolean[].class, defineElementsType(boolean.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(final Binary bytes, final boolean[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_boolean(this.typeId(), oid, array);
	}

	@Override
	public final boolean[] create(final Binary bytes)
	{
		return bytes.createArray_boolean();
	}

	@Override
	public final void update(final Binary bytes, final boolean[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_boolean(instance);
	}

}
