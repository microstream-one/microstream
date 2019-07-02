package one.microstream.java.util;

import java.util.LinkedHashSet;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public class BinaryHandlerLinkedHashSet extends AbstractBinaryHandlerSet<LinkedHashSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<LinkedHashSet<?>> typeWorkaround()
	{
		return (Class)LinkedHashSet.class; // no idea how to get ".class" to work otherwise
	}


	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerLinkedHashSet()
	{
		super(
			typeWorkaround()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final LinkedHashSet<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new LinkedHashSet<>();
	}

}
