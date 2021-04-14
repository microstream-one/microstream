package one.microstream.persistence.binary.java.util;

import java.util.LinkedHashSet;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerLinkedHashSet extends AbstractBinaryHandlerSet<LinkedHashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedHashSet<?>> handledType()
	{
		return (Class)LinkedHashSet.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerLinkedHashSet New()
	{
		return new BinaryHandlerLinkedHashSet();
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLinkedHashSet()
	{
		super(
			handledType()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedHashSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new LinkedHashSet<>();
	}

}
