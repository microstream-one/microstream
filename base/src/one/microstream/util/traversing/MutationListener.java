package one.microstream.util.traversing;

public interface MutationListener
{
	public boolean registerChange(Object parent, Object oldReference, Object newReference);
		
}
