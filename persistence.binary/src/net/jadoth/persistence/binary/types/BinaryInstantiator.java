package net.jadoth.persistence.binary.types;

import net.jadoth.exceptions.InstantiationRuntimeException;

public interface BinaryInstantiator<T>
{
	public T newInstance(long buildItemAddress) throws InstantiationRuntimeException;
	
}
