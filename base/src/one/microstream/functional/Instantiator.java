package one.microstream.functional;

import one.microstream.exceptions.InstantiationRuntimeException;


public interface Instantiator<T>
{
	public T newInstance() throws InstantiationRuntimeException;
}
