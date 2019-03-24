package one.microstream.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.BulkList;
import one.microstream.collections.IndexExceededException;
import one.microstream.collections.ListView;
import one.microstream.collections.XArrays;
import one.microstream.collections.old.OldList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XImmutableList;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;

public final class CompositeList<E> implements XGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	public static final <E> CompositeList<E> New(final XGettingList<E>[] lists)
	{
		return New(lists, 0, lists.length);
	}

	public static final <E> CompositeList<E> New(final XGettingList<E>[] lists, final int length)
	{
		return New(lists, 0, length);
	}

	public static final <E> CompositeList<E> New(final XGettingList<E>[] lists, final int offset, final int length)
	{
		return new CompositeList<>(lists, offset, length);
	}



	/* (19.08.2013)FIXME: complete CompositeList implementation
	 * (reenable and process all FIX-ME below)
	 */

	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	final XGettingList<E>[] lists;
	final int               count;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	CompositeList(final XGettingList<E>[] lists, final int offset, final int length)
	{
		super();
		this.lists = XArrays.copyRange(lists, offset, length);
		this.count = length;
	}



	///////////////////////////////////////////////////////////////////////////
	// getters         //
	/////////////////////

	@Override
	public final E get()
	{
		return this.lists[0].get();
	}

	@Override
	public final E at(final long index)
	{
		final XGettingList<E>[] lists = this.lists;

		// this can get pretty inefficient. However, the main intention of this implementation is to use iterate anyway
		for(int c = 0, i = X.checkArrayRange(index); c < this.count; c++)
		{
			if(i < lists[c].size())
			{
				return lists[c].at(i);
			}
			i -= lists[c].size();
		}
		throw new IndexExceededException(index, XTypes.to_int(this.size()));
	}

	@Override
	public final E first()
	{
		return this.lists[0].first();
	}

	@Override
	public final E last()
	{
		return this.lists[this.count - 1].last();
	}

	@Override
	public final E poll()
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = 0; c < this.count; c++)
		{
			if(!lists[c].isEmpty())
			{
				return lists[c].first(); // existence of first element ensured before
			}
		}
		return null; // no list contains any element
	}

	@Override
	public final E peek()
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(!lists[c].isEmpty())
			{
				return lists[c].last(); // existence of last element ensured before
			}
		}
		return null; // no list contains any element
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#maxIndex()
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#minIndex()
	}

	@Override
	public final long indexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#indexOf()
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#indexOf()
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#scan()
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#isSorted()
	}

	@Override
	public final <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingSequence<E>#copySelection()
	}

	@Override
	public final Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#iterator()
	}

	private static <S, E extends S> S[] toArray(final CompositeList<E> subject, final Class<S> type)
	{
		final XGettingList<E>[] lists = subject.lists;

		final BulkList<S> buffer = new BulkList<>(XTypes.to_int(subject.size())); // size() should be better than frequent bulk rebuilds
		for(int c = 0; c < subject.count; c++)
		{
			buffer.addAll(lists[c]);
		}
		return buffer.toArray(type);
	}

	@Override
	public final Object[] toArray()
	{
		return toArray(this, Object.class); // oh the irony...
	}

	@Override
	public final E[] toArray(final Class<E> type)
	{
		return toArray(this, type);
	}

	@Override
	public final boolean hasVolatileElements()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#hasVolatileElements()
	}

	@Override
	public final long size()
	{
		final XGettingList<E>[] lists = this.lists;

		int size = 0;
		for(int c = this.count; c --> 0;)
		{
			size += lists[c].size();
		}
		return size;
	}

	@Override
	public final boolean isEmpty()
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(!lists[c].isEmpty())
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public final Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equality()
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equals()
	}

	@Override
	public final boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#equalsContent()
	}

	@Override
	public final boolean nullContained()
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(lists[c].nullContained())
			{
				return true;
			}
		}
		return false;
	}

//	@Override
//	public final <R> R aggregate(final Aggregator<? super E, R> aggregate)
//	{
//		final XGettingList<E>[] lists = this.lists;
//
//		for(int c = 0; c < this.count; c++)
//		{
//			lists[c].iterate(aggregate);
//		}
//		return aggregate.yield();
//	}

	@Override
	public final boolean containsId(final E element)
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(lists[c].containsId(element))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final E element)
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(lists[c].contains(element))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = this.count; c --> 0;)
		{
			if(lists[c].containsSearched(predicate))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#containsAll()
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#applies()
	}

	@Override
	public final long count(final E element)
	{
		final XGettingList<E>[] lists = this.lists;

		int count = 0;
		for(int c = this.count; c --> 0;)
		{
			count += lists[c].count(element);
		}
		return count;
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		final XGettingList<E>[] lists = this.lists;

		int count = 0;
		for(int c = 0; c < this.count; c++)
		{
			count += lists[c].countBy(predicate);
		}
		return count;
	}

//	@Override
//	public final boolean hasDistinctValues()
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#hasDistinctValues()
//	}
//
//	@Override
//	public final boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#hasDistinctValues()
//	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#search()
	}

	@Override
	public final E seek(final E sample)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#seek()
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#max()
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#min()
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#distinct()
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#distinct()
	}

	@Override
	public final <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#copyTo()
	}

	@Override
	public final <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#filterTo()
	}

	@Override
	public final <T extends Consumer<? super E>> T union(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#union()
	}

	@Override
	public final <T extends Consumer<? super E>> T intersect(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#intersect()
	}

	@Override
	public final <T extends Consumer<? super E>> T except(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingCollection<E>#except()
	}

	@Override
	public final boolean nullAllowed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME ExtendedCollection<E>#nullAllowed()
	}

	@Override
	public final long maximumCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#maximumCapacity()
	}

	@Override
	public final long remainingCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#freeCapacity()
	}

	@Override
	public final boolean isFull()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME CapacityCarrying#isFull()
	}

	@Override
	public final XImmutableList<E> immure()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#immure()
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#listIterator()
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#listIterator()
	}

	@Override
	public final OldList<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#old()
	}

	@Override
	public final XGettingList<E> copy()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#copy()
	}

	@Override
	public final XGettingList<E> toReversed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#toReversed()
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = 0; c < this.count; c++)
		{
			lists[c].iterate(procedure);
		}

		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		final XGettingList<E>[] lists = this.lists;

		for(int c = 0; c < this.count; c++)
		{
			lists[c].join(joiner, aggregate);
		}

		return aggregate;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		// this will get really ugly... :-/
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#iterate()
	}

	@Override
	public final XGettingList<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public final XGettingList<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#view()
	}

	@Override
	public final XGettingList<E> range(final long fromIndex, final long toIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME XGettingList<E>#range()
	}

}
