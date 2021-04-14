package one.microstream.collections;

import one.microstream.collections.types.XAddingCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.concurrency.Synchronized;


public final class SynchAdder<E> implements XAddingCollection<E>, Synchronized
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XAddingCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SynchAdder(final XAddingCollection<E> collection)
	{
		super();
		this.subject = collection;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final synchronized boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public final synchronized boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@SafeVarargs
	@Override
	public final synchronized SynchAdder<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public final synchronized void accept(final E e)
	{
		this.subject.accept(e);
	}

	@Override
	public final synchronized SynchAdder<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public final synchronized SynchAdder<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Override
	public final synchronized long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public final synchronized long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public final synchronized boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public final synchronized long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public final synchronized long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public final synchronized boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public final synchronized boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public final synchronized boolean isEmpty() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public final synchronized long size() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

}
