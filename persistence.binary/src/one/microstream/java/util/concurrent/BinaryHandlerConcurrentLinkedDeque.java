package one.microstream.java.util.concurrent;

import java.util.concurrent.ConcurrentLinkedDeque;

import one.microstream.java.util.AbstractBinaryHandlerQueue;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerConcurrentLinkedDeque extends AbstractBinaryHandlerQueue<ConcurrentLinkedDeque<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<ConcurrentLinkedDeque<?>> typeWorkaround()
	{
		return (Class)ConcurrentLinkedDeque.class; // no idea how to get ".class" to work otherwise
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerConcurrentLinkedDeque()
	{
		super(
			typeWorkaround()
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public ConcurrentLinkedDeque<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new ConcurrentLinkedDeque<>();
	}
	
}
