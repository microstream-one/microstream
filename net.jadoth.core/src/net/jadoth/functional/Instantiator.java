package net.jadoth.functional;

import net.jadoth.exceptions.InstantiationRuntimeException;


public interface Instantiator<T>
{
	public T newInstance() throws InstantiationRuntimeException;
}
