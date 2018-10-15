package net.jadoth.storage.io;

public interface ProtageFile
{
	public ProtageDirectory directory();
	
	public String name();
	
	public long length();
	
	public boolean exists();
}
