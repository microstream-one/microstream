package one.microstream.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerNativeArray_char extends AbstractBinaryHandlerNativeArrayPrimitive<char[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerNativeArray_char New()
	{
		return new BinaryHandlerNativeArray_char();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerNativeArray_char()
	{
		super(char[].class, defineElementsType(char.class));
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(final Binary bytes, final char[] array, final long objectId, final PersistenceStoreHandler handler)
	{
		bytes.store_chars(this.typeId(), objectId, array);
	}

	@Override
	public char[] create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return bytes.create_chars();
	}

	@Override
	public void update(final Binary bytes, final char[] instance, final PersistenceObjectIdResolver idResolver)
	{
		bytes.update_chars(instance);
	}

}
