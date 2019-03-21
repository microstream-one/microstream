package one.microstream.persistence.binary.internal;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

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
	public final void store(
		final Binary                  bytes   ,
		final T                       instance,
		final long                    objectId,
		final PersistenceStoreHandler handler
	)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary bytes, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void update(final Binary bytes, final T instance, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

}
