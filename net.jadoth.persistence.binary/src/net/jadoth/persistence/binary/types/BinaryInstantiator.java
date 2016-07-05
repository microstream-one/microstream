package net.jadoth.persistence.binary.types;

import net.jadoth.exceptions.InstantiationRuntimeException;

public interface BinaryInstantiator<T> // extends Instantiator<T>
{
	public T newInstance(final long buildItemAddress) throws InstantiationRuntimeException;

}
