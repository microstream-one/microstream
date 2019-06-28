package one.microstream.java.util;

import java.util.HashMap;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerHashMap extends AbstractBinaryHandlerMap<HashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashMap<?, ?>> typeWorkaround()
	{
		return (Class)HashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerHashMap()
	{
		super(typeWorkaround());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final HashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new HashMap<>();
	}

}
