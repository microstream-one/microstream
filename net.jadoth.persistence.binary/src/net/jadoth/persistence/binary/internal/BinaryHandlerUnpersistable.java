package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceTypeDescription;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleStoreLinker;

public class BinaryHandlerUnpersistable<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerUnpersistable(final Class<T> type, final long typeId)
	{
		super(type, typeId);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final SwizzleStoreLinker linker)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary bytes)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void update(final Binary bytes, final T instance, final SwizzleBuildLinker builder)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
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
	public PersistenceTypeDescription<T> typeDescription()
	{
		throw new UnsupportedOperationException();
	}

}
