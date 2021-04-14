package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.exceptions.PersistenceExceptionTypeNotPersistable;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceStoreHandler;

public final class BinaryHandlerUnpersistable<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerUnpersistable<T> New(final Class<T> type)
	{
		return new BinaryHandlerUnpersistable<>(
			notNull(type)
		);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerUnpersistable(final Class<T> type)
	{
		super(type);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}

	@Override
	public final void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final void guaranteeSpecificInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final boolean isSpecificInstanceViable()
	{
		return false;
	}
	
	@Override
	public final void guaranteeSubTypeInstanceViablity() throws PersistenceExceptionTypeNotPersistable
	{
		throw new PersistenceExceptionTypeNotPersistable(this.type());
	}
	
	@Override
	public final boolean isSubTypeInstanceViable()
	{
		return false;
	}

}
