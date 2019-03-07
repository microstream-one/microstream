package one.microstream.typing;

public interface Caching extends Clearable
{
	public boolean hasFilledCache();
	
	public boolean ensureFilledCache();
	
	@Override
	public void clear();
}
