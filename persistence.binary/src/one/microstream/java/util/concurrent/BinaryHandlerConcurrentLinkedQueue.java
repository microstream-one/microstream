package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

import one.microstream.java.util.BinaryHandlerQueue;


public final class BinaryHandlerConcurrentLinkedQueue extends BinaryHandlerQueue<ConcurrentLinkedQueue<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedQueue<?>> typeWorkaround()
	{
		return (Class)ConcurrentLinkedQueue.class; // no idea how to get ".class" to work otherwise
	}
	
	public static final ConcurrentLinkedQueue<?> instantiate(final long elementCount)
	{
		return new ConcurrentLinkedQueue<>();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentLinkedQueue()
	{
		super(
			typeWorkaround(),
			BinaryHandlerConcurrentLinkedQueue::instantiate
		);
	}
	
}
