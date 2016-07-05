package net.jadoth.memory.objectstate;

public interface ObjectStateCopier<T>
{
	public void copy(T source, T target);
}
