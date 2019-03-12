package one.microstream.persistence.binary.types;

@FunctionalInterface
public interface BinaryEntityDataAcceptor
{
	public boolean acceptEntityData(long entityStartAddress, long dataBoundAddress);
}