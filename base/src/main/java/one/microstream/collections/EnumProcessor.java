
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
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.old.OldCollection;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingEnum;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XProcessingEnum;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;


public final class EnumProcessor<E> implements XProcessingEnum<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final XProcessingEnum<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public EnumProcessor(final XProcessingEnum<E> list)
	{
		super();
		this.subject = list;
	}



	///////////////////////////////////////////////////////////////////////////
	// constant override methods //
	//////////////////////////////

	@Override
	public final XImmutableEnum<E> immure()
	{
		return this.subject.immure();
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
	public final void clear()
	{
		this.subject.clear();
	}

	@Override
	public final boolean contains(final E element)
	{
		return this.subject.contains(element);
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
	public final boolean containsId(final E element)
	{
		return this.subject.containsId(element);
	}

	@Override
	public final EnumProcessor<E> copy()
	{
		this.subject.copy();
		return this;
	}

	@Override
	public final <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return this.subject.copySelection(target, indices);
	}

	@Override
	public final <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.filterTo(target, predicate);
	}

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return target;
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
		return this.subject.equals(samples, equalator);
	}

	@Override
	public final boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		return this.subject.equalsContent(samples, equalator);
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
		this.subject.iterate(procedure);
		return procedure;
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		return this.subject.join(joiner, aggregate);
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		this.subject.iterateIndexed(procedure);
		return procedure;
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
	public final boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public final long indexOf(final E element)
	{
		return this.subject.indexOf(element);
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(predicate);
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
		return this.subject.iterator();
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
	public final XGettingEnum<E> range(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EnumProcessor#range
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
	public final EnumProcessor<E> toReversed()
	{
		this.subject.toReversed();
		return this;
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



	///////////////////////////////////////////////////////////////////////////
	// removing methods //
	/////////////////////

	@Override
	public final long consolidate()
	{
		return this.subject.consolidate();
	}

	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		return this.subject.moveSelection(target, indices);
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		return this.subject.moveTo(target, predicate);
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		return this.subject.process(procedure);
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		return this.subject.removeBy(predicate);
	}

	@Override
	public final long remove(final E element)
	{
		return this.subject.remove(element);
	}

	@Override
	public final E removeAt(final long index)
	{
		return this.subject.removeAt(index);
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.removeAll(elements);
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		return this.subject.removeDuplicates(equalator);
	}

	@Override
	public final long removeDuplicates()
	{
		return this.subject.removeDuplicates();
	}

	@Override
	public final E fetch()
	{
		return this.subject.fetch();
	}

	@Override
	public final E pop()
	{
		return this.subject.pop();
	}

	@Override
	public final E pinch()
	{
		return this.subject.pinch();
	}

	@Override
	public final E pick()
	{
		return this.subject.pick();
	}

	@Override
	public final E retrieve(final E element)
	{
		return this.subject.retrieve(element);
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		return this.subject.retrieveBy(predicate);
	}

	@Override
	public final boolean removeOne(final E element)
	{
		return this.subject.removeOne(element);
	}

	@Override
	public final EnumProcessor<E> removeRange(final long startIndex, final long length)
	{
		this.subject.removeRange(startIndex, length);
		return this;
	}

	@Override
	public final EnumProcessor<E> retainRange(final long startIndex, final long length)
	{
		this.subject.retainRange(startIndex, length);
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		return this.subject.removeSelection(indices);
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		return this.subject.retainAll(elements);
	}

	@Override
	public final long optimize()
	{
		return this.subject.optimize();
	}

	@Override
	public final EnumView<E> view()
	{
		return new EnumView<>(this);
	}

	@Override
	public final XGettingEnum<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME EnumProcessor#view
	}

	@Override
	public final void truncate()
	{
		this.subject.truncate();
	}

	@Override
	public final long nullRemove()
	{
		return this.subject.nullRemove();
	}

	@Override
	public final OldCollection<E> old()
	{
		return this.subject.old();
	}

	@Override
	public final String toString()
	{
		return this.subject.toString();
	}

}
