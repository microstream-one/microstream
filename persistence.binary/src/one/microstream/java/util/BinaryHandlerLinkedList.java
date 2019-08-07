package one.microstream.java.util;

import java.util.LinkedList;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;


public final class BinaryHandlerLinkedList extends AbstractBinaryHandlerList<LinkedList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedList<?>> handledType()
	{
		return (Class)LinkedList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerLinkedList New()
	{
		return new BinaryHandlerLinkedList();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLinkedList()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedList<?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new LinkedList<>();
	}

}
