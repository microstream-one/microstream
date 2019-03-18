package one.microstream.java.lang;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerStateless;
import one.microstream.persistence.binary.types.Binary;

public final class BinaryHandlerNativeObject extends AbstractBinaryHandlerStateless<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeObject()
	{
		super(Object.class);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Object create(final Binary bytes)
	{
		return new Object(); // funny
	}

}
