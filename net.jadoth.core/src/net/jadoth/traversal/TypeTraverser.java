package net.jadoth.traversal;

public interface TypeTraverser<T>
{
	public void traverseReferences(T instance, TraversalAcceptor acceptor, TraversalEnqueuer enqueuer);
	
	public void traverseReferences(T instance, TraversalMutator mutator, TraversalEnqueuer enqueuer, MutationListener mutationListener);
	
	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
