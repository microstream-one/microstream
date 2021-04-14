package one.microstream.persistence.binary.java.util;

import java.util.HashSet;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerHashSet extends AbstractBinaryHandlerSet<HashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<HashSet<?>> handledType()
	{
		return (Class)HashSet.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerHashSet New()
	{
		return new BinaryHandlerHashSet();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashSet()
	{
		super(
			handledType()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final HashSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new HashSet<>();
	}

}
