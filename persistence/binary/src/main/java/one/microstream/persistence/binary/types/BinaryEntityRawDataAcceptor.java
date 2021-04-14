package one.microstream.persistence.binary.types;

@FunctionalInterface
public interface BinaryEntityRawDataAcceptor
{
	public boolean acceptEntityData(long entityStartAddress, long dataBoundAddress);
}