package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;

public final class BinaryHandlerNativeObject extends AbstractBinaryHandlerStateless<Object>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNativeObject(final long tid)
	{
		super(tid, Object.class);
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
