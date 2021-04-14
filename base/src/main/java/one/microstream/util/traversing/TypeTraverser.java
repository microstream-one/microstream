package one.microstream.util.traversing;

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
	
	public void traverseReferences(
		T                 instance,
		TraversalAcceptor acceptor
	);
	
	public void traverseReferences(
		T                instance        ,
		TraversalMutator mutator         ,
		MutationListener mutationListener
	);

	public void traverseReferences(
		T                 instance        ,
		TraversalAcceptor acceptor        ,
		TraversalMutator  mutator         ,
		MutationListener  mutationListener
	);
	

	
	public interface Creator
	{
		public <T> TypeTraverser<T> createTraverser(Class<T> type);
						
	}
	
}
