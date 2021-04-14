package one.microstream.persistence.binary.java.util;

import java.util.ArrayList;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerArrayList extends AbstractBinaryHandlerList<ArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ArrayList<?>> handledType()
	{
		return (Class)ArrayList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerArrayList New()
	{
		return new BinaryHandlerArrayList();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerArrayList()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final ArrayList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long elementCount = getElementCount(data);
		
		/*
		 * InitialCapacity 1 instead of default constructor is a workaround for yet another JDK bug.
		 * Using the default constructor causes #ensureCapacity to yield incorrect behavior for values of
		 * 10 or below, which causes a subsequent array length validation exception.
		 * Also see https://bugs.openjdk.java.net/browse/JDK-8206945
		 * 
		 * However, having an actually zero-size instance should still cause the internal dummy array instance
		 * to be used instead of a redundant one that unnecessarily occupies memory. Hence the if.
		 */
		return elementCount == 0
			? new ArrayList<>(0)
			: new ArrayList<>(1)
		;
	}

}
