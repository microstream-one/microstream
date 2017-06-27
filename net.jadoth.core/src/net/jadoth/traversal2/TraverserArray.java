package net.jadoth.traversal2;

public final class TraverserArray implements ReferenceAccessor, TraversalHandler
{
	private int index;

	@Override
	public Object get(final Object parent)
	{
		return ((Object[])parent)[this.index];
	}

	@Override
	public void set(final Object parent, final Object newValue)
	{
		((Object[])parent)[this.index] = newValue;
	}

	@Override
	public void traverseReferences(final Object instance, final TraversalAcceptor acceptor, final TraversalEnqueuer enqueuer)
	{
		final Object[] array  = (Object[])instance;
		final int      length = array.length;
		
		for(this.index = 0; this.index < length; this.index++)
		{
			acceptor.acceptInstance(array[this.index], array, this, enqueuer);
		}
	}

}
