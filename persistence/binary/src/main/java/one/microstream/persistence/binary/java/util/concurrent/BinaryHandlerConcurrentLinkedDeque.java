package one.microstream.persistence.binary.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedDeque;

import one.microstream.persistence.binary.java.util.AbstractBinaryHandlerQueue;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentLinkedDeque extends AbstractBinaryHandlerQueue<ConcurrentLinkedDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedDeque<?>> handledType()
	{
		return (Class)ConcurrentLinkedDeque.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerConcurrentLinkedDeque New()
	{
		return new BinaryHandlerConcurrentLinkedDeque();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConcurrentLinkedDeque()
	{
		super(
			handledType()
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ConcurrentLinkedDeque<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new ConcurrentLinkedDeque<>();
	}
	
}
