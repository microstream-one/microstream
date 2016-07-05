package net.jadoth.memory.objectstate;


public interface ObjectValueCopier
{
	public void copy(long memoryOffset, Object instance1, Object instance2);
}
