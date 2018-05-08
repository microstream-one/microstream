package net.jadoth.persistence.binary.internal;

import net.jadoth.memory.Memory;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.swizzling.types.PersistenceStoreFunction;


public class BinaryHandlerStateless<T> extends AbstractBinaryHandlerTrivial<T>
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
	public void store(final Binary medium, final T instance, final long oid, final PersistenceStoreFunction linker)
	{
		BinaryPersistence.storeStateless(medium, this.typeId(), oid);
	}

	@Override
	public T create(final Binary medium)
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

}
