package net.jadoth.traversal;

import net.jadoth.collections.types.XSet;

public abstract class AbstractReferenceHandler implements TraversalEnqueuer
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final TypeTraverserProvider traverserProvider;
	final XSet<Object>          alreadyHandled   ;

	Object[] iterationTail      = ObjectGraphTraverser.Implementation.createIterationSegment();
	Object[] iterationHead      = this.iterationTail;
	boolean  tailIsHead         = true;
	int      iterationTailIndex;
	int      iterationHeadIndex;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AbstractReferenceHandler(
		final TypeTraverserProvider traverserProvider,
		final XSet<Object>          alreadyHandled
	)
	{
		super();
		this.traverserProvider = traverserProvider;
		this.alreadyHandled    = alreadyHandled   ;

	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public final boolean skip(final Object instance)
	{
		return this.alreadyHandled.add(instance);
	}
	
	final void increaseIterationQueue()
	{
		final Object[] nextIterationSegment = ObjectGraphTraverser.Implementation.createIterationSegment();
		this.iterationHead[ObjectGraphTraverser.Implementation.SEGMENT_SIZE]    = nextIterationSegment    ;
		this.iterationHead                  = nextIterationSegment    ;
		this.iterationHeadIndex             = 0                       ;
		this.tailIsHead                     = false                   ;
	}
	
	@Override
	public final void enqueue(final Object instance)
	{
		// must check for null as there is no control over what custom handler implementations might pass
		if(instance == null)
		{
			return;
		}
		
		if(!this.alreadyHandled.add(instance))
		{
			return;
		}
		
		/* this check causes a redundant lookup in the handler registry: one here, one later to
		 * actually get the handler.
		 * Nevertheless, this is considered the favorable strategy, as the alternatives would be:
		 * - no check at all, meaning to potentially enqueuing millions of leaf type instances,
		 *   bloating the queue, maybe even causing out of memory problems
		 * - co-enqueuing the handler, but at the price of doubled queue size, complicated dequeueing logic and
		 *   compromised type safety (every second item is actually a handler instance hacked into the queue)
		 * So in the end, it seems best to accept a slight performance overhead but keep the queue as
		 * small as possible.
		 * Other implementations can take different approaches to optimize runtime behavior to suit their needs.
		 */
		if(this.traverserProvider.isUnhandled(instance))
		{
			return;
		}
						
		if(this.iterationHeadIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.increaseIterationQueue();
		}
		this.iterationHead[this.iterationHeadIndex++] = instance;
	}
				
	@SuppressWarnings("unchecked")
	final <T> T dequeue()
	{
		// (25.06.2017 TM)TODO: test performance of outsourced private methods
		if(this.tailIsHead)
		{
			this.checkForCompletion();
		}
		if(this.iterationTailIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.advanceSegment();
		}
		
		return (T)this.iterationTail[this.iterationTailIndex++];
	}
		
	final void checkForCompletion()
	{
		if(this.iterationTailIndex >= this.iterationHeadIndex)
		{
			ObjectGraphTraverser.signalAbortTraversal();
		}
	}
	
	final void advanceSegment()
	{
		this.iterationTail      = (Object[])this.iterationTail[ObjectGraphTraverser.Implementation.SEGMENT_SIZE];
		this.iterationTailIndex = 0;
		this.tailIsHead         = this.iterationTail == this.iterationHead;
	}

	final void handleAll(final Object[] instances)
	{
		for(final Object instance : instances)
		{
			this.enqueue(instance);
		}
		
		try
		{
			while(true)
			{
				this.handle(this.dequeue());
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return;
			return;
		}
	}

	abstract <T> void handle(T instance);
	
	final <T> void handle(final T instance, final TraversalAcceptor traversalAcceptor)
	{
		final TypeTraverser<T> traverser = this.provideTraverser(instance);
		
//		JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this, traversalAcceptor);
	}
	
	final <T> void handle(final T instance, final TraversalMutator traversalMutator, final MutationListener mutationListener)
	{
		final TypeTraverser<T> traverser = this.provideTraverser(instance);
		
//		JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this, traversalMutator, mutationListener);
	}
	
	final <T> void handle(
		final T                 instance         ,
		final TraversalAcceptor traversalAcceptor,
		final TraversalMutator  traversalMutator,
		final MutationListener  mutationListener
	)
	{
		final TypeTraverser<T> traverser = this.provideTraverser(instance);
		
//		JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this, traversalAcceptor, traversalMutator, mutationListener);
	}
	
	final <T> TypeTraverser<T> provideTraverser(final T instance)
	{
		return this.traverserProvider.provide(instance);
	}
	
}
