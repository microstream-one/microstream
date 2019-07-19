package one.microstream.java.util;

import java.util.HashSet;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerHashSet extends AbstractBinaryHandlerSet<HashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashSet<?>> handledType()
	{
		return (Class)HashSet.class; // no idea how to get ".class" to work otherwise
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerHashSet()
	{
		super(
			handledType()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final HashSet<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new HashSet<>();
	}

}
