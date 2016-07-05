package net.jadoth.memory.objectstate;


public interface ObjectStateComparer<T>
{
	public boolean isEqual(T source, T target, ObjectStateHandlerLookup instanceStateHandlerLookup);
}
