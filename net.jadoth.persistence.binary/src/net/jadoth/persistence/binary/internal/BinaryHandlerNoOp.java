package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNoOp<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNoOp(final Class<T> type)
	{
		super(type);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final SwizzleStoreLinker linker)
	{
		// no-op, abort recursive storing here
	}

	@Override
	public final T create(final Binary bytes) throws UnsupportedOperationException
	{
		// no-op is only applicable to storing/updating. Creation must fail.
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean isEqual(
		final T                        source                    ,
		final T                        target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return source == target;
	}

}
