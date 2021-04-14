package one.microstream.persistence.binary.java.lang;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
	public void store(
		final Binary                          data    ,
		final char[]                          array   ,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler)
	{
		data.store_chars(this.typeId(), objectId, array);
	}

	@Override
	public char[] create(final Binary data, final PersistenceLoadHandler handler)
	{
		return data.create_chars();
	}

	@Override
	public void updateState(final Binary data, final char[] instance, final PersistenceLoadHandler handler)
	{
		data.update_chars(instance);
	}

}
