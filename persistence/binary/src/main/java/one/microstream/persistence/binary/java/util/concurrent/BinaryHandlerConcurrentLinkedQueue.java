package one.microstream.persistence.binary.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

import one.microstream.persistence.binary.java.util.AbstractBinaryHandlerQueue;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentLinkedQueue
extends AbstractBinaryHandlerQueue<ConcurrentLinkedQueue<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedQueue<?>> handledType()
	{
		return (Class)ConcurrentLinkedQueue.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerConcurrentLinkedQueue New()
	{
		return new BinaryHandlerConcurrentLinkedQueue();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentLinkedQueue()
	{
		super(
			handledType()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ConcurrentLinkedQueue<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ConcurrentLinkedQueue<>();
	}
	
}
