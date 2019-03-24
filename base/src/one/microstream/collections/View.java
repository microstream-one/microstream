package one.microstream.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.old.OldCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableCollection;
import one.microstream.equality.Equalator;
import one.microstream.typing.XTypes;

public class View<E> implements XGettingCollection<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final XGettingCollection<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public View(final XGettingCollection<E> collection)
	{
		super();
		this.subject = collection;
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public synchronized E get()
	{
		return this.subject.get();
	}

	@Override
	public Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

//	@Override
//	public <R> R aggregate(final Aggregator<? super E, R> aggregate)
//	{
//		return this.subject.iterate(aggregate);
//	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

//	@Override
//	public boolean contains(final E sample, final Equalator<? super E> equalator)
//	{
//		return this.collection.contains(sample, equalator);
//	}

	@Override
	public boolean nullAllowed()
	{
		return this.subject.nullAllowed();
	}

	@Override
	public boolean nullContained()
	{
		return this.subject.nullContained();
	}

//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		return this.subject.containsAll(samples, equalator);
//	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public View<E> copy()
	{
		return new View<>(this.subject);
	}

	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.subject.distinct(target);
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(this.subject, equalator);
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(this.subject, equalator);
	}

	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.except(other, equalator, target);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		return this.subject.iterate(procedure);
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

//	@Override
//	public boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return this.subject.hasDistinctValues(equalator);
//	}
//
//	@Override
//	public boolean hasDistinctValues()
//	{
//		return this.subject.hasDistinctValues();
//	}

	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

//	@Override
//	public E search(final E sample, final Equalator<? super E> equalator)
//	{
//		return this.collection.search(sample, equalator);
//	}

	@Override
	public E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives  //
	////////////////////////////////////

	@Deprecated
	@Override
	public int hashCode()
	{
		return this.subject.hashCode();
	}

	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public XImmutableCollection<E> immure()
	{
		return this.subject.immure();
	}

	@Override
	public XGettingCollection<E> view()
	{
		return this;
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public OldCollection<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

}
