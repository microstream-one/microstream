package one.microstream.afs;

public interface AFile extends AItem
{
	public ADirectory directory();
	
	@Override
	public String name();
	
	public long length();
	
	public boolean exists();
}
