package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStateless<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> BinaryHandlerStateless<T> New(final Class<T> type)
	{
		return new BinaryHandlerStateless<>(
			notNull(type)
		);
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerStateless(final Class<T> type)
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
		bytes.storeStateless(this.typeId(), objectId);
	}

	@Override
	public final T create(final Binary bytes, final PersistenceObjectIdResolver idResolver)
	{
		return XMemory.instantiate(this.type());
	}

}
