package one.microstream.functional;

import one.microstream.collections.BulkList;

public final class AggregateArrayBuilder<E> implements Aggregator<E, E[]>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final AggregateArrayBuilder<Object> New()
	{
		return New(Object.class);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType)
	{
		return New(elementType, 1);
	}

	public static final <E> AggregateArrayBuilder<E> New(final Class<E> elementType, final int initialCapacity)
	{
		return new AggregateArrayBuilder<>(elementType, new BulkList<E>(initialCapacity));
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final Class<E>    elementType;
	final BulkList<E> collector  ;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	AggregateArrayBuilder(final Class<E> elementType, final BulkList<E> collector)
	{
		super();
		this.elementType = elementType;
		this.collector   = collector  ;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void accept(final E element)
	{
		this.collector.add(element);
	}

	@Override
	public final E[] yield()
	{
		return this.collector.toArray(this.elementType);
	}

}
