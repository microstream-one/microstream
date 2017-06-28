package net.jadoth.traversal2;

public final class TraverserArray implements TraversalHandler
{
	@Override
	public void traverseReferences(final Object instance, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		final Object[] array  = (Object[])instance;
		final int      length = array.length;
		
		for(int i = 0; i < length; i++)
		{
			final Object current, v;
			if((v = acceptor.acceptInstance(current = array[i], array, enqueuer)) != current)
			{
				array[i] = v;
			}
		}
	}

}
