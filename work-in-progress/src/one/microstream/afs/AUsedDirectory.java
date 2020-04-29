package one.microstream.afs;

public interface AUsedDirectory extends ADirectory, ADirectory.Wrapper
{
	public boolean release();
}
