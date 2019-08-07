package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerStateless;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;

public final class BinaryHandlerObject extends AbstractBinaryHandlerStateless<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerObject New()
	{
		return new BinaryHandlerObject();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerObject()
	{
		super(Object.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Object create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return new Object(); // funny
	}

}
