package one.microstream.java.util;

import java.util.Vector;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;


public final class BinaryHandlerVector extends AbstractBinaryHandlerList<Vector<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Vector<?>> handledType()
	{
		return (Class)Vector.class; // no idea how to get ".class" to work otherwise
	}
	
	public static BinaryHandlerVector New()
	{
		return new BinaryHandlerVector();
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerVector()
	{
		super(handledType());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Vector<?> create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new Vector<>();
	}

}
