package one.microstream.storage.io;

public interface ProtageFile extends ProtageIoElement
{
	public ProtageDirectory directory();
	
	@Override
	public String name();
	
	public long length();
	
	public boolean exists();
}
