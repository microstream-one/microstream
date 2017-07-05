package net.jadoth.traversal;

public interface TraverserMutating<T>
{
	public void traverseReferences(T instance, TraversalMutator mutator, TraversalEnqueuer enqueuer);
	
	
	public interface Creator
	{
		public <T> TraverserMutating<T> createTraverserMutating(Class<T> type);
		
	}

	
}
