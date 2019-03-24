package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedDeque;

import one.microstream.java.util.BinaryHandlerQueue;


public final class BinaryHandlerConcurrentLinkedDeque extends BinaryHandlerQueue<ConcurrentLinkedDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedDeque<?>> typeWorkaround()
	{
		return (Class)ConcurrentLinkedDeque.class; // no idea how to get ".class" to work otherwise
	}
	
	public static final ConcurrentLinkedDeque<?> instantiate(final long elementCount)
	{
		return new ConcurrentLinkedDeque<>();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentLinkedDeque()
	{
		super(
			typeWorkaround(),
			BinaryHandlerConcurrentLinkedDeque::instantiate
		);
	}
	
}
