package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public abstract class AbstractReferenceHandler2 implements TraversalReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final TypeTraverserProvider  traverserProvider;
	final XSet<Object>           alreadyHandled   ;
	final TraversalPredicateSkip predicateSkip    ;
	final TraversalPredicateNode predicateNode    ;
	final TraversalPredicateLeaf predicateLeaf    ;
	final TraversalPredicateFull predicateFull    ;
	final Predicate<Object>      predicateHandle  ; // more used for logging stuff than for filtering, see skipping.

	Object[] head   ;
	Object[] enqueue = this.head = ObjectGraphTraverser.Implementation.createIterationSegment();
	Object[] dequeue = null;
	int      enqueueIndex;
	int      dequeueIndex;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	AbstractReferenceHandler2(
		final TypeTraverserProvider  traverserProvider,
		final XSet<Object>           alreadyHandled   ,
		final TraversalPredicateSkip predicateSkip    ,
		final TraversalPredicateNode predicateNode    ,
		final TraversalPredicateLeaf predicateLeaf    ,
		final TraversalPredicateFull predicateFull    ,
		final Predicate<Object>      predicateHandle
	)
	{
		super();
		this.traverserProvider = traverserProvider;
		this.alreadyHandled    = alreadyHandled   ;
		this.predicateSkip     = predicateSkip    ;
		this.predicateNode     = predicateNode    ;
		this.predicateLeaf     = predicateLeaf    ;
		this.predicateFull     = predicateFull    ;
		this.predicateHandle   = predicateHandle  ;
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
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME AbstractReferenceHandler2#increaseIterationQueue()
	}
	
	@Override
	public final void enqueue(final Object instance)
	{
		// must check for null as there is no control over what custom handler implementations might pass.
		if(instance == null)
		{
			return;
		}
		if(!this.alreadyHandled.add(instance))
		{
			return;
		}
		if(this.predicateSkip != null && this.predicateSkip.skip(instance))
		{
			return;
		}
		
		if(this.enqueueIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.increaseIterationQueue();
		}
		this.enqueue[this.enqueueIndex++] = instance;
	}
					
	private Object dequeue()
	{
		// this indicates either a completely processed segment or a segment change via enqueue
		if(this.dequeueIndex >= ObjectGraphTraverser.Implementation.SEGMENT_LAST_INDEX)
		{
			this.advanceSegment((Object[])this.dequeue[ObjectGraphTraverser.Implementation.SEGMENT_SIZE]);
		}
		return this.dequeue[++this.dequeueIndex];
	}
	
	final void updateDequeueSegment()
	{
		if(this.dequeue != this.head)
		{
			this.advanceSegment(this.head);
		}
		else
		{
			this.advanceSegment((Object[])this.dequeue[ObjectGraphTraverser.Implementation.SEGMENT_SIZE]);
		}
	}
	
	final void advanceSegment(final Object[] passed)
	{
		for(Object[] seg = passed; seg != null; seg = (Object[])seg[ObjectGraphTraverser.Implementation.SEGMENT_SIZE])
		{
			// quick-check for the common case
			if(seg[0] != null)
			{
				this.dequeue = this.head = seg;
				this.dequeueIndex = -1;
				return;
			}
			
			// scan current segument for next item
			int i = 0;
			while(++i < ObjectGraphTraverser.Implementation.SEGMENT_LAST_INDEX)
			{
				if(seg[i] != null)
				{
					this.dequeue = this.head = seg;
					this.dequeueIndex = i - 1;
					return;
				}
			}
		}
		
		// if there is no more segment, the traversal is complete
		ObjectGraphTraverser.signalAbortTraversal();
	}

	private void enqueueAll(final Object[] instances)
	{
		for(final Object instance : instances)
		{
			this.enqueue(instance);
		}
	}
	
	@Override
	public final void handleAsFull(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleFull(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsNode(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateLeaf != null && this.predicateLeaf.isLeaf(instance))
				{
					this.handleLeaf(instance, traverser);
				}
				else
				{
					this.handleNode(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	@Override
	public final void handleAsLeaf(final Object[] instances)
	{
		this.enqueueAll(instances);

		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				if(this.predicateHandle != null && !this.predicateHandle.test(instance))
				{
					continue;
				}
				
				final TypeTraverser<Object> traverser = this.traverserProvider.provide(instance);
				
				if(this.predicateFull != null && this.predicateFull.isFull(instance))
				{
					this.handleFull(instance, traverser);
				}
				else if(this.predicateNode != null && this.predicateNode.isNode(instance))
				{
					this.handleNode(instance, traverser);
				}
				else
				{
					this.handleLeaf(instance, traverser);
				}
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return. (This is a signal, NOT a problem!)
			return;
		}
	}
	
	

	abstract <T> void handleFull(T instance, final TypeTraverser<T> traverser);
	
	abstract <T> void handleLeaf(T instance, final TypeTraverser<T> traverser);
	
	final <T> void handleNode(final T instance, final TypeTraverser<T> traverser)
	{
//		JadothConsole.debugln("Traversing NODE " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this);
	}
	
}
