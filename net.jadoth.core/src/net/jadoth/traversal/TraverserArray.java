package net.jadoth.traversal;

public final class TraverserArray implements TypeTraverser<Object[]>
{
	@Override
	public void traverseReferences(
		final Object[]          instance,
		final TraversalAcceptor acceptor,
		final TraversalEnqueuer enqueuer
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				acceptor.acceptReference(instance[i], instance, enqueuer);
				enqueuer.enqueue(instance[i]);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	
	@Override
	public final void traverseReferences(
		final Object[]          instance        ,
		final TraversalMutator  mutator         ,
		final TraversalEnqueuer enqueuer        ,
		final MutationListener  mutationListener
	)
	{
		final int length = instance.length;
		try
		{
			for(int i = 0; i < length; i++)
			{
				final Object current, returned;
				if((returned = mutator.mutateReference(current = instance[i], instance, enqueuer)) != current)
				{
					if(mutationListener != null)
					{
						mutationListener.registerChange(instance, current, returned);
					}
					instance[i] = returned;
				}
				
				// note: if the current (now prior) value has to be enqueued, the acceptor can do that internally
				enqueuer.enqueue(returned);
			}
		}
		catch(final AbstractTraversalSkipSignal s)
		{
			// any skip signal reaching this point means abort the whole instance, in one way or another
		}
	}
	

	
	/* (29.06.2017 TM)NOTE:
	 * lots of fancy stuff with fine-grained loop control mechanisms, but in the end:
	 * - how often is something like that realy needed that can't be done with skip() and enqueue()?
	 * - how high is the price in performance and code complexity to pay for such a special nice-to-have feature?
	 * - if it is REALLY needed, it's ridiculously easy to provide a tailored implementation to do it.
	 * 
	 */
//	@Override
//	public final void traverseReferences(final Object instance, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
//	{
//		try
//		{
//			this.handleAndTraverse((Object[])instance, acceptor, enqueuer);
//		}
//		catch(final AbstractTraversalSkipSignal s)
//		{
//			// any skip signal reaching this point means abort the whole instance, in one way or another
//		}
//	}
//
//	private void handleAndTraverse(final Object[] array, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
//	{
//		final int length = array.length;
//		int i = 0;
//
//		try
//		{
//			while(i < length)
//			{
//				final Object current, v;
//				if((v = acceptor.acceptInstance(current = array[i], array, enqueuer)) != current)
//				{
//					array[i] = v;
//				}
//				enqueuer.enqueue(v);
//				i++;
//			}
//		}
//		catch(final TraversalSignalSkipHandlingReferences s)
//		{
//			this.traverseOnly(array, i, enqueuer);
//		}
//		catch(final TraversalSignalSkipTraversingReferences s)
//		{
//			this.handleOnly(array, i, acceptor, enqueuer);
//		}
//		catch(final TraversalSignalSkipInstance s)
//		{
//			// skip completely, meaning abort
//			return;
//		}
//	}
//
//	private void traverseOnly(final Object[] array, final int index, final TraversalEnqueuer enqueuer)
//	{
//		final int length = array.length;
//		for(int i = index; i < length; i++)
//		{
//			enqueuer.enqueue(array[i]);
//		}
//	}
//
//	private void handleOnly(final Object[] array, final int index, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
//	{
//		final int length = array.length;
//		for(int i = index; i < length; i++)
//		{
//			final Object current, v;
//			if((v = acceptor.acceptInstance(current = array[i], array, enqueuer)) != current)
//			{
//				array[i] = v;
//			}
//		}
//	}

}
