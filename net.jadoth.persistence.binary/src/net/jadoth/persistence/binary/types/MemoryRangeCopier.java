package net.jadoth.persistence.binary.types;

@FunctionalInterface
public interface MemoryRangeCopier
{
	public void copyMemory(long address, long length);
}
