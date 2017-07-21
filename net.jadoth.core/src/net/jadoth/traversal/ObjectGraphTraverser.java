package net.jadoth.traversal;

import static net.jadoth.Jadoth.coalesce;
import static net.jadoth.Jadoth.notNull;

import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;


public interface ObjectGraphTraverser
{
	public void traverse();
	
	public default void traverse(final Object instance)
	{
		this.traverseAll(Jadoth.array(instance));
	}
	
	public default void traverseAll(final Object[] instances)
	{
		throw new RuntimeException("No traversal logic specified"); // (17.07.2017 TM)EXCP: proper exception
	}
	
	public <A extends TraversalAcceptor> A traverse(A acceptor);
	
	public <M extends TraversalMutator> M traverse(M acceptor);

	public default <A extends TraversalAcceptor> A traverse(final Object instance, final A acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}
	
	public default <M extends TraversalMutator> M traverse(final Object instance, final M acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}


	public <A extends TraversalAcceptor> A traverseAll(Object[] instances, A acceptor);
	
	public <M extends TraversalMutator> M traverseAll(Object[] instances, M mutator);
	
	
	public static void signalAbortTraversal() throws TraversalSignalAbort
	{
		TraversalSignalAbort.fire();
	}

	
	public static ObjectGraphTraverserBuilder Builder()
	{
		return new ObjectGraphTraverserBuilder.Implementation();
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                 ,
		final XGettingCollection<Object>                         skipped               ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
		final TypeTraverserProvider                              traverserProvider
	)
	{
		return new ObjectGraphTraverser.Implementation(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)
		);
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                 ,
		final XGettingCollection<Object>                         skipped               ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
		final TypeTraverserProvider                              traverserProvider     ,
		final TraversalAcceptor                                  acceptor
	)
	{
		return new ImplementationAccepting(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)                                        ,
			acceptor
		);
	}
	
	public static ObjectGraphTraverser New(
		final Object[]                                           roots                 ,
		final XGettingCollection<Object>                         skipped               ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
		final TypeTraverserProvider                              traverserProvider     ,
		final TraversalMutator                                   mutator
	)
	{
		return new ImplementationMutating(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			notNull(traverserProvider)                                        ,
			mutator
		);
	}
	
	public class Implementation implements ObjectGraphTraverser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Object[]                                           roots                 ;
		private final XGettingCollection<Object>                         skipped               ;
		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider;
		private final TypeTraverserProvider                              traverserProvider     ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final Object[]                                           roots                 ,
			final XGettingCollection<Object>                         skipped               ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
			final TypeTraverserProvider                              traverserProvider
		)
		{
			super();
			this.roots                  = roots                 ;
			this.skipped                = skipped               ;
			this.alreadyHandledProvider = alreadyHandledProvider;
			this.traverserProvider      = traverserProvider     ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final synchronized void internalTraverseAllAccepting(
			final Object[]          instances,
			final TraversalAcceptor acceptor
		)
		{
			notNull(acceptor);
			
			final ReferenceHandlerAccepting referenceHandler = new ReferenceHandlerAccepting(
				this.traverserProvider,
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
		
		protected final synchronized void internalTraverseAllMutating(
			final Object[]         instances,
			final TraversalMutator mutator
		)
		{
			notNull(mutator);
			
			final ReferenceHandlerMutating referenceHandler = new ReferenceHandlerMutating(
				this.traverserProvider,
				this.alreadyHandledProvider.apply(this.skipped),
				instances
			);

			try
			{
				while(true)
				{
					referenceHandler.handleNext(mutator);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return;
			}
		}
		
		@Override
		public void traverse()
		{
			this.traverseAll(this.roots);
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverse(final A acceptor)
		{
			this.internalTraverseAllAccepting(this.roots, acceptor);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverse(final M mutator)
		{
			this.internalTraverseAllMutating(this.roots, mutator);
			return mutator;
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverseAll(final Object[] instances, final A acceptor)
		{
			this.internalTraverseAllAccepting(instances, acceptor);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverseAll(final Object[] instances, final M mutator)
		{
			this.internalTraverseAllMutating(instances, mutator);
			return mutator;
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
		
		
		static abstract class AbstractReferenceHandler implements TraversalEnqueuer
		{
			///////////////////////////////////////////////////////////////////////////
			// instance fields //
			////////////////////
			
			final TypeTraverserProvider traverserProvider;
			final XSet<Object>          alreadyHandled   ;

			Object[] iterationTail      = createIterationSegment();
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
				final Object[]              instances
			)
			{
				super();
				this.traverserProvider = traverserProvider;
				this.alreadyHandled    = alreadyHandled  ;

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
			
			final void increaseIterationQueue()
			{
				final Object[] nextIterationSegment = createIterationSegment();
				this.iterationHead[SEGMENT_SIZE]    = nextIterationSegment    ;
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
								
				if(this.iterationHeadIndex >= SEGMENT_SIZE)
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
				if(this.iterationTailIndex >= SEGMENT_SIZE)
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
				this.iterationTail      = (Object[])this.iterationTail[SEGMENT_SIZE];
				this.iterationTailIndex = 0;
				this.tailIsHead         = this.iterationTail == this.iterationHead;
			}
		}
		
		static final class ReferenceHandlerAccepting extends AbstractReferenceHandler
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandlerAccepting(
				final TypeTraverserProvider traverserProvider,
				final XSet<Object>          alreadyHandled ,
				final Object[]              instances
			)
			{
				super(traverserProvider, alreadyHandled, instances);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
												
			final <T> void handleNext(final TraversalAcceptor acceptor) throws TraversalSignalAbort
			{
				final T                instance  = this.dequeue();
				final TypeTraverser<T> traverser = this.traverserProvider.provide(instance);
				
//				JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(handler));
				traverser.traverseReferences(instance, acceptor, this);
			}
			
		}
		
		static final class ReferenceHandlerMutating extends AbstractReferenceHandler
		{
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandlerMutating(
				final TypeTraverserProvider traverserProvider,
				final XSet<Object>          alreadyHandled ,
				final Object[]              instances
			)
			{
				super(traverserProvider, alreadyHandled, instances);
			}
			
			
			
			///////////////////////////////////////////////////////////////////////////
			// methods //
			////////////
												
			final <T> void handleNext(final TraversalMutator mutator) throws TraversalSignalAbort
			{
				final T                instance  = this.dequeue();
				final TypeTraverser<T> traverser = this.traverserProvider.provide(instance);
				
//				JadothConsole.debugln("Traversing " + Jadoth.systemString(instance) + " via " + Jadoth.systemString(handler));
				traverser.traverseReferences(instance, mutator, this);
			}
			
		}
				
	}
	
	
	public final class ImplementationAccepting extends ObjectGraphTraverser.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalAcceptor acceptor;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImplementationAccepting(
			final Object[]                                           roots                 ,
			final XGettingCollection<Object>                         skipped               ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
			final TypeTraverserProvider                              traverserProvider     ,
			final TraversalAcceptor                                  acceptor
		)
		{
			super(roots, skipped, alreadyHandledProvider, traverserProvider);
			this.acceptor = acceptor;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void traverseAll(final Object[] instances)
		{
			this.internalTraverseAllAccepting(instances, this.acceptor);
		}
		
	}
	
	
	
	public final class ImplementationMutating extends ObjectGraphTraverser.Implementation
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private final TraversalMutator mutator;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ImplementationMutating(
			final Object[]                                           roots                 ,
			final XGettingCollection<Object>                         skipped               ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
			final TypeTraverserProvider                              traverserProvider     ,
			final TraversalMutator                                   mutator
		)
		{
			super(roots, skipped, alreadyHandledProvider, traverserProvider);
			this.mutator = mutator;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public void traverseAll(final Object[] instances)
		{
			this.internalTraverseAllMutating(instances, this.mutator);
		}
		
	}
	
}
