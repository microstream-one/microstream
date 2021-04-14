package one.microstream.persistence.binary.types;

public interface MessageWaiter
{
	public void waitForBytes(int readCount);
}
