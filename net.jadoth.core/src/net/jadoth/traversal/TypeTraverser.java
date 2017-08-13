package net.jadoth.traversal;

import net.jadoth.meta.NotImplementedYetError;

public interface TypeTraverser<T>
{
	public default void traverseReferences(final T instance, final TraversalEnqueuer enqueuer)
	{
		this.traverseReferences(instance, enqueuer, null, null, null);
	}
	
	public default void traverseReferences(final T instance, final TraversalEnqueuer enqueuer, final TraversalAcceptor acceptor)
	{
		this.traverseReferences(instance, enqueuer, acceptor, null, null);
	}
	
	public default void traverseReferences(final T instance, final TraversalEnqueuer enqueuer, final TraversalMutator mutator, final MutationListener mutationListener)
	{
		this.traverseReferences(instance, enqueuer, null, mutator, mutationListener);
	}
	
	public default void traverseReferences(final T instance, final TraversalAcceptor acceptor)
	{
		this.traverseReferences(instance, null, acceptor, null, null);
	}
	
	public default void traverseReferences(final T instance, final TraversalMutator mutator, final MutationListener mutationListener)
	{
		this.traverseReferences(instance, null, null, mutator, mutationListener);
	}
	
	public default void traverseReferences(final T instance, final TraversalAcceptor acceptor, final TraversalMutator mutator, final MutationListener mutationListener)
	{
		this.traverseReferences(instance, null, acceptor, mutator, mutationListener);
	}
	
	public default void traverseReferences(final T instance, final TraversalEnqueuer enqueuer, final TraversalAcceptor acceptor, final TraversalMutator mutator, final MutationListener mutationListener)
	{
		throw new NotImplementedYetError();
	}
	
	
	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
