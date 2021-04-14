package one.microstream.persistence.binary.java.util;

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
	
	public static BinaryHandlerWeakHashMap New()
	{
		return new BinaryHandlerWeakHashMap();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerWeakHashMap()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final WeakHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new WeakHashMap<>();
	}

}
