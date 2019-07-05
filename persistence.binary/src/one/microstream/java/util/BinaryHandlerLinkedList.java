package one.microstream.java.util;

import java.util.LinkedList;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerLinkedList extends AbstractBinaryHandlerList<LinkedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedList<?>> typeWorkaround()
	{
		return (Class)LinkedList.class; // no idea how to get ".class" to work otherwise
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLinkedList()
	{
		super(typeWorkaround());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedList<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedList<>();
	}

}
