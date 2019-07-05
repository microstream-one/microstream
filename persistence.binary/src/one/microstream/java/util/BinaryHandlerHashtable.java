package one.microstream.java.util;

import java.util.Hashtable;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerHashtable extends AbstractBinaryHandlerMap<Hashtable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Hashtable<?, ?>> typeWorkaround()
	{
		return (Class)Hashtable.class; // no idea how to get ".class" to work otherwise
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerHashtable()
	{
		super(
			typeWorkaround()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Hashtable<?, ?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new Hashtable<>(
			X.checkArrayRange(getElementCount(bytes))
		);
	}
	
}
