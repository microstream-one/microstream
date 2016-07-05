package net.jadoth.persistence.binary.types;

public interface MessageWaiter
{
	public void waitForBytes(int readCount);
}
