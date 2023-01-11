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

import one.microstream.collections.old.AbstractOldSettingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;

/**
 * Wrapper class that reduces the services provided by any wrapped {@link XSettingList} to only those of
 * {@link XSettingList}, effectively making the wrapped {@link XSettingList} instance structural unmodifiable
 * if used through an instance of this class.
 * <p>
 * All methods declared in {@link XSettingList} are transparently passed to the wrapped list.<br>
 * All structural modifying methods declared in {@link Collection} and {@link List}
 * (all variations of add~(), remove~() and retain~() as well as clear()) immediately throw an
 * {@link UnsupportedOperationException} when called.
 * <p>
 * This concept can be very useful if a class wants to provide public read and write access to an internal list without
 * either the danger of the list being structurally modified from the outside or the need to copy the whole list on
 * every access.
 * <p>
 * This is one of many useful concepts that are missing in the JDK Collections Framework.
 *
 */
public class ListAccessor<E> implements XSettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XSettingList<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ListAccessor(final XSettingList<E> list)
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Equalator<? super E> equality()
	{
		return this.subject.equality();
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(predicate);
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(predicate);
	}

	@Override
	public final boolean nullAllowed()
	{
		return true;
	}

	@Override
	public final boolean nullContained()
	{
		return this.subject.nullContained();
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.containsAll(elements);
	}

	@Override
	public final boolean contains(final E element)
	{
		return this.subject.contains(element);
	}

	@Override
	public final boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public final ListAccessor<E> copy()
	{
		return new ListAccessor<>(this.subject.copy());
	}

	@Override
	public final <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return this.subject.copyTo(target);
	}

	@Override
	public final long count(final E element)
	{
		return this.subject.count(element);
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(predicate);
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return this.subject.distinct(target, equalator);
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target)
	{
		return this.subject.distinct(target);
	}

	@Deprecated
	@Override
	public final boolean equals(final Object o)
	{
		return this.subject.equals(o);
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equals(this.subject, equalator);
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		return this.subject.equalsContent(this.subject, equalator);
	}

	@Override
	public final <C extends Consumer<? super E>> C except(
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
	public final ListAccessor<E> fill(final long offset, final long length, final E element)
	{
		this.subject.fill(offset, length, element);
		return this;
	}

	@Override
	public final E at(final long index)
	{
		return this.subject.at(index);
	}

	@Override
	public final E get()
	{
		return this.subject.get();
	}

	@Override
	public final E first()
	{
		return this.subject.first();
	}

	@Override
	public final E last()
	{
		return this.subject.last();
	}

	@Override
	public final E poll()
	{
		return this.subject.poll();
	}

	@Override
	public final E peek()
	{
		return this.subject.peek();
	}

	@Deprecated
	@Override
	public final int hashCode()
	{
		return this.subject.hashCode();
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
	}

	@Override
	public final long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public final <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.intersect(other, equalator, target);
	}

	@Override
	public final boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(comparator);
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(predicate);
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		return this.subject.lastIndexOf(element);
	}

	@Override
	public final Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		AbstractExtendedCollection.validateIndex(this.subject.size(), index);
		return new ReadOnlyListIterator<>(this, (int)index);
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		return this.subject.max(comparator);
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(comparator);
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		return this.subject.min(comparator);
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(comparator);
	}

	@Override
	public final long replace(final E element, final E replacement)
	{
		return this.subject.replace(element, replacement);
	}

	@Override
	public final long replace(final Predicate<? super E> predicate, final E substitute)
	{
		return this.subject.replace(predicate, substitute);
	}

	@Override
	public final long substitute(final Function<? super E, ? extends E> mapper)
	{
		return this.subject.substitute(mapper);
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		return this.subject.substitute(predicate, mapper);
	}

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		return this.subject.replaceAll(elements, replacement);
	}

	@Override
	public final boolean replaceOne(final E element, final E replacement)
	{
		return this.subject.replaceOne(element, replacement);
	}

	@Override
	public final boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		return this.subject.replaceOne(predicate, substitute);
	}

	@Override
	public final ListAccessor<E> reverse()
	{
		this.subject.reverse();
		return this;
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(predicate);
	}

	@Override
	public final E seek(final E sample)
	{
		return this.subject.seek(sample);
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		return this.subject.search(predicate);
	}

	@SafeVarargs
	@Override
	public final ListAccessor<E> setAll(final long offset, final E... elements)
	{
		this.subject.setAll(offset, elements);
		return this;
	}

	@Override
	public final boolean set(final long index, final E element)
	{
		return this.subject.set(index, element);
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		return this.subject.setGet(index, element);
	}

	@Override
	public final ListAccessor<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		this.subject.set(offset, src, srcIndex, srcLength);
		return this;
	}

	@Override
	public final ListAccessor<E> set(
		final long                           offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                           elementsOffset,
		final long                           elementsLength
	)
	{
		this.subject.set(offset, elements, elementsOffset, elementsLength);
		return this;
	}

	@Override
	public final void setFirst(final E element)
	{
		this.subject.setFirst(element);
	}

	@Override
	public final void setLast(final E element)
	{
		this.subject.setLast(element);
	}

	@Override
	public final long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public final long maximumCapacity()
	{
		return this.subject.maximumCapacity();
	}

	@Override
	public final boolean isFull()
	{
		return this.subject.isFull();
	}

	@Override
	public final long remainingCapacity()
	{
		return this.subject.remainingCapacity();
	}

	@Override
	public final ListAccessor<E> sort(final Comparator<? super E> comparator)
	{
		this.subject.sort(comparator);
		return this;
	}

	@Override
	public final XSettingList<E> range(final long fromIndex, final long toIndex)
	{
		return this.subject.range(fromIndex, toIndex);
	}

	@Override
	public final ListView<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public final SubListView<E> view(final long fromIndex, final long toIndex)
	{
		// range check is done in Constructor already
		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public final ListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		this.subject.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final ListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		this.subject.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final ListAccessor<E> shiftBy(final long sourceIndex, final long distance)
	{
		this.subject.shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public final ListAccessor<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		this.subject.shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public final ListAccessor<E> swap(final long indexA, final long indexB, final long length)
	{
		this.subject.swap(indexA, indexB, length);
		return this;
	}

	@Override
	public final ListAccessor<E> swap(final long indexA, final long indexB)
	{
		this.subject.swap(indexA, indexB);
		return this;
	}

	@Override
	public final Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public final E[] toArray(final Class<E> type)
	{
		return this.subject.toArray(type);
	}

	@Override
	public final ListAccessor<E> toReversed()
	{
		return new ListAccessor<>(this.subject.toReversed());
	}

	@Override
	public final <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final C                               target
	)
	{
		return this.subject.union(other, equalator, target);
	}

	@Override
	public final <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Override
	public final XImmutableList<E> immure()
	{
		return ConstList.New(this.subject);
	}



	@Override
	public final OldListAccessor<E> old()
	{
		return new OldListAccessor<>(this);
	}

	public static final class OldListAccessor<E> extends AbstractOldSettingList<E>
	{
		OldListAccessor(final ListAccessor<E> list)
		{
			super(list);
		}

		@Override
		public final ListAccessor<E> parent()
		{
			return (ListAccessor<E>)super.parent();
		}

	}

}
