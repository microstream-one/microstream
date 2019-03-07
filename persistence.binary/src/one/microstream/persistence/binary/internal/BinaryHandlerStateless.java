package one.microstream.persistence.binary.internal;

import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerStateless<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerStateless(final Class<T> type)
	{
		super(type);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                  medium  ,
		final T                       instance,
		final long                    oid     ,
		final PersistenceStoreHandler handler
	)
	{
		medium.storeStateless(this.typeId(), oid);
	}

	@Override
	public final T create(final Binary medium)
	{
		return XMemory.instantiate(this.type());
	}

}
