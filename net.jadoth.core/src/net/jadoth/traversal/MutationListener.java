package net.jadoth.traversal;

public interface MutationListener
{
	public boolean registerChange(Object parent, Object oldReference, Object newReference);
		
}
