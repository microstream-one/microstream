package net.jadoth.traversal;

import java.util.function.Predicate;

import net.jadoth.collections.types.XSet;

public abstract class AbstractReferenceHandler implements TraversalReferenceHandler
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final TypeTraverserProvider      traverserProvider;
	final XSet<Object>               alreadyHandled   ;
	final Predicate<Object>          isHandleable     ;
	final Predicate<Object>          isNode           ;
	final Predicate<Object>          isFull           ;

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
		final XSet<Object>          alreadyHandled   ,
		final Predicate<Object>     isHandleable     ,
		final Predicate<Object>     isNode           ,
		final Predicate<Object>     isFull
	)
	{
		super();
		this.traverserProvider = traverserProvider;
		this.alreadyHandled    = alreadyHandled   ;
		this.isHandleable      = isHandleable     ;
		this.isNode            = isNode           ;
		this.isFull            = isFull           ;
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
		// must check for null as there is no control over what custom handler implementations might pass.
		if(instance == null)
		{
			return;
		}
		if(!this.alreadyHandled.add(instance))
		{
			return;
		}
		if(this.isHandleable != null && !this.isHandleable.test(instance))
		{
			return;
		}
						
		if(this.iterationHeadIndex >= ObjectGraphTraverser.Implementation.SEGMENT_SIZE)
		{
			this.increaseIterationQueue();
		}
		this.iterationHead[this.iterationHeadIndex++] = instance;
	}
				
	private Object dequeue()
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
		
		return this.iterationTail[this.iterationTailIndex++];
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

	
	final void handleAll(final Object[] instances, final boolean eager)
	{
		for(final Object instance : instances)
		{
			this.enqueue(instance);
		}
		
		if(eager)
		{
			if(this.isNode == null)
			{
				this.handleAllEager(instances);
			}
			else
			{
				this.handleAllEagerTesting(instances);
			}
		}
		else
		{
			if(this.isFull == null)
			{
				this.handleAllLazy(instances);
			}
			else
			{
				this.handleAllLazyTesting(instances);
			}
		}
	}
	
	private void handleAllEager(final Object[] instances)
	{
		try
		{
			while(true)
			{
				final Object instance = this.dequeue();
				this.handle(instance, this.traverserProvider.provide(instance));
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return;
			return;
		}
	}
	
	private void handleAllEagerTesting(final Object[] instances)
	{
		try
		{
			final Predicate<Object> isNode = this.isNode;
			
			while(true)
			{
				final Object instance;
				if(isNode.test(instance = this.dequeue()))
				{
					this.handleNode(instance);
				}
				else
				{
					this.handle(instance, this.traverserProvider.provide(instance));
				}
				
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return;
			return;
		}
	}
	
	private void handleAllLazy(final Object[] instances)
	{
		try
		{
			while(true)
			{
				this.handleNode(this.dequeue());
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return;
			return;
		}
	}
	
	private void handleAllLazyTesting(final Object[] instances)
	{
		try
		{
			final Predicate<Object> isFull = this.isFull;
			
			while(true)
			{
				final Object instance;
				if(isFull.test(instance = this.dequeue()))
				{
					this.handle(instance, this.traverserProvider.provide(instance));
				}
				else
				{
					this.handleNode(instance);
				}
				
			}
		}
		catch(final TraversalSignalAbort s)
		{
			// some logic signaled to abort the traversal. So abort and return;
			return;
		}
	}

	abstract <T> void handle(T instance, final TypeTraverser<T> traverser);
	
	final <T> void handleNode(final T instance)
	{
		final TypeTraverser<T> traverser = this.traverserProvider.provide(instance);
		
//		JadothConsole.debugln("Traversing NODE " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(traverser));
		traverser.traverseReferences(instance, this);
	}
	
			
	@Override
	public final void handleAsFull(final Object[] instances)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME AbstractReferenceHandler#handleAsFull()
	}
	
	@Override
	public final void handleAsNode(final Object[] instances)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME AbstractReferenceHandler#handleAsNode()
	}
	
	@Override
	public final void handleAsLeaf(final Object[] instances)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME AbstractReferenceHandler#handleAsLeaf()
	}
	
}
