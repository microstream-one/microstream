package one.microstream.persistence.binary.types;


@FunctionalInterface
public interface BinaryElementReader
{
	public void readElement(Binary binary, long offset);
}
