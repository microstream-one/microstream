package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;

import one.microstream.java.util.AbstractBinaryHandlerQueue;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentLinkedQueue
extends AbstractBinaryHandlerQueue<ConcurrentLinkedQueue<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedQueue<?>> typeWorkaround()
	{
		return (Class)ConcurrentLinkedQueue.class; // no idea how to get ".class" to work otherwise
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentLinkedQueue()
	{
		super(
			typeWorkaround()
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public ConcurrentLinkedQueue<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new ConcurrentLinkedQueue<>();
	}
	
}
