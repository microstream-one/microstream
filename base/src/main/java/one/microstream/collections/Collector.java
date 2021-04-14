package one.microstream.collections;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XPuttingCollection;
import one.microstream.typing.XTypes;


public final class Collector<E> implements XPuttingCollection<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XPuttingCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Collector(final XPuttingCollection<E> collection)
	{
		super();
		this.subject = collection;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public boolean nullAdd()
	{
		return this.subject.nullAdd();
	}

	@Override
	public boolean add(final E e)
	{
		return this.subject.add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collector<E> addAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public Collector<E> addAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public Collector<E> addAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public boolean nullPut()
	{
		return this.subject.nullAdd();
	}

	@Override
	public void accept(final E e)
	{
		this.subject.add(e);
	}

	@Override
	public boolean put(final E element)
	{
		return this.subject.add(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collector<E> putAll(final E... elements)
	{
		this.subject.addAll(elements);
		return this;
	}

	@Override
	public Collector<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.subject.addAll(elements, offset, length);
		return this;
	}

	@Override
	public Collector<E> putAll(final XGettingCollection<? extends E> elements)
	{
		this.subject.addAll(elements);
		return this;
	}



	@Override
	public Collector<E> ensureCapacity(final long minimalCapacity)
	{
		this.subject.ensureCapacity(minimalCapacity);
		return this;
	}

	@Override
	public long currentCapacity()
	{
		return this.subject.currentCapacity();
	}

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public boolean isFull()
	{
		return XTypes.to_int(this.subject.size()) >= this.subject.maximumCapacity();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public Collector<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public boolean isEmpty() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long size() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}



}
