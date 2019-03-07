package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.types.PersistenceStoreHandler;


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
