package net.jadoth.traversal2;

import java.util.function.Function;

import net.jadoth.Jadoth;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XSet;

public interface ObjectTraverser
{
	public default void traverse(final Object instance)
	{
		this.traverseAll(Jadoth.array(instance), null);
	}

	public default <A extends TraversalAcceptor> A traverse(final Object instance, final A acceptor)
	{
		this.traverseAll(Jadoth.array(instance), acceptor);
		return acceptor;
	}

	public default void traverseAll(final Object[] instances)
	{
		this.traverseAll(instances, null);
	}

	public <A extends TraversalAcceptor> A traverseAll(Object[] instances, A acceptor);
	
	
	public static void signalAbortTraversal() throws TraversalSignalAbort
	{
		TraversalSignalAbort.fire();
	}

	public static void signalSkipInstance() throws TraversalSignalSkipInstance
	{
		TraversalSignalSkipInstance.fire();
	}
	
	
	public final class Implementation implements ObjectTraverser
	{
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
			final Function<XGettingCollection<Object>, XSet<Object>> alreadyHandledProvider,
			final TraversalHandlerProvider                           handlerProvider       ,
			final XGettingCollection<Object>                         skipped
		)
		{
			super();
			this.alreadyHandledProvider = alreadyHandledProvider;
			this.handlerProvider        = handlerProvider       ;
			this.skipped                = skipped               ;
		}
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final synchronized <A extends TraversalAcceptor> A traverseAll(
			final Object[] instances    ,
			final A        acceptor
		)
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
					referenceHandler.handleNext(acceptor);
				}
			}
			catch(final TraversalSignalAbort s)
			{
				// some logic signaled to abort the traversal. So abort and fall through to returning.
				return acceptor;
			}
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
			
			private final TraversalHandlerProvider handlerProvider ;
			private final XSet<Object>             alreadyHandled  ;

			Object[] iterationTail      = createIterationSegment();
			Object[] iterationHead      = this.iterationTail;
			boolean  tailIsHead         = true;
			int      iterationTailIndex;
			int      iterationHeadIndex;

			
			
			///////////////////////////////////////////////////////////////////////////
			// constructors //
			/////////////////
			
			ReferenceHandler(
				final TraversalHandlerProvider handlerProvider,
				final XSet<Object>             alreadyHandled ,
				final Object[]                 instances
			)
			{
				super();
				this.handlerProvider = handlerProvider;
				this.alreadyHandled  = alreadyHandled ;

				for(final Object instance : instances)
				{
					if(instance == null)
					{
						continue;
					}
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
				
				if(this.iterationHeadIndex >= SEGMENT_SIZE)
				{
					final Object[] nextIterationSegment = createIterationSegment();
					this.iterationHead[SEGMENT_SIZE] = nextIterationSegment;
					this.iterationHead = nextIterationSegment;
					this.iterationHeadIndex = 0;
					this.tailIsHead = false;
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
				if(this.iterationTailIndex >= SEGMENT_SIZE)
				{
					this.advanceSegment();
				}
				
				return this.iterationTail[this.iterationTailIndex++];
			}
			
			private void checkForCompletion()
			{
				if(this.iterationTailIndex >= this.iterationHeadIndex)
				{
					ObjectTraverser.signalAbortTraversal();
				}
			}
			
			private void advanceSegment()
			{
				this.iterationTail      = (Object[])this.iterationTail[SEGMENT_SIZE];
				this.iterationTailIndex = 0;
				this.tailIsHead         = this.iterationTail == this.iterationHead;
			}
						
			final void handleNext(final TraversalAcceptor acceptor) throws TraversalSignalAbort
			{
				final Object           instance = this.dequeue();
				final TraversalHandler handler  = this.handlerProvider.provideTraversalHandler(instance);
				handler.traverseReferences(instance, acceptor, this.alreadyHandled.add(instance) ? this : null);
			}
			
		}
				
	}
	
}
