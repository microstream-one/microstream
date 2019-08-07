package one.microstream.java.util;

import java.util.Hashtable;

import one.microstream.X;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;


public final class BinaryHandlerHashtable extends AbstractBinaryHandlerMap<Hashtable<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Hashtable<?, ?>> handledType()
	{
		return (Class)Hashtable.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerHashtable New()
	{
		return new BinaryHandlerHashtable();
	}
	


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerHashtable()
	{
		super(
			handledType()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Hashtable<?, ?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new Hashtable<>(
			X.checkArrayRange(getElementCount(bytes))
		);
	}
	
}
