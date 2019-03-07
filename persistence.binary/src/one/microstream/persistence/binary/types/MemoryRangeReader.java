package one.microstream.persistence.binary.types;

@FunctionalInterface
public interface MemoryRangeReader
{
	public void readMemory(long address, long length);
}
