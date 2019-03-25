package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentHashMap;

import one.microstream.X;
import one.microstream.java.util.BinaryHandlerMap;


public final class BinaryHandlerConcurrentHashMap extends BinaryHandlerMap<ConcurrentHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentHashMap<?, ?>> typeWorkaround()
	{
		return (Class)ConcurrentHashMap.class; // no idea how to get ".class" to work otherwise
	}
	
	public static final ConcurrentHashMap<?, ?> instantiate(final long elementCount)
	{
		return new ConcurrentHashMap<>(
			X.checkArrayRange(elementCount)
		);
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentHashMap()
	{
		super(
			typeWorkaround(),
			BinaryHandlerConcurrentHashMap::instantiate
		);
	}
	
}
