package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;

public class BinaryHandlerUnpersistable<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerUnpersistable(final Class<T> type)
	{
		super(type);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(final Binary bytes, final T instance, final long oid, final PersistenceHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary bytes)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void update(final Binary bytes, final T instance, final PersistenceBuildLinker builder)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

}
