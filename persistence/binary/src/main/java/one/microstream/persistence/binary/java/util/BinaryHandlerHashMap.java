package one.microstream.persistence.binary.java.util;

import java.util.HashMap;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerHashMap extends AbstractBinaryHandlerMap<HashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashMap<?, ?>> handledType()
	{
		return (Class)HashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerHashMap New()
	{
		return new BinaryHandlerHashMap();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashMap()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final HashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new HashMap<>();
	}

}
