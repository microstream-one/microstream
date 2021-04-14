package one.microstream.persistence.binary.internal;

import static one.microstream.X.notNull;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
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
		final Binary                          data    ,
		final T                               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeStateless(this.typeId(), objectId);
	}

	@Override
	public final T create(final Binary data, final PersistenceLoadHandler handler)
	{
		return XMemory.instantiateBlank(this.type());
	}

}
