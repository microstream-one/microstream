package net.jadoth.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.old.OldCollection;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XImmutableSequence;
import net.jadoth.functional.BiProcedure;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.util.Equalator;

public class SubView<E> implements XGettingSequence<E>
{
	/* (12.07.2012 TM)FIXME: implement SubView
	 * (see all not implemented errors in method stubs)
	 */

	@Override
	public XGettingSequence<E> copy()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset, final long offset, final int length)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME SubView#iterate()
	}

	@Override
	public final <P extends IndexProcedure<? super E>> P iterateIndexed(final P procedure)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME SubView#iterate()
	}

	@Override
	public <A> A join(final BiProcedure<? super E, ? super A> joiner, final A aggregate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIX-ME SubView#join()
	}

	@Override
	public E get()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E first()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E at(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public XImmutableSequence<E> immure()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long indexOf(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

//	@Override
//	public int indexOf(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError();
//	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E last()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long lastIndexOf(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E peek()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E poll()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> range(final long lowIndex, final long highIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> toReversed()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> view()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public XGettingSequence<E> view(final long lowIndex, final long highIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean contains(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

//	@Override
//	public boolean contains(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError();
//	}

//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError();
//	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean containsId(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T> T[] copyTo(final T[] target, final int targetOffset)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long count(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

//	@Override
//	public int count(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError();
//	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasDistinctValues()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasDistinctValues(final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean hasVolatileElements()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean isEmpty()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullContained()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public OldCollection<E> old()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

//	@Override
//	public E search(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new net.jadoth.meta.NotImplementedYetError();
//	}

	@Override
	public E seek(final E sample)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long size()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public Object[] toArray()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean nullAllowed()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long remainingCapacity()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public boolean isFull()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public long maximumCapacity()
	{
		throw new net.jadoth.meta.NotImplementedYetError();
	}

}
