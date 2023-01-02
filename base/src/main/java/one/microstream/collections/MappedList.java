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

import static one.microstream.X.notNull;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.old.OldList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingList;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XList;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;

public class MappedList<E, S> implements XGettingList<E>
{
	/* (12.07.2012 TM)FIXME: complete MappedList implementation
	 * See all "FIXME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingList<S> subject;
	final Function<S, E> mapper;
	final Equalator<? super E> equality;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public MappedList(final XGettingList<S> subject, final Function<S, E> mapper, final Equalator<? super E> equality)
	{
		super();
		this.subject  = notNull(subject);
		this.mapper   = notNull(mapper);
		this.equality = notNull(equality);
	}

	public MappedList(final XList<S> subject, final Function<S, E> mapper)
	{
		this(subject, mapper, Equalator.identity());
	}




	private Comparator<S> mapComparator(final Comparator<? super E> comparator)
	{
		return new Comparator<S>()
		{
			@Override
			public int compare(final S o1, final S o2)
			{
				return comparator.compare(MappedList.this.mapper.apply(o1), MappedList.this.mapper.apply(o2));
			}
		};
	}

	private Predicate<S> mapPredicate(final Predicate<? super E> predicate)
	{
		return new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return predicate.test(MappedList.this.mapper.apply(e));
			}
		};
	}

	private Predicate<S> mapIsEqual(final E element)
	{
		return new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return MappedList.this.equality.equal(element, MappedList.this.mapper.apply(e));
			}
		};
	}




	@Override
	public E at(final long index)
	{
		return this.mapper.apply(this.subject.at(index));
	}

	@Override
	public E get()
	{
		return this.mapper.apply(this.subject.get());
	}

	@Override
	public E first()
	{
		return this.mapper.apply(this.subject.first());
	}

	@Override
	public E last()
	{
		return this.mapper.apply(this.subject.last());
	}

	@Override
	public E poll()
	{
		return this.mapper.apply(this.subject.poll());
	}

	@Override
	public E peek()
	{
		return this.mapper.apply(this.subject.peek());
	}

	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return this.subject.maxIndex(this.mapComparator(comparator));
	}

	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return this.subject.minIndex(this.mapComparator(comparator));
	}

	@Override
	public long indexOf(final E element)
	{
		return this.subject.indexBy(this.mapIsEqual(element));
	}

	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return this.subject.indexBy(this.mapPredicate(predicate));
	}

	@Override
	public long lastIndexOf(final E element)
	{
		return this.subject.lastIndexBy(this.mapIsEqual(element));
	}

	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return this.subject.lastIndexBy(this.mapPredicate(predicate));
	}

	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return this.subject.scan(this.mapPredicate(predicate));
	}

	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return this.subject.isSorted(this.mapComparator(comparator));
	}

	@Override
	public <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		final int length = indices.length;
		final int size = XTypes.to_int(this.subject.size());

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexExceededException(size, indices[i]);
			}
		}

		// actual copying
		final XGettingList<S> subject = this.subject;
		final Function<S, E>  mapper  = this.mapper;
		for(int i = 0; i < length; i++)
		{
			// (19.11.2011)NOTE: single element access can get pretty inefficient.
			target.accept(mapper.apply(subject.at(indices[i])));
		}

		return target;
	}

	@Override
	public Iterator<E> iterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#iterator
	}

	@Override
	public boolean hasVolatileElements()
	{
		return this.subject.hasVolatileElements();
	}

	@Override
	public long size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Equalator<? super E> equality()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equality
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#toArray
	}

	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equals
	}

	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#equalsContent
	}

	@Override
	public boolean nullContained()
	{
		// (19.11.2011)NOTE: would actually have to iterative over every element and see if function returns null
		return this.subject.nullContained();
	}

	@Override
	public boolean containsId(final E element)
	{
		return this.subject.containsSearched(new Predicate<S>()
		{
			@Override
			public boolean test(final S e)
			{
				return MappedList.this.mapper.apply(e) == element;
			}
		});
	}

	@Override
	public boolean contains(final E element)
	{
		return this.subject.containsSearched(this.mapIsEqual(element));
	}

	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return this.subject.containsSearched(this.mapPredicate(predicate));
	}

	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#containsAll
	}

	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return this.subject.applies(this.mapPredicate(predicate));
	}

	@Override
	public long count(final E element)
	{
		return this.subject.countBy(this.mapIsEqual(element));
	}

	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return this.subject.countBy(this.mapPredicate(predicate));
	}

	@Override
	public E search(final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#search
	}

	@Override
	public E seek(final E sample)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#seek
	}

	@Override
	public E max(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#max
	}

	@Override
	public E min(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#min
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#distinct
	}

	@Override
	public <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#distinct
	}

	@Override
	public <T extends Consumer<? super E>> T copyTo(final T target)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#copyTo
	}

	@Override
	public <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#copyTo
	}

	@Override
	public <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#union
	}

	@Override
	public <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#intersect
	}

	@Override
	public <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#except
	}

	@Override
	public boolean nullAllowed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ExtendedCollection<E>#nullAllowed
	}

	@Override
	public long maximumCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME CapacityCarrying#maximumCapacity
	}

	@Override
	public long remainingCapacity()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME CapacityCarrying#freeCapacity
	}

	@Override
	public boolean isFull()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME CapacityCarrying#isFull
	}

	@Override
	public XImmutableList<E> immure()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#immure
	}

	@Override
	public ListIterator<E> listIterator()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator
	}

	@Override
	public ListIterator<E> listIterator(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator
	}

	@Override
	public OldList<E> old()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#old
	}

	@Override
	public XGettingList<E> copy()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#copy
	}

	@Override
	public XGettingList<E> toReversed()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#toReversed
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#iterate
	}

	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#join
	}

	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#iterate
	}

	@Override
	public XGettingList<E> view()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#view
	}

	@Override
	public XGettingList<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#view
	}

	@Override
	public XGettingList<E> range(final long fromIndex, final long toIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME XGettingList<E>#range
	}

}
