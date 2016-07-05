package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public final class BinaryHandlerNoOp<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerNoOp(final Class<T> type, final long typeId)
	{
		super(type, typeId);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final SwizzleStoreLinker linker)
	{
		// no-op, abort recursive storing here
	}

	@Override
	public final T create(final Binary bytes)
	{
		// no-op is only applicable to storing/updating. Creation must fail.
		throw new UnsupportedOperationException();
	}

	@Override
	public final void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		// no-op, don't modify anything
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

	@Override
	public final boolean hasInstanceReferences()
	{
		return false;
	}

	@Override
	public PersistenceTypeDescription typeDescription()
	{
		throw new UnsupportedOperationException();
	}

}
