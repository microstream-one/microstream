package one.microstream.collections.old;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.AbstractArrayStorage;
import one.microstream.collections.AbstractSimpleArrayCollection;
import one.microstream.collections.BulkList;
import one.microstream.collections.ConstList;
import one.microstream.collections.ListView;
import one.microstream.collections.SubList;
import one.microstream.collections.SubListView;
import one.microstream.collections.XSort;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.reflect.XReflect;

/**
 * Extended-Collection wrapper implementation for old {@link ArrayList}.
 * <p>
 * Use this wrapper only if a certain, long-living ArrayList instance has to be used as a XList simultaneously to using
 * it as a {@link List}.
 * If the ArrayList's shall be simply be simply converted to a XList, just copy the data to a new {@link BulkList} and
 * forget about the ArrayList instance.
 * <p>
 * {@link XArrayList} is slower than direct {@link XList} implementations like {@link BulkList}.
 *
 * @author Thomas Muenz
 *
 * @param <E>
 */
public class XArrayList<E> extends AbstractSimpleArrayCollection<E> implements XList<E>
{
	/* (12.07.2012 TM)FIXME: complete XArrayList implementation
	 * See all "FIX-ME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	// internal marker object for marking to be removed slots for batch removal and null ambiguity resolution
	private static final Object MARKER = new Object();



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ArrayList<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public XArrayList()
	{
		super();
		this.subject = new ArrayList<>();
	}

	public XArrayList(final int initialCapacity)
	{
		super();
		this.subject = new ArrayList<>(initialCapacity);
	}

	public XArrayList(final ArrayList<E> list)
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public ArrayList<E> getOld()
	{
		return this.subject;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected E[] internalGetStorageArray()
	{
		return (E[])XReflect.accessArray(this.subject);
	}

	@Override
	protected int internalSize()
	{
		return this.subject.size();
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.subject.size()}; // trivial section
	}

	@Override
	public long size()
	{
		return this.subject.size();
	}


	///////////////////////////////////////////////////////////////////////////
	//   add methods    //
	/////////////////////

	@Override
	public void accept(final E element)
	{
		this.subject.add(element);
	}

	@Override
	public boolean add(final E element)
	{
		return this.subject.add(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XArrayList<E> addAll(final E... elements)
	{
		final ArrayList<E> list = this.subject;
		for(int i = 0; i < elements.length; i++)
		{
			list.add(elements[i]);
		}
		return this;
	}

	@Override
	public XArrayList<E> addAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullAdd()
	{
		return this.subject.add(null);
	}



	///////////////////////////////////////////////////////////////////////////
	//   put methods    //
	/////////////////////

	@Override
	public boolean put(final E element)
	{
		return this.subject.add(element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XArrayList<E> putAll(final E... elements)
	{
		final ArrayList<E> list = this.subject;
		for(int i = 0; i < elements.length; i++)
		{
			list.add(elements[i]);
		}
		return this;
	}

	@Override
	public XArrayList<E> putAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullPut()
	{
		return this.subject.add(null);
	}



	///////////////////////////////////////////////////////////////////////////
	// prepend methods //
	////////////////////

	@Override
	public boolean prepend(final E element)
	{
		this.subject.add(0, element);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XArrayList<E> prependAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> prependAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullPrepend()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// preput methods  //
	////////////////////

	@Override
	public boolean preput(final E element)
	{
		this.subject.add(0, element);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public XArrayList<E> preputAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> preputAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullPreput()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	//  insert methods  //
	/////////////////////

	@Override
	public boolean insert(final long index, final E element)
	{
		this.subject.add(X.checkArrayRange(index), element);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public long insertAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullInsert(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	//  input methods   //
	/////////////////////

	@Override
	public boolean input(final long index, final E element)
	{
		this.subject.add(X.checkArrayRange(index), element);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public long inputAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}



	@Override
	public XArrayList<E> copy()
	{
		return new XArrayList<>(new ArrayList<>(this.subject));
	}

	@Override
	public <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		for(final E element : this.subject)
		{
			procedure.accept(element);
		}
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join(this.internalGetStorageArray(), this.subject.size(), joiner, aggregate);
		return aggregate;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		// massively more efficient than Iterator... not that I would care for backward compatability stuff.
		AbstractArrayStorage.process(this.internalGetStorageArray(), this.subject.size(), procedure, (E)MARKER);
		return procedure;
	}

	@Override
	public SubList<E> range(final long fromIndex, final long toIndex)
	{
		return new SubList<>(this, fromIndex, toIndex);
	}

	@Override
	public XArrayList<E> toReversed()
	{
		return this.copy().reverse();
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardContains(this.internalGetStorageArray(), 0, this.subject.size(), predicate);
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardApplies(this.internalGetStorageArray(), 0, this.subject.size(), predicate);
	}

	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame(this.internalGetStorageArray(), 0, this.subject.size(), element);
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll(this.internalGetStorageArray(), this.subject.size(), elements);
	}

	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame(this.internalGetStorageArray(), 0, this.subject.size(), element);
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.forwardCount(this.internalGetStorageArray(), 0, this.subject.size(), element);
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalCount(this.internalGetStorageArray(), 0, this.subject.size(), predicate);
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public boolean hasDistinctValues()
//	{
//		return AbstractArrayStorage.hasDistinctValues(this.internalGetStorageArray(), this.subject.size());
//	}
//
//	@Override
//	public boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.hasDistinctValues(this.internalGetStorageArray(), this.subject.size(), equalator);
//	}

	@Override
	public boolean hasVolatileElements()
	{
		return false; // jul ArrayList does not contain volatile elements
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
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
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max(this.internalGetStorageArray(), this.subject.size(), comparator);
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min(this.internalGetStorageArray(), this.subject.size(), comparator);
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@Override
	public boolean nullContained()
	{
		return this.subject.contains(null);
	}

	@Override
	public OldList<E> old()
	{
		return new OldArrayList<>(this);
	}

	/**
	 * Note that {@link #old()} should be preferred to this method as it maintains the back reference to this.
	 *
	 * @return the wrapped old {@link java.util.ArrayList}.
	 * @see #old()
	 */
	public ArrayList<E> getArrayList()
	{
		return this.subject;
	}

//	@Override
//	public E search(final E sample, final Equalator<? super E> equalator)
//	{
////		return DelegateArrayLogic.search(this.getInternalStorageArray(), this.list.size(), sample, equalator);
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardQueryElement(this.internalGetStorageArray(), 0, this.subject.size(), predicate, null);
	}

	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.forwardContainsSame(this.internalGetStorageArray(), 0, this.subject.size(), sample)
			? sample
			: null
		;
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(X.Array(type, this.subject.size()));
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> ensureCapacity(final long minimalCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long currentCapacity()
	{
		return this.internalGetStorageArray().length;
	}

	@Override
	public long maximumCapacity()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isFull()
	{
		return this.subject.size() >= Integer.MAX_VALUE;
	}

	@Override
	public long remainingCapacity()
	{
		return Integer.MAX_VALUE - this.subject.size();
	}

	@Override
	public long optimize()
	{
		this.subject.trimToSize();
		return this.internalGetStorageArray().length;
	}

	@Override
	public void clear()
	{
		this.subject.clear();
	}

	@Override
	public long consolidate()
	{
		return 0;
	}

//	@Override
//	public XArrayList<E> process(final CtrlProcedure<? super E> procedure)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long nullRemove()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long remove(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int remove(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int removeAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long removeDuplicates()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int retainAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public void truncate()
	{
		this.subject.clear();
		this.subject.trimToSize();
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		AbstractArrayStorage.iterate(this.internalGetStorageArray(), this.subject.size(), procedure);
		return procedure;
	}

	@Override
	public E at(final long index)
	{
		return this.subject.get(X.checkArrayRange(index));
	}

	@Override
	public E get()
	{
		return this.subject.get(0);
	}

	@Override
	public E first()
	{
		return this.subject.get(0);
	}

	@Override
	public E last()
	{
		return this.subject.get(this.subject.size() - 1);
	}

	@Override
	public E poll()
	{
		return this.subject.size() == 0 ? null : (E)this.at(0);
	}

	@Override
	public E peek()
	{
		return this.subject.size() == 0 ? null : (E)this.at(this.subject.size() - 1);
	}

	@Override
	public long indexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int indexOf(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long lastIndexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public XArrayList<E> iterate(final CtrlIndexProcedure<? super E> procedure)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public E removeAt(final long index)
	{
		return this.subject.remove(X.checkArrayRange(index));
	}

	@Override
	public E fetch()
	{
		return this.subject.remove(0);
	}

	@Override
	public E pop()
	{
		return this.subject.remove(this.subject.size() - 1);
	}

	@Override
	public E pinch()
	{
		return this.subject.size() == 0 ? null : this.subject.remove(0);
	}

	@Override
	public E pick()
	{
		return this.subject.size() == 0 ? null : this.subject.remove(this.subject.size() - 1);
	}

	@Override
	public E retrieve(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public boolean removeOne(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public boolean removeOne(final E sample, final Equalator<? super E> equalator)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public XArrayList<E> removeRange(final long startIndex, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> retainRange(final long startIndex, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long removeSelection(final long[] indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> fill(final long offset, final long length, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int replace(final CtrlPredicate<? super E> predicate, final E substitute)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long replace(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int replace(final E sample, final Equalator<? super E> equalator, final E replacement)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int replaceAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator, final E replacement)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

//	@Override
//	public int modify(final CtrlPredicate<? super E> predicate, final Function<E, E> mapper)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
//	}

	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		return AbstractArrayStorage.replaceOne(this.internalGetStorageArray(), this.subject.size(), element, replacement);
	}

//	@Override
//	public boolean replaceOne(final E sample, final Equalator<? super E> equalator, final E replacement)
//	{
//		return AbstractArrayStorage.replaceOne(this.internalGetStorageArray(), this.subject.size(), sample, replacement, equalator);
//	}

	@Override
	public XArrayList<E> reverse()
	{
		AbstractArrayStorage.reverse(this.internalGetStorageArray(), this.subject.size());
		return this;
	}

	@Override
	public boolean set(final long index, final E element)
	{
		this.subject.set(X.checkArrayRange(index), element);
		return false;
	}

	@Override
	public E setGet(final long index, final E element)
	{
		return this.subject.set(X.checkArrayRange(index), element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public XArrayList<E> setAll(final long offset, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> set(final long offset, final XGettingSequence<? extends E> elements, final long elementsOffset, final long elementsLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public void setFirst(final E element)
	{
		this.subject.set(0, element);
	}

	@Override
	public void setLast(final E element)
	{
		this.subject.set(this.subject.size() - 1, element);
	}

	@Override
	public XArrayList<E> sort(final Comparator<? super E> comparator)
	{
		XSort.mergesort(this.internalGetStorageArray(), 0, this.subject.size(), comparator);
		return this;
	}

	@Override
	public SubListView<E> view(final long fromIndex, final long toIndex)
	{
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public long replace(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substitute(this.internalGetStorageArray(), this.subject.size(), predicate, substitute);
	}

	@Override
	public boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		return AbstractArrayStorage.substituteOne(this.internalGetStorageArray(), this.subject.size(), predicate, substitute);
	}

	@Override
	public XArrayList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> shiftBy(final long sourceIndex, final long distance)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	public XArrayList<E> swap(final long indexA, final long indexB)
	{
		AbstractArrayStorage.swap(
			this.internalGetStorageArray(),
			this.subject.size()           ,
			X.checkArrayRange(indexA),
			X.checkArrayRange(indexB)
		);

		return this;
	}

	@Override
	public XArrayList<E> swap(final long indexA, final long indexB, final long length)
	{
		AbstractArrayStorage.swap(
			this.internalGetStorageArray(),
			this.subject.size(),
			X.checkArrayRange(indexA),
			X.checkArrayRange(indexB),
			X.checkArrayRange(length)
		);

		return this;
	}

	@Override
	public ListIterator<E> listIterator()
	{
		return this.subject.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		return this.subject.listIterator(X.checkArrayRange(index));
	}

	@Override
	public XImmutableList<E> immure()
	{
		return ConstList.New(this);
	}



	@Override
	public ListView<E> view()
	{
		return new ListView<>(this);
	}

	/* this class is really crazy stuff:
	 * wrap an old-collection-wrapping new collection in an new-collection-wrapping old collection o_0.
	 * But can't return the old ArrayList directly (which is intentional by design due to x() method)
	 */
	public static final class OldArrayList<E> extends AbstractBridgeXList<E>
	{
		OldArrayList(final XArrayList<E> list)
		{
			super(list);
		}

		@Override
		public XArrayList<E> parent()
		{
			return (XArrayList<E>)super.parent();
		}

	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIX-ME Auto-generated method stub, not implemented yet
	}

}
