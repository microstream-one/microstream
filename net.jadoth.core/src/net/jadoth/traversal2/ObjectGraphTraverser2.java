package net.jadoth.traversal2;

import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;


public interface ObjectGraphTraverser2
{
	public default TraversalAcceptor traverse(final Object instance)
	{
		return this.traverseAll(Jadoth.array(instance));
	}

	public default <A extends TraversalAcceptor> A traverse(final Object instance, final A acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}

	public TraversalAcceptor traverseAll(final Object[] instances);

	public <A extends TraversalAcceptor> A traverseAll(Object[] instances, A acceptor);
	
	
	public static void signalAbortTraversal() throws TraversalSignalAbort
	{
		TraversalSignalAbort.fire();
	}

	
	public static ObjectGraphTraverser2Factory Factory()
	{
		return new ObjectGraphTraverser2Factory.Implementation();
	}
	
	public static ObjectGraphTraverser2 New(
		final TraverserAcceptingProvider                           handlerProvider       ,
		final XGettingCollection<Object>                         skipped               ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
		final TraversalAcceptor                                  acceptor,
		final TraversalMutator                                   mutator
	)
	{
		return new ObjectGraphTraverser2.Implementation(
			handlerProvider,
			coalesce(skipped, X.empty()).immure(),
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			acceptor
			
		);
	}
	
	public final class Implementation implements ObjectGraphTraverser2
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final TraverserAcceptingProvider                           handlerProvider       ;
		private final XGettingCollection<Object>                         skipped               ;
		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider;
		private final TraversalAcceptor                                  acceptor              ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final TraverserAcceptingProvider                           handlerProvider       ,
			final XGettingCollection<Object>                         skipped               ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
			final TraversalAcceptor                                  acceptor
		)
		{
			super();
			this.handlerProvider        = handlerProvider       ;
			this.skipped                = skipped               ;
			this.alreadyHandledProvider = alreadyHandledProvider;
			this.acceptor               = acceptor              ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		private synchronized void internalTraverseAll(
			final Object[]          instances,
			final TraversalAcceptor acceptor
		)
		{
			notNull(acceptor);
			
			final ReferenceHandler referenceHandler = new ReferenceHandler(
				this.handlerProvider,
				this.alreadyHandledProvider.apply(this.skipped),
				instances
			);

			try
			{
				while(true)
				{
					referenceHandler.handleNext(acceptor);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return;
			}
		}
				
		@Override
		public final TraversalAcceptor traverseAll(final Object[] instances)
		{
			this.internalTraverseAll(instances, this.acceptor);
			return this.acceptor;
		}

		@Override
		public final <A extends TraversalAcceptor> A traverseAll(final Object[] instances, final A acceptor)
		{
			this.internalTraverseAll(instances, acceptor);
			return acceptor;
		}
		
		/**
		 * Must be a multiple of 2.
		 *
		 * Surprisingly, the exact value here doesn't matter much.
		 * The initial idea was to replace hundreds of Entry instances with one array of about one cache page size
		 * (~500 references assuming 4096 page size and no coops minus object header etc.).
		 * However, tests with graphs from 1000 to ~30 million handled instances showed:
		 * - the segment structure is only measurably faster for really big graphs (8 digit instance count)
		 * - a segment count of 100 is equally fast as 10000. Only unreasonably tiny sizes like <= 8 are slower.
		 *
		 * However, a slight performance gain is still better than none. Plus there is much less memory used
		 * for object header and chain-reference overhead.
		 */
		private static final int SEGMENT_SIZE = 500;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final Object[] createIterationSegment()
		{
			// one trailing slot as a pointer to the next segment array. Both hacky and elegant.
			return new Object[SEGMENT_SIZE + 1];
		}
		
		final class ReferenceHandler implements TraversalEnqueuer
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			private final TraverserAcceptingProvider handlerProvider;
			private final XSet<Object>             alreadyHandled ;

			Object[] iterationTail      = createIterationSegment();
			Object[] iterationHead      = this.iterationTail;
			boolean  tailIsHead         = true;
			int      iterationTailIndex;
			int      iterationHeadIndex;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandler(
				final TraverserAcceptingProvider handlerProvider,
				final XSet<Object>             alreadyHandled ,
				final Object[]                 instances
			)
			{
				super();
				this.handlerProvider = handlerProvider;
				this.alreadyHandled  = alreadyHandled ;

				for(final Object instance : instances)
				{
					this.enqueue(instance);
				}
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////

			@Override
			public final boolean skip(final Object instance)
			{
				return this.alreadyHandled.add(instance);
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
				if(this.handlerProvider.isUnhandled(instance))
				{
					return;
				}
								
				if(this.iterationHeadIndex >= SEGMENT_SIZE)
				{
					this.increaseIterationQueue();
				}
				this.iterationHead[this.iterationHeadIndex++] = instance;
			}
			
			private void increaseIterationQueue()
			{
				final Object[] nextIterationSegment = createIterationSegment();
				this.iterationHead[SEGMENT_SIZE]    = nextIterationSegment    ;
				this.iterationHead                  = nextIterationSegment    ;
				this.iterationHeadIndex             = 0                       ;
				this.tailIsHead                     = false                   ;
			}
						
			@SuppressWarnings("unchecked")
			private <T> T dequeue()
			{
				// (25.06.2017 TM)TODO: test performance of outsourced private methods
				if(this.tailIsHead)
				{
					this.checkForCompletion();
				}
				if(this.iterationTailIndex >= SEGMENT_SIZE)
				{
					this.advanceSegment();
				}
				
				return (T)this.iterationTail[this.iterationTailIndex++];
			}
			
			private void checkForCompletion()
			{
				if(this.iterationTailIndex >= this.iterationHeadIndex)
				{
					ObjectGraphTraverser2.signalAbortTraversal();
				}
			}
			
			private void advanceSegment()
			{
				this.iterationTail      = (Object[])this.iterationTail[SEGMENT_SIZE];
				this.iterationTailIndex = 0;
				this.tailIsHead         = this.iterationTail == this.iterationHead;
			}
						
			final <T> void handleNext(final TraversalAcceptor acceptor) throws TraversalSignalAbort
			{
				final T                   instance = this.dequeue();
				final TraverserAccepting<T> handler  = this.handlerProvider.provideTraversalHandler(instance);
				
//				JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(handler));
				handler.traverseReferences(instance, acceptor, this);
			}
			
		}
				
	}
	
}
