package net.jadoth.traversal2;

public interface TraverserMutatingCreator
{
	public <T> TraverserMutating<T> createTraverserMutating(Class<T> type);
	
}
