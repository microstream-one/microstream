package one.microstream.persistence.binary.java.util;

import java.util.Stack;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerStack extends AbstractBinaryHandlerList<Stack<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Stack<?>> handledType()
	{
		return (Class)Stack.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerStack New()
	{
		return new BinaryHandlerStack();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStack()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Stack<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Stack<>();
	}

}
