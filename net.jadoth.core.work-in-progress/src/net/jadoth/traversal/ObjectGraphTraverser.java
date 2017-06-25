package net.jadoth.traversal;

import static net.jadoth.Jadoth.coalesce;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.Jadoth;
import net.jadoth.collections.HashTable;
import net.jadoth.collections.X;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XList;
import net.jadoth.collections.types.XSet;
import net.jadoth.functional.JadothPredicates;
import net.jadoth.traversal.TraversalHandler;
import net.jadoth.traversal.TraversalHandlerProvider;
import net.jadoth.traversal2.OpenAdressingMiniSet;
import net.jadoth.traversal2.TraversalSignalAbort;
import net.jadoth.traversal2.TraversalSignalSkipInstance;


@FunctionalInterface
public interface ObjectGraphTraverser
{
	public default void traverse(final Object instance)
	{
		this.traverseAll(Jadoth.array(instance), null);
	}

	public default <C extends Consumer<Object>> C traverse(final Object instance, final C matchListener)
	{
		this.traverseAll(Jadoth.array(instance), matchListener);
		return matchListener;
	}

	public default void traverseAll(final Object[] instances)
	{
		this.traverseAll(instances, null);
	}

	public <C extends Consumer<Object>> C traverseAll(Object[] instances, C matchListener);

	public static <C extends Consumer<Object>> C traverseGraph(final Object root, final C matchListener)
	{
		return traverseGraph(root, JadothPredicates.all(), matchListener);
	}

	public static <C extends Consumer<Object>> C traverseGraph(
		final Object            root         ,
		final Predicate<Object> filter       ,
		final C                 matchListener
	)
	{
		final ObjectGraphTraverser traverser = ObjectGraphTraverser.Factory()
			.setHandlingLogic(filter)
			.buildObjectGraphTraverser()
		;
		traverser.traverse(root, matchListener);

		return matchListener;
	}

	public static <T, C extends Consumer<? super T>> C traverseGraph(
		final Object              root         ,
		final Predicate<Object>   filter       ,
		final Function<Object, T> projector    ,
		final C                   matchListener
	)
	{
		final ObjectGraphTraverser traverser = ObjectGraphTraverser.Factory()
			.setHandlingLogic(filter)
			.buildObjectGraphTraverser()
		;
		traverser.traverse(root, e -> matchListener.accept(projector.apply(e)));

		return matchListener;
	}

	public static <T, C extends Consumer<? super T>> C selectFromGraph(
		final Object               root         ,
		final Class<T>             filterType   ,
		final Predicate<? super T> instanceFilter ,
		final C                    matchListener
	)
	{
		final ObjectGraphTraverser traverser = ObjectGraphTraverser.Factory()
			.setHandlingLogic(e -> filterType.isInstance(e) && instanceFilter.test(filterType.cast(e)))
			.buildObjectGraphTraverser()
		;
		traverser.traverse(root, e -> matchListener.accept(filterType.cast(e)));

		return matchListener;
	}

	public static <T> XList<T> selectFromGraph(
		final Object               root         ,
		final Class<T>             filterType   ,
		final Predicate<? super T> instanceFilter
	)
	{
		return selectFromGraph(root, filterType, instanceFilter, X.List());
	}


	public static void signalAbortTraversal() throws TraversalSignalAbort
	{
		TraversalSignalAbort.fire();
	}

	public static void signalSkipInstance() throws TraversalSignalSkipInstance
	{
		TraversalSignalSkipInstance.fire();
	}




	public static ObjectGraphTraverserFactory Factory()
	{
		return new ObjectGraphTraverserFactory.Implementation();
	}

	public static ObjectGraphTraverser New(
		final TraversalHandlerProvider                           handlerProvider       ,
		final XGettingCollection<Object>                         skipped               ,
		final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider
	)
	{
		return new Implementation(
			handlerProvider,
			coalesce(skipped.immure(), X.empty()),
			coalesce(alreadyHandledProvider, s -> OpenAdressingMiniSet.New(s))
		);
	}

	public final class Implementation implements ObjectGraphTraverser
	{
		/* Possible performance optimizations:
		 * x use direct field offsets via Unsafe instead of Reflection (see TraversalHandlerReflective comment).
		 * - minimalistic (e.g. non-ordered) Set implementation for alreadyHandled, maybe even flat-hashing-array
		 * - array-segment list instead of iteration head
		 * - provide completed handler lookup table instead of generating handlers on the fly, but should be negligible
		 */

		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

		static final TraversalHandler<?> UNHANDLED = (e, h) ->
		{
			// empty
		};




		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider;
		private final TraversalHandlerProvider                           handlerProvider       ;
		private final XGettingCollection<Object>                         skipped               ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Implementation(
			final TraversalHandlerProvider                           handlerProvider       ,
			final XGettingCollection<Object>                         skipped               ,
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider
		)
		{
			super();
			this.handlerProvider        = handlerProvider       ;
			this.skipped                = skipped               ;
			this.alreadyHandledProvider = alreadyHandledProvider;
		}



		///////////////////////////////////////////////////////////////////////////
		// constants        //
		/////////////////////

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

		final class ReferenceHandler implements Consumer<Object>
		{
			private final TraversalHandlerProvider                 handlerProvider ;
			private final HashTable<Class<?>, TraversalHandler<?>> instanceHandlers;
			private final XSet<Object>                             alreadyHandled  ;

			Object[] iterationTail      = createIterationSegment();
			Object[] iterationHead      = this.iterationTail;
			boolean  tailIsHead         = true;
			int      iterationTailIndex;
			int      iterationHeadIndex;

			ReferenceHandler(
				final TraversalHandlerProvider handlerProvider,
				final XSet<Object>             alreadyHandled ,
				final Object[]                 instances
			)
			{
				super();
				this.handlerProvider  = handlerProvider;
				this.alreadyHandled   = alreadyHandled ;
				this.instanceHandlers = HashTable.New();

				for(final Object instance : instances)
				{
					if(instance == null)
					{
						continue;
					}
					this.accept(instance);
				}
			}

			final <T> TraversalHandler<T> ensureTraversalHandler(final Class<T> type)
			{
				TraversalHandler<?> handler = this.instanceHandlers.get(type);
				if(handler == null)
				{
					handler = this.handlerProvider.provideTraversalHandler(type);

					// the provider can decide that this type should not be handled (meaning not providing a handler)
					if(handler == null)
					{
						// if so, an "unhandled" dummy handler is used to avoid repeated provider calls
						handler = UNHANDLED;
					}

					this.instanceHandlers.add(type, handler);
				}

				@SuppressWarnings("unchecked") // cast safety guaranteed by registering and "dummy" logic
				final TraversalHandler<T> castedHandler = (TraversalHandler<T>)handler;

				return castedHandler;
			}

			private final <T> void addIterationItem(final T subject, final TraversalHandler<T> handler)
			{
				if(this.iterationHeadIndex >= SEGMENT_SIZE)
				{
					final Object[] nextIterationSegment = createIterationSegment();
					this.iterationHead[SEGMENT_SIZE] = nextIterationSegment;
					this.iterationHead = nextIterationSegment;
					this.iterationHeadIndex = 0;
					this.tailIsHead = false;
				}
				this.iterationHead[this.iterationHeadIndex  ] = subject;
				this.iterationHead[this.iterationHeadIndex + 1] = handler;
				this.iterationHeadIndex += 2;
			}

			final void handleNext(final Consumer<Object> matchListener) throws TraversalSignalAbort
			{
				if(this.tailIsHead)
				{
					if(this.iterationTailIndex >= this.iterationHeadIndex)
					{
						ObjectGraphTraverser.signalAbortTraversal();
					}
				}
				if(this.iterationTailIndex >= SEGMENT_SIZE)
				{
					this.iterationTail = (Object[])this.iterationTail[SEGMENT_SIZE];
					this.iterationTailIndex = 0;
					this.tailIsHead = this.iterationTail == this.iterationHead;
				}

				final Object                   item    = this.iterationTail[this.iterationTailIndex];
				@SuppressWarnings("unchecked")
				final TraversalHandler<Object> handler =
					(TraversalHandler<Object>)this.iterationTail[this.iterationTailIndex + 1]
				;
				this.iterationTailIndex += 2;

				try
				{
					if(handler.handleObject(item, this) && matchListener != null)
					{
						matchListener.accept(item);
					}
					handler.traverseReferences(item, this);
				}
				catch(final TraversalSignalSkipInstance s)
				{
					/*
					 * some logic (the handler or an evaluating logic it deferred to)
					 * signaled to skip the current instance
					 */
				}
			}

			@Override
			public final void accept(final Object instance)
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

				TraversalHandler<?> handler = this.instanceHandlers.get(instance.getClass());
				if(handler == null)
				{
					handler = this.handlerProvider.provideTraversalHandler(instance.getClass());

					// the provider can decide that this type should not be handled (meaning not providing a handler)
					if(handler == null)
					{
						// if so, an "unhandled" dummy handler is used to avoid repeated provider calls
						handler = UNHANDLED;
					}

					this.instanceHandlers.add(instance.getClass(), handler);
				}

				if(handler != UNHANDLED)
				{
					@SuppressWarnings("unchecked") // type erasure loophole annoying stuff
					final TraversalHandler<Object> castedHandler = (TraversalHandler<Object>)handler;
					this.addIterationItem(instance, castedHandler);
				}
			}

		}



		///////////////////////////////////////////////////////////////////////////
		// override methods //
		/////////////////////

		@Override
		public synchronized <C extends Consumer<Object>> C traverseAll(final Object[] instances, final C matchListener)
		{
			final ReferenceHandler referenceHandler = new ReferenceHandler(
				this.handlerProvider,
				this.alreadyHandledProvider.apply(this.skipped),
				instances
			);

			try
			{
				while(true)
				{
					referenceHandler.handleNext(matchListener);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return matchListener;
			}


		}

	}

}
