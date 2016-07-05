package net.jadoth.memory.objectstate;


public interface ObjectStateHandlerLookup
{
	public <T> ObjectStateHandler<T> lookupTypeHandler(T instance);

	public <T> ObjectStateHandler<T> lookupTypeHandler(Class<T> type);
}
