package one.microstream.java.util;

import java.util.WeakHashMap;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerWeakHashMap extends AbstractBinaryHandlerMap<WeakHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<WeakHashMap<?, ?>> handledType()
	{
		return (Class)WeakHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerWeakHashMap()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final WeakHashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new WeakHashMap<>();
	}

}
