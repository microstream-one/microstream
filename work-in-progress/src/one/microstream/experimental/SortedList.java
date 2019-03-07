package one.microstream.experimental;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.ConstList;
import one.microstream.collections.SubListView;
import one.microstream.collections.XSort;
import one.microstream.collections.old.OldList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XProcessingList;
import one.microstream.collections.types.XPuttingList;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;

/**
 * @author Thomas Muenz
 *
 */
// (05.03.2011)FIXME: overhaul as Sortation XCollection branch...
@Deprecated
public final class SortedList<E> implements XPuttingList<E>, XProcessingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	//  static methods  //
	/////////////////////

	static String exceptionStringSkipNegative(final int skip)
	{
		return "Skip count may not be negative: "+skip;
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final XList<E> subject;
	private final Comparator<E> comparator;
	        final Equalator<E> notEqual;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public SortedList(final XList<E> list, final Comparator<E> comparator)
	{
		super();
		if(!list.isSorted(comparator))
		{
			list.sort(comparator); // list has to be sorted for the SortedList to work
		}
		this.subject = list;
		this.comparator = comparator;
		this.notEqual = new Equalator<E>(){
			@Override public boolean equal(final E e1, final E e2) {
				return comparator.compare(e1, e2) != 0;
			}
		};
	}



	///////////////////////////////////////////////////////////////////////////
	// getters          //
	/////////////////////

	public Comparator<E> getComparator()
	{
		return this.comparator;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////





	///////////////////////////////////////////////////////////////////////////
	// constant override methods //
	//////////////////////////////

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
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
//		return this.list.contains(sample, equalator);
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
	public SortedList<E> copy()
	{
		return new SortedList<>(this.subject, this.comparator);
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
	public <C extends Consumer<? super E>> C except(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final C target)
	{
		return this.subject.except(other, equalator, target);
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		return this.subject.iterate(procedure);
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		return this.subject.iterateIndexed(procedure);
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public E get()
	{
		return this.subject.get();
	}

	@Override
	public E first()
	{
		return this.subject.first();
	}

	@Override
	public E last()
	{
		return this.subject.last();
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

//	@Override
//	public int indexOf(final E sample, final Equalator<? super E> equalator)
//	{
//		return this.list.indexOf(sample, equalator);
//	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public <C extends Consumer<? super E>> C intersect(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final C target)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(predicate);
	}

//	@Override
//	public E search(final E sample, final Equalator<? super E> equalator)
//	{
//		return this.list.search(sample, equalator);
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
	public XGettingList<E> range(final long fromIndex, final long toIndex)
	{
		return this.subject.range(fromIndex, toIndex);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public SortedList<E> toReversed()
	{
		return new SortedList<>(this.subject.toReversed(), XSort.reverse(this.comparator));
	}

	@Override
	public <C extends Consumer<? super E>> C union(final XGettingCollection<? extends E> other, final Equalator<? super E> equalator, final C target)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}



	///////////////////////////////////////////////////////////////////////////
	// adding methods   //
	/////////////////////

	@Override
	public boolean add(final E e)
	{
		// 1.) get index i by binary searching e
		// 2.) scroll to index of next highest element if necessary
		// 3.) insert element at found index
//		final XList<E> list;
//		final int b;
//		if((b = (list = this.list).binarySearch(e, this.comparator)) < 0)
//		{
//			list.insert(-b - 1, e);
//		}
//		else
//		{
////			list.insert(list.rngIndexOf(b, list.size() - b, e, this.notEqual),	e);
//		}
		return true;
	}

	@Override
	public boolean nullAdd()
	{
//		final XList<E> list;
//		final int b;
//		if((b = (list = this.list).binarySearch(null, this.comparator)) < 0)
//		{
//			list.insert(-b - 1, (E)null);
//		}
//		else
//		{
////			list.insert(list.rngIndexOf(b, list.size() - b, null, this.notEqual), (E)null);
//		}
		return true;
	}

	@SafeVarargs
	@Override
	public final SortedList<E> addAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public SortedList<E> addAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}


	@Override
	public SortedList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}


	@Override
	public void accept(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean put(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@SafeVarargs
	@Override
	public final SortedList<E> putAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public SortedList<E> putAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public SortedList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullPut()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// removing methods  //
	//////////////////////

	@Override
	public long consolidate()
	{
		return this.subject.consolidate();
	}

	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		return this.subject.moveSelection(target, indices);
	}

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.moveTo(target, predicate);
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		return this.subject.process(procedure);
	}

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		return this.subject.removeBy(predicate);
	}

	@Override
	public long remove(final E element)
	{
		return this.subject.remove(element);
	}

	@Override
	public long removeAll(final XGettingCollection<? extends E> samples)
	{
		return this.subject.removeAll(samples);
	}

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		return this.subject.removeDuplicates(equalator);
	}

	@Override
	public long removeDuplicates()
	{
		return this.subject.removeDuplicates();
	}

	@Override
	public E fetch()
	{
		return this.subject.fetch();
	}

	@Override
	public E pop()
	{
		return this.subject.pop();
	}

	@Override
	public E pinch()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public E pick()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		return this.subject.retrieveBy(predicate);
	}

	@Override
	public E retrieve(final E element)
	{
		return this.subject.retrieve(element);
	}

	@Override
	public SortedList<E> removeRange(final long startIndex, final long length)
	{
		this.subject.removeRange(startIndex, length);
		return this;
	}

	@Override
	public SortedList<E> retainRange(final long startIndex, final long length)
	{
		this.subject.retainRange(startIndex, length);
		return this;
	}

	@Override
	public long removeSelection(final long[] indices)
	{
		return this.subject.removeSelection(indices);
	}

//	@Override
//	public int retainAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		return this.subject.retainAll(samples, equalator);
//	}

	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.retainAll(elements);
	}

	@Override
	public long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		// range check is done in Constructor already
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public void truncate()
	{
		this.subject.truncate();
	}

	@Override
	public long nullRemove()
	{
		return this.subject.nullRemove();
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives  //
	////////////////////////////////////

	@Override
	public String toString()
	{
		return this.subject.toString();
	}

	@Override
	public void clear()
	{
		this.subject.clear();
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.subject.iterator();
	}

	@Override
	public long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}
	@Override
	public SortedList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.subject.ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public SortedList<E> ensureCapacity(final long minimalCapacity)
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
		return this.subject.isFull();
	}

	@Override
	public long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public E at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return this.subject.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		return this.subject.listIterator(index);
	}

	@Override
	public E removeAt(final long index)
	{
		return this.subject.removeAt(index);
	}



	@Override
	public OldList<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}



	@Override
	public XGettingList<E> view()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}



	@Override
	public E peek()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}



	@Override
	public E poll()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean removeOne(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public boolean removeOne(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Auto-generated method stub, not implemented yet
//	}

}
