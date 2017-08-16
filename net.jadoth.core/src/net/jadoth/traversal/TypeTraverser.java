package net.jadoth.traversal;

public interface TypeTraverser<T>
{
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer
	);
	
	public void traverseReferences(
		T                 instance,
		TraversalEnqueuer enqueuer,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalEnqueuer enqueuer        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	
	
	
	public final class Dummy<T> implements TypeTraverser<T>
	{

		@Override
		public void traverseReferences(final T instance, final TraversalEnqueuer enqueuer)
		{
			// no-op in Dummy implementation
		}

		@Override
		public void traverseReferences(final T instance, final TraversalEnqueuer enqueuer, final TraversalAcceptor acceptor)
		{
			// no-op in Dummy implementation
		}

		@Override
		public void traverseReferences(
			final T                 instance        ,
			final TraversalEnqueuer enqueuer        ,
			final TraversalMutator  mutator         ,
			final MutationListener  mutationListener
		)
		{
			// no-op in Dummy implementation
		}

		@Override
		public void traverseReferences(
			final T                 instance        ,
			final TraversalEnqueuer enqueuer        ,
			final TraversalAcceptor acceptor        ,
			final TraversalMutator  mutator         ,
			final MutationListener  mutationListener)
		{
			// no-op in Dummy implementation
		}
		
	}
	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
