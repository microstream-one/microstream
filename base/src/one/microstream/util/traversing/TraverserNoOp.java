package one.microstream.util.traversing;

public final class TraverserNoOp<T> implements TypeTraverser<T>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static <T> TraverserNoOp<T> New(final Class<T> type)
	{
//		XDebug.debugln("NoOp traverser for " + type);
		
		return new TraverserNoOp<>(type);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final Class<T> type;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	TraverserNoOp(final Class<T> type)
	{
		super();
		this.type = type;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	public final Class<T> type()
	{
		return this.type;
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalEnqueuer enqueuer,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalEnqueuer enqueuer        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	

	@Override
	public final void traverseReferences(
		final T                 instance,
		final TraversalAcceptor acceptor
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
	@Override
	public final void traverseReferences(
		final T                 instance        ,
		final TraversalAcceptor acceptor        ,
		final TraversalMutator  mutator         ,
		final MutationListener  mutationListener
	)
	{
		// no-op
	}
	
}
