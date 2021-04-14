package one.microstream.persistence.binary.java.util;

import java.util.concurrent.CopyOnWriteArrayList;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerCopyOnWriteArrayList extends AbstractBinaryHandlerList<CopyOnWriteArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<CopyOnWriteArrayList<?>> handledType()
	{
		return (Class)CopyOnWriteArrayList.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerCopyOnWriteArrayList New()
	{
		return new BinaryHandlerCopyOnWriteArrayList();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerCopyOnWriteArrayList()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final CopyOnWriteArrayList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new CopyOnWriteArrayList<>();
	}

}
