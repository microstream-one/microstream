package net.jadoth.persistence.binary.types;

@FunctionalInterface
public interface MemoryRangeReader
{
	public void readMemory(long address, long length);
}
