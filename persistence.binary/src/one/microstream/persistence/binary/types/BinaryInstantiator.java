package one.microstream.persistence.binary.types;

import one.microstream.exceptions.InstantiationRuntimeException;

public interface BinaryInstantiator<T>
{
	public T newInstance(long buildItemAddress) throws InstantiationRuntimeException;
	
}
