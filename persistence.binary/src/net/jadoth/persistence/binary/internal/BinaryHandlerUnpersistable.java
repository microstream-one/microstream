package net.jadoth.persistence.binary.internal;

import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceStoreHandler;

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
	public final void store(final Binary bytes, final T instance, final long oid, final PersistenceStoreHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary bytes)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void update(final Binary bytes, final T instance, final PersistenceLoadHandler builder)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

}
