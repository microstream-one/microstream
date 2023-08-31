package one.microstream.collections;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XEnum;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XReference;
import one.microstream.collections.types.XReferencing;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;

// (02.11.2012 TM)FIXME: Single: consolidate with Singleton<E>
public class Single<E> implements XList<E>, XEnum<E>, XReference<E>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static void validateIndex(final long index)
	{
		if(index != 0)
		{
			throw new IndexOutOfBoundsException();
		}
	}

	private static void validateElementsLength(final int length)
	{
		if(length != 1)
		{
			throw new IndexOutOfBoundsException();
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	E element;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Single()
	{
		super();
		this.element = null;
	}

	public Single(final E element)
	{
		super();
		this.element = element;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void set(final E element)
	{
		this.element = element;
	}

	@Override
	public void accept(final E element)
	{
		this.internalPut(element);
	}

	private boolean internalAdd(final E element)
	{
		if(this.element != null)
		{
			return false;
		}
		this.element = element;
		return true;
	}

	private boolean internalPut(final E element)
	{
		if(this.element != null)
		{
			this.element = element;
			return false;
		}
		this.element = element;
		return true;
	}

	@Override
	public boolean add(final E element)
	{
		if(this.element != null)
		{
			return false;
		}
		this.element = element;
		return true;
	}

	@Override
	public boolean nullAdd()
	{
		throw new NullPointerException();
	}

	@Override
	public boolean nullAllowed()
	{
		return false;
	}

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public Single<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		if(minimalFreeCapacity == 0 || this.element == null && minimalFreeCapacity == 1)
		{
			return this;
		}
		throw new InvalidCapacityException(1, minimalFreeCapacity);
	}

	@Override
	public Single<E> ensureCapacity(final long minimalCapacity)
	{
		if(minimalCapacity == 1)
		{
			return this;
		}
		throw new InvalidCapacityException(1, minimalCapacity);
	}

	@Override
	public long currentCapacity()
	{
		return 1;
	}

	@Override
	public long maximumCapacity()
	{
		return 1;
	}

	@Override
	public long remainingCapacity()
	{
		return this.element == null ? 1 : 0;
	}

	@Override
	public boolean isFull()
	{
		return this.element != null;
	}

	@Override
	public long size()
	{
		return this.element == null ? 0 : 1;
	}

	@Override
	public boolean isEmpty()
	{
		return this.element == null;
	}

	@Override
	public long optimize()
	{
		return 1;
	}

	@Override
	public boolean put(final E element)
	{
		return this.internalPut(element);
	}

	@Override
	public boolean nullPut()
	{
		throw new NullPointerException();
	}

	@Override
	public XReferencing<E> view()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingBag<E>#view()
	}

	@Override
	public XReferencing<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#view()
	}

	@Override
	public Constant<E> immure()
	{
		return new Constant<>(this.element);
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#iterator()
	}

	@Override
	public Object[] toArray()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#toArray()
	}

	@Override
	public OldSingle old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#old()
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equality()
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#toArray()
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equals()
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equalsContent()
	}

	@Override
	public boolean nullContained()
	{
		return false;
	}

//	@Override
//	public <R> R aggregate(final Aggregator<? super E, R> aggregate)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#aggregate()
//	}

	@Override
	public boolean containsId(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#containsId()
	}

	@Override
	public boolean contains(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#contains()
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#contains()
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#containsAll()
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#applies()
	}

	@Override
	public long count(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#count()
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#count()
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#search()
	}

	@Override
	public E seek(final E sample)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#seek()
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#max()
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#min()
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#distinct()
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#distinct()
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#copyTo()
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#copyTo()
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#union()
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#intersect()
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#except()
	}

	@Override
	public void clear()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#clear()
	}

	@Override
	public void truncate()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#truncate()
	}

	@Override
	public long consolidate()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#consolidate()
	}

	@Override
	public long nullRemove()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#nullRemove()
	}

	@Override
	public boolean removeOne(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeOne()
	}

	@Override
	public long remove(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#remove()
	}

	@Override
	public long removeAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeAll()
	}

	@Override
	public long retainAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#retainAll()
	}

	@Override
	public long removeDuplicates()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeDuplicates()
	}

	@Override
	public E retrieve(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#retrieve()
	}

	@Override
	public E retrieveBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#retrieve()
	}

	@Override
	public long removeDuplicates(final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#removeDuplicates()
	}

	@Override
	public long removeBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#remove()
	}

	@Override
	public <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#moveTo()
	}

	@Override
	public E at(final long index)
	{
		validateIndex(index);
		return this.element;
	}

	@Override
	public E get()
	{
		if(this.element == null)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.element;
	}

	@Override
	public E first()
	{
		if(this.element == null)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.element;
	}

	@Override
	public E last()
	{
		if(this.element == null)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.element;
	}

	@Override
	public E poll()
	{
		return this.element;
	}

	@Override
	public E peek()
	{
		return this.element;
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#maxIndex()
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#minIndex()
	}

	@Override
	public long indexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#indexOf()
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#indexOf()
	}

	@Override
	public long lastIndexOf(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#scan()
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#isSorted()
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#copySelection()
	}

	@Override
	public E removeAt(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#remove()
	}

	@Override
	public E fetch()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#fetch()
	}

	@Override
	public E pop()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#pop()
	}

	@Override
	public E pinch()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#pinch()
	}

	@Override
	public E pick()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#pick()
	}

	@Override
	public Single<E> removeRange(final long offset, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#removeRange()
	}

	@Override
	public Single<E> retainRange(final long offset, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#removeRange()
	}

	@Override
	public long removeSelection(final long[] indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#removeSelection()
	}

	@Override
	public <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#moveSelection()
	}

	@Override
	public ListIterator<E> listIterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator()
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator()
	}

	@Override
	public boolean input(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#input()
	}

	@Override
	public boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#nullInput()
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#input()
	}

	@Override
	public long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#inputAll()
	}

	@Override
	public long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#inputAll()
	}

	@Override
	public boolean insert(final long index, final E element)
	{
		validateIndex(index);
		return this.internalAdd(element);
	}

	@Override
	public boolean nullInsert(final long index)
	{
		throw new NullPointerException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public long insertAll(final long index, final E... elements)
	{
		validateIndex(index);
		validateElementsLength(elements.length);
		return this.internalAdd(elements[0]) ? 1 : 0;
	}

	@Override
	public long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		validateIndex(index);
		return this.internalAdd(elements[offset]) ? 1 : 0;
	}

	@Override
	public long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		validateIndex(index);
		if(this.element != null)
		{
			return 0;
		}
		return this.internalAdd(elements.get()) ? 1 : 0;
	}

	@Override
	public boolean prepend(final E element)
	{
		return this.internalAdd(element);
	}

	@Override
	public boolean nullPrepend()
	{
		throw new NullPointerException();
	}

	@Override
	public boolean preput(final E element)
	{
		return this.internalPut(element);
	}

	@Override
	public boolean nullPreput()
	{
		throw new NullPointerException();
	}

	@Override
	public boolean replaceOne(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replaceOne()
	}

	@Override
	public long replace(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replace()
	}

	@Override
	public long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replaceAll()
	}

	@Override
	public boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replaceOne()
	}

	@Override
	public long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replace()
	}

//	@Override
//	public int replace(final CtrlPredicate<? super E> predicate, final E substitute)
//	{
//		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#replace()
//	}

	@Override
	public long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#modify()
	}

	@Override
	public long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XReplacingCollection<E>#modify()
	}

	@Override
	public boolean set(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#set()
	}

	@Override
	public E setGet(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#setGet()
	}

	@Override
	public void setFirst(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#setFirst()
	}

	@Override
	public void setLast(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#setLast()
	}

	@Override
	public E addGet(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Single#addGet()
	}

	@Override
	public E deduplicate(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Single#substitute()
	}

	@Override
	public E putGet(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XPutGetSet<E>#putGet()
	}

	@Override
	public E replace(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME Single#replace()
	}

	@SafeVarargs
	@Override
	public final Single<E> addAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#add()
	}

	@Override
	public Single<E> addAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#addAll()
	}

	@Override
	public Single<E> addAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#addAll()
	}

	@SafeVarargs
	@Override
	public final Single<E> putAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#put()
	}

	@Override
	public Single<E> putAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#putAll()
	}

	@Override
	public Single<E> putAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#putAll()
	}

	@SafeVarargs
	@Override
	public final Single<E> prependAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#prepend()
	}

	@Override
	public Single<E> prependAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#prependAll()
	}

	@Override
	public Single<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#prependAll()
	}

	@SafeVarargs
	@Override
	public final Single<E> preputAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#preput()
	}

	@Override
	public Single<E> preputAll(final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#preputAll()
	}

	@Override
	public Single<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#preputAll()
	}

	@SafeVarargs
	@Override
	public final Single<E> setAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#set()
	}

	@Override
	public Single<E> set(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#set()
	}

	@Override
	public Single<E> set(
		final long                           index   ,
		final XGettingSequence<? extends E> elements,
		final long                           offset  ,
		final long                           length
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#set()
	}

	@Override
	public Single<E> swap(final long indexA, final long indexB)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#swap()
	}

	@Override
	public Single<E> swap(final long indexA, final long indexB, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#swap()
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		procedure.accept(this.element);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		joiner.accept(this.element, aggregate);
		return aggregate;
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#iterate()
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#process()
	}

	@Override
	public Single<E> copy()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#copy()
	}

	@Override
	public Single<E> toReversed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#toReversed()
	}

	@Override
	public Single<E> reverse()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#reverse()
	}

	@Override
	public Single<E> range(final long fromIndex, final long toIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#range()
	}

	@Override
	public Single<E> fill(final long offset, final long length, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#fill()
	}

	@Override
	public Single<E> sort(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#sort()
	}

	@Override
	public Single<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#shiftTo()
	}

	@Override
	public Single<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#shiftTo()
	}

	@Override
	public Single<E> shiftBy(final long sourceIndex, final long distance)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#shiftBy()
	}

	@Override
	public Single<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XList<E>#shiftBy()
	}


	public final class OldSingle implements one.microstream.collections.old.OldSingle<E>
	{
		@Override
		public int size()
		{
			return XTypes.to_int(Single.this.size());
		}

		@Override
		public boolean isEmpty()
		{
			return Single.this.isEmpty();
		}

		@Override
		public boolean contains(final Object o)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#contains()
		}

		@Override
		public Iterator<E> iterator()
		{
			return Single.this.iterator();
		}

		@Override
		public Object[] toArray()
		{
			return new Object[]{Single.this.element};
		}

		@Override
		public <T> T[] toArray(final T[] a)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#toArray()
		}

		@Override
		public boolean add(final E e)
		{
			return Single.this.add(e);
		}

		@Override
		public boolean remove(final Object o)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#remove()
		}

		@Override
		public boolean containsAll(final Collection<?> c)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#containsAll()
		}

		@Override
		public boolean addAll(final Collection<? extends E> c)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#addAll()
		}

		@Override
		public boolean addAll(final int index, final Collection<? extends E> c)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#addAll()
		}

		@Override
		public boolean removeAll(final Collection<?> c)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#removeAll()
		}

		@Override
		public boolean retainAll(final Collection<?> c)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#retainAll()
		}

		@Override
		public void clear()
		{
			Single.this.clear();
		}

		@Override
		public E get(final int index)
		{
			return Single.this.at(index);
		}

		@Override
		public E set(final int index, final E element)
		{
			return Single.this.setGet(index, element);
		}

		@Override
		public void add(final int index, final E element)
		{
			Single.this.insert(index, element);
		}

		@Override
		public E remove(final int index)
		{
			return Single.this.removeAt(index);
		}

		@Override
		public int indexOf(final Object o)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#indexOf()
		}

		@Override
		public int lastIndexOf(final Object o)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#lastIndexOf()
		}

		@Override
		public ListIterator<E> listIterator()
		{
			return Single.this.listIterator();
		}

		@Override
		public ListIterator<E> listIterator(final int index)
		{
			return Single.this.listIterator(index);
		}

		@Override
		public List<E> subList(final int fromIndex, final int toIndex)
		{
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME OldSingle<E>#subList()
		}

		@Override
		public Single<E> parent()
		{
			return Single.this;
		}

	}

}
