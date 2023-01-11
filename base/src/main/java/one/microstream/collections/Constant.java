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
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.old.OldList;
import one.microstream.collections.old.OldSet;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.collections.types.XImmutableList;
import one.microstream.collections.types.XReferencing;
import one.microstream.equality.Equalator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.TrivialIterator;


/**
 * Immutable singleton dummy collection used to pass a single instance masked as a collection.
 * <p>
 * As there is always only one element, this type can be a List and a Set (Enum) at the same time, enabling it
 * to be used in any type situation.
 *
 * @param <E> the type of elements in this collection
 * @see Singleton
 */
public class Constant<E> implements XImmutableList<E>, XImmutableEnum<E>, XReferencing<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final E element;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public Constant(final E element)
	{
		super();
		this.element = element;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	/**
	 * Convenience alias for {@link #first()}.
	 *
	 * @return the contained element.
	 */
	@Override
	public final E get()
	{
		return this.element;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final Constant<E> copy()
	{
		return new Constant<>(this.element);
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
		procedure.accept(this.element, 0);
		return procedure;
	}

	@Override
	public final Constant<E> immure()
	{
		return this;
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		return new TrivialIterator<>(this);
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		if(index != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return new TrivialIterator<>(this);
	}

	@Override
	public final OldConstant old()
	{
		return new OldConstant();
	}

	@Override
	public final Constant<E> range(final long fromIndex, final long toIndex)
	{
		if(fromIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(toIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return new Constant<>(this.element);
	}

	@Override
	public final Constant<E> toReversed()
	{
		return new Constant<>(this.element);
	}

	@Override
	public final <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		for(int i = 0; i < indices.length; i++)
		{
			if(indices[i] != 0)
			{
				throw new IndexOutOfBoundsException();
			}
		}
		target.accept(this.element);
		return target;
	}

	@Override
	public final E first()
	{
		return this.element;
	}

	@Override
	public final E at(final long index)
	{
		if(index != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return this.element;
	}

	@Override
	public final long indexOf(final E element)
	{
		if(element == this.element)
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		return true; // hehe
	}

	@Override
	public final E last()
	{
		return this.element;
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		if(element == this.element)
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		return 0;
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		return 0;
	}

	@Override
	public final E peek()
	{
		return this.element;
	}

	@Override
	public final E poll()
	{
		return this.element;
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return 0;
		}
		return -1;
	}

	@Override
	public final Constant<E> view()
	{
		return this;
	}

	@Override
	public final Constant<E> view(final long lowIndex, final long highIndex)
	{
		if(lowIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		if(highIndex != 0)
		{
			throw new IndexOutOfBoundsException();
		}
		return this;
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element);
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element);
	}

	@Override
	public final boolean contains(final E element)
	{
		return this.element == element;
	}

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		final E element = this.element;
		return elements.applies(e ->
			e == element
		);
	}

	@Override
	public final boolean containsId(final E element)
	{
		return this.element == element;
	}

	@Override
	public final <T extends Consumer<? super E>> T copyTo(final T target)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			target.accept(this.element);
		}
		return target;
	}

	@Override
	public final long count(final E element)
	{
		return this.element == element ? 1 : 0;
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		return predicate.test(this.element) ? 1 : 0;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		target.accept(this.element);
		return target;
	}

	@Override
	public final Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples instanceof Constant)
		{
			return equalator.equal(this.element, ((Constant<? extends E>)samples).element);
		}
		return false;
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends E> samples  ,
		final Equalator<? super E>            equalator
	)
	{
		return XTypes.to_int(samples.size()) == 1 && equalator.equal(this.element, samples.get());
	}

	@Override
	public final <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME  not implemented yet
	}

	@Override
	public final boolean isEmpty()
	{
		return false;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new TrivialIterator<>(this);
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		return this.element;
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		return this.element;
	}

	@Override
	public final boolean nullContained()
	{
		return this.element == null;
	}

	@Override
	public final E seek(final E sample)
	{
		return this.element == sample ? sample : null;
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		if(predicate.test(this.element))
		{
			return this.element;
		}
		return null;
	}

	@Override
	public final long size()
	{
		return 1;
	}

	@Override
	public Object[] toArray()
	{
		return new Object[]{this.element};
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = X.Array(type, 1);
		array[0] = this.element;
		return array;
	}

	@Override
	public final <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullAllowed()
	{
		return false;
	}

	@Override
	public final long remainingCapacity()
	{
		return 0;
	}

	@Override
	public final boolean isFull()
	{
		return true;
	}

	@Override
	public final long maximumCapacity()
	{
		return 1;
	}



	public final class OldConstant implements OldList<E>, OldSet<E>
	{
		@Override
		public final Constant<E> parent()
		{
			return Constant.this;
		}

		@Override
		public final boolean add(final E e)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void add(final int index, final E element)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean addAll(final Collection<? extends E> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean addAll(final int index, final Collection<? extends E> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final void clear()
		{
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final boolean contains(final Object o)
		{
			return Constant.this.contains((E)o); // safe because of referential comparison
		}

		@Override
		public final boolean containsAll(final Collection<?> c)
		{
			for(final Object o : c)
			{
				if(o != Constant.this.element)
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public final E get(final int index)
		{
			return Constant.this.at(index);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final int indexOf(final Object o)
		{
			return X.checkArrayRange(Constant.this.indexOf((E)o)); // safe because of referential comparison
		}

		@Override
		public final boolean isEmpty()
		{
			return Constant.this.isEmpty();
		}

		@Override
		public final Iterator<E> iterator()
		{
			return new TrivialIterator<>(Constant.this);
		}

		@SuppressWarnings("unchecked")
		@Override
		public final int lastIndexOf(final Object o)
		{
			return X.checkArrayRange(Constant.this.lastIndexOf((E)o)); // safe because of referential comparison
		}

		@Override
		public final ListIterator<E> listIterator()
		{
			return new TrivialIterator<>(Constant.this);
		}

		@Override
		public final ListIterator<E> listIterator(final int index)
		{
			if(index != 0)
			{
				throw new IndexOutOfBoundsException();
			}
			return new TrivialIterator<>(Constant.this);
		}

		@Override
		public final boolean remove(final Object o)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final E remove(final int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean removeAll(final Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final boolean retainAll(final Collection<?> c)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final E set(final int index, final E element)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public final int size()
		{
			return XTypes.to_int(Constant.this.size());
		}

		@Override
		public final List<E> subList(final int fromIndex, final int toIndex)
		{
			if(fromIndex != 0 || toIndex != 0)
			{
				// admitted quick&dirty checks for a rather academic use case anyway
				throw new IllegalArgumentException();
			}
			return this;
		}

		@Override
		public final Object[] toArray()
		{
			return Constant.this.toArray();
		}

		@SuppressWarnings("unchecked")
		@Override
		public final <T> T[] toArray(final T[] a)
		{
			a[0] = (T)Constant.this.element;
			if(a.length > 1)
			{
				a[1] = null;
			}
			return a;
		}

		@Override
		public final Spliterator<E> spliterator()
		{
			return OldList.super.spliterator();
		}

	}

}
