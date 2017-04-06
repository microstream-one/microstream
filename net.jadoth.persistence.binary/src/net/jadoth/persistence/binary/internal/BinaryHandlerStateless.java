package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.Memory;
import net.jadoth.memory.objectstate.ObjectStateHandlerLookup;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.SwizzleStoreLinker;


public class BinaryHandlerStateless<T> extends AbstractBinaryHandlerTrivial<T>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public BinaryHandlerStateless(final Class<T> type, final long typeId)
	{
		super(type, typeId);
	}

	@Override
	public final void store(final Binary medium, final T instance, final long oid, final SwizzleStoreLinker linker)
	{
		BinaryPersistence.storeStateless(medium, this.typeId(), oid);
	}

	@Override
	public final T create(final Binary medium)
	{
		try
		{
			return Memory.instantiate(this.type());
		}
		catch(final InstantiationException e)
		{
			throw new RuntimeException(e); // (10.04.2013)EXCP: proper exception
		}
	}

	@Override
	public final boolean isEqual(
		final T                        source                    ,
		final T                        target                    ,
		final ObjectStateHandlerLookup instanceStateHandlerLookup
	)
	{
		return source == target; // no other meaningful way to test equality of stateless instances
	}

}
