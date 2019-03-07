package net.jadoth.util.traversing;

import static net.jadoth.X.coalesce;
import static net.jadoth.X.notNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;


public interface ObjectGraphTraverser
{
	public void traverse();
	
	public default void traverse(final Object instance)
	{
		this.traverseAll(X.array(instance));
	}
	
	public void traverseAll(final Object[] instances);
	
	public <A extends TraversalAcceptor> A traverse(A acceptor);
	
	public <M extends TraversalMutator> M traverse(M acceptor);

	public default <A extends TraversalAcceptor> A traverse(final Object instance, final A acceptor)
	{
		this.traverseAll(X.array(instance), acceptor);
		return acceptor;
	}
	
	public default <C extends Consumer<Object>> C traverse(final Object instance, final C logic)
	{
		this.traverse(instance, TraversalAcceptor.New(logic));
		return logic;
	}
	
	public default <M extends TraversalMutator> M traverse(final Object instance, final M acceptor)
	{
		this.traverseAll(X.array(instance), acceptor);
		return acceptor;
	}
	
	public default <F extends Function<Object, Object>> F traverse(final Object instance, final F logic)
	{
		this.traverse(instance, TraversalMutator.New(logic));
		return logic;
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
		final Object[]                                           roots                   ,
		final XGettingCollection<Object>                         skipped                 ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
		final TraversalReferenceHandlerProvider                  referenceHandlerProvider,
		final TypeTraverserProvider                              traverserProvider       ,
		final TraversalPredicateSkip                             predicateSkip           ,
		final TraversalPredicateNode                             predicateNode           ,
		final TraversalPredicateLeaf                             predicateLeaf           ,
		final TraversalPredicateFull                             predicateFull           ,
		final Predicate<Object>                                  predicateHandle         ,
		final TraversalAcceptor                                  traversalAcceptor       ,
		final TraversalMutator                                   traversalMutator        ,
		final MutationListener                                   mutationListener        ,
		final TraversalMode                                      traversalMode           ,
		final Runnable                                           initializerLogic        ,
		final Runnable                                           finalizerLogic
	)
	{
		return new ObjectGraphTraverser.Implementation(
			roots                                                             ,
			coalesce(skipped, X.empty()).immure()                             ,
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s)),
			referenceHandlerProvider                                          ,
			notNull(traverserProvider)                                        ,
			predicateSkip                                                     ,
			predicateNode                                                     ,
			predicateLeaf                                                     ,
			predicateFull                                                     ,
			predicateHandle                                                   ,
			traversalAcceptor                                                 ,
			traversalMutator                                                  ,
			mutationListener                                                  ,
			traversalMode                                                     ,
			initializerLogic                                                  ,
			finalizerLogic
		);
	}
	
	public final class Implementation implements ObjectGraphTraverser
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Object[]                                           roots                   ;
		private final XGettingCollection<Object>                         skipped                 ;
		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ;
		private final TraversalPredicateSkip                             predicateSkip           ;
		private final TraversalPredicateNode                             predicateNode           ;
		private final TraversalPredicateLeaf                             predicateLeaf           ;
		private final TraversalPredicateFull                             predicateFull           ;
		private final Predicate<Object>                                  predicateHandle         ;
		private final TraversalAcceptor                                  traversalAcceptor       ;
		private final TraversalMutator                                   traversalMutator        ;
		private final TraversalReferenceHandlerProvider                  referenceHandlerProvider;
		private final TypeTraverserProvider                              traverserProvider       ;
		private final MutationListener                                   mutationListener        ;
		private final TraversalMode                                      traversalMode           ;
		private final Runnable                                           initializerLogic        ;
		private final Runnable                                           finalizerLogic          ;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		Implementation(
			final Object[]                                           roots                   ,
			final XGettingCollection<Object>                         skipped                 ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider  ,
			final TraversalReferenceHandlerProvider                  referenceHandlerProvider,
			final TypeTraverserProvider                              traverserProvider       ,
			final TraversalPredicateSkip                             predicateSkip           ,
			final TraversalPredicateNode                             predicateNode           ,
			final TraversalPredicateLeaf                             predicateLeaf           ,
			final TraversalPredicateFull                             predicateFull           ,
			final Predicate<Object>                                  predicateHandle         ,
			final TraversalAcceptor                                  traversalAcceptor       ,
			final TraversalMutator                                   traversalMutator        ,
			final MutationListener                                   mutationListener        ,
			final TraversalMode                                      traversalMode           ,
			final Runnable                                           initializerLogic        ,
			final Runnable                                           finalizerLogic
		)
		{
			super();
			this.roots                    = roots                   ;
			this.skipped                  = skipped                 ;
			this.alreadyHandledProvider   = alreadyHandledProvider  ;
			this.predicateSkip            = predicateSkip           ;
			this.predicateNode            = predicateNode           ;
			this.predicateLeaf            = predicateLeaf           ;
			this.predicateFull            = predicateFull           ;
			this.predicateHandle          = predicateHandle         ;
			this.traversalAcceptor        = traversalAcceptor       ;
			this.traversalMutator         = traversalMutator        ;
			this.referenceHandlerProvider = referenceHandlerProvider;
			this.traverserProvider        = traverserProvider       ;
			this.mutationListener         = mutationListener        ;
			this.traversalMode            = traversalMode           ;
			this.initializerLogic         = initializerLogic        ;
			this.finalizerLogic           = finalizerLogic          ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		protected final synchronized void internalTraverseAll(
			final Object[]          instances               ,
			final TraversalAcceptor traversalAcceptor       ,
			final TraversalMutator  traversalMutator        ,
			final MutationListener  mutationListener
		)
		{
			if(this.initializerLogic != null)
			{
				this.initializerLogic.run();
			}
			
			final AbstractReferenceHandler referenceHandler = this.referenceHandlerProvider.provideReferenceHandler(
				this.alreadyHandledProvider.apply(this.skipped),
				this.traverserProvider                         ,
				this.predicateSkip                             ,
				this.predicateNode                             ,
				this.predicateLeaf                             ,
				this.predicateFull                             ,
				this.predicateHandle                           ,
				traversalAcceptor                              ,
				traversalMutator                               ,
				mutationListener
			);
			this.traversalMode.handle(instances, referenceHandler);
			
			if(this.finalizerLogic != null)
			{
				this.finalizerLogic.run();
			}
		}
		
		
		@Override
		public void traverse()
		{
			this.internalTraverseAll(
				this.roots            ,
				this.traversalAcceptor,
				this.traversalMutator ,
				this.mutationListener
			);
		}
		
		@Override
		public void traverseAll(final Object[] instances)
		{
			this.internalTraverseAll(
				instances             ,
				this.traversalAcceptor,
				this.traversalMutator ,
				this.mutationListener
			);
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverse(final A acceptor)
		{
			this.internalTraverseAll(this.roots,  acceptor, null, null);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverse(final M mutator)
		{
			this.internalTraverseAll(this.roots, null, mutator, null);
			return mutator;
		}
		
		@Override
		public <A extends TraversalAcceptor> A traverseAll(final Object[] instances, final A acceptor)
		{
			this.internalTraverseAll(instances, acceptor, null, null);
			return acceptor;
		}
		
		@Override
		public <M extends TraversalMutator> M traverseAll(final Object[] instances, final M mutator)
		{
			this.internalTraverseAll(instances, null, mutator, null);
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
		static final int SEGMENT_SIZE       = 500;
		static final int SEGMENT_LAST_INDEX = SEGMENT_SIZE - 1;
		static final int SEGMENT_LENGTH     = SEGMENT_SIZE + 1;



		///////////////////////////////////////////////////////////////////////////
		// static methods //
		///////////////////

		static final Object[] createIterationSegment()
		{
			// one trailing slot as a pointer to the next segment array. Both hacky and elegant.
			return new Object[SEGMENT_LENGTH];
		}
				
	}
			
}
