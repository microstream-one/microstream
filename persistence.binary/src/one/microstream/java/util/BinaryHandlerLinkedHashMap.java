package one.microstream.java.util;

import java.util.LinkedHashMap;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerLinkedHashMap extends AbstractBinaryHandlerMap<LinkedHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedHashMap<?, ?>> handledType()
	{
		return (Class)LinkedHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLinkedHashMap()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedHashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedHashMap<>(
			X.checkArrayRange(getElementCount(bytes))
		);
	}

}
