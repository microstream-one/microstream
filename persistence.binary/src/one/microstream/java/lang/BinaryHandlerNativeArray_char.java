package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_char extends AbstractBinaryHandlerNativeArrayPrimitive<char[]>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeArray_char()
	{
		super(char[].class, defineElementsType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final char[] array, final long oid, final PersistenceStoreHandler handler)
	{
		bytes.storeArray_char(this.typeId(), oid, array);
	}

	@Override
	public char[] create(final Binary bytes)
	{
		return bytes.createArray_char();
	}

	@Override
	public void update(final Binary bytes, final char[] instance, final PersistenceLoadHandler builder)
	{
		bytes.updateArray_char(instance);
	}

}
