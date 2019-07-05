package one.microstream.java.util;

import java.util.Vector;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;


public final class BinaryHandlerVector extends AbstractBinaryHandlerList<Vector<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<Vector<?>> typeWorkaround()
	{
		return (Class)Vector.class; // no idea how to get ".class" to work otherwise
	}
	
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerVector()
	{
		super(typeWorkaround());
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final Vector<?> create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		return new Vector<>();
	}

}
