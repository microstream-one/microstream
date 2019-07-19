package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

import one.microstream.X;
import one.microstream.java.util.AbstractBinaryHandlerMap;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentHashMap extends AbstractBinaryHandlerMap<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentHashMap<?, ?>> handledType()
	{
		return (Class)ConcurrentHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentHashMap()
	{
		super(
			handledType()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ConcurrentHashMap<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new ConcurrentHashMap<>(
			X.checkArrayRange(getElementCount(bytes))
		);
	}
	
}
