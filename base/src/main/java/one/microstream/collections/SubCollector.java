package one.microstream.collections;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XPuttingSequence;

public class SubCollector<E> extends SubView<E> implements XPuttingSequence<E>
{

	@SafeVarargs
	@Override
	public final SubCollector<E> putAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> putAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final boolean nullPut()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final boolean put(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final boolean add(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@SafeVarargs
	@Override
	public final SubCollector<E> addAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> addAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> addAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final void accept(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final boolean nullAdd()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final long currentCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> ensureCapacity(final long minimalCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final SubCollector<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public final long optimize()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

}
