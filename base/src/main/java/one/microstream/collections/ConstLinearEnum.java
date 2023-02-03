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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.old.AbstractOldGettingSet;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XImmutableEnum;
import one.microstream.equality.Equalator;
import one.microstream.equality.IdentityEqualityLogic;
import one.microstream.exceptions.IndexBoundsException;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.typing.XTypes;
import one.microstream.util.iterables.ReadOnlyListIterator;


/**
 *
 * 
 * @version 0.91, 2011-02-28
 */
public final class ConstLinearEnum<E> extends AbstractSimpleArrayCollection<E>
implements XImmutableEnum<E>, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final Object[] EMPTY_DATA = new Object[0];



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final Object[] data; // the storage array containing the elements



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ConstLinearEnum() // required for "empty" list constant
	{
		super();
		this.data = EMPTY_DATA;
	}

	public ConstLinearEnum(final int initialCapacity)
	{
		super();
		this.data = new Object[initialCapacity];
	}

	public ConstLinearEnum(final ConstLinearEnum<? extends E> original) throws NullPointerException
	{
		super();
		this.data = original.data.clone();
	}

	public ConstLinearEnum(final XGettingCollection<? extends E> elements) throws NullPointerException
	{
		super();
		this.data = elements.toArray();
	}

	@SafeVarargs
	public ConstLinearEnum(final E... elements) throws NullPointerException
	{
		super();
		this.data = elements.clone();
	}

	public ConstLinearEnum(final E[] src, final int srcStart, final int srcLength)
	{
		super();
		// automatically check arguments 8-)
		System.arraycopy(src, srcStart, this.data = new Object[srcLength], 0, srcLength);
	}

	ConstLinearEnum(final Object[] internalData, final int size)
	{
		super();
		this.data = internalData;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	@Override
	protected E[] internalGetStorageArray()
	{
		return (E[])this.data;
	}

	@Override
	protected int internalSize()
	{
		return this.data.length;
	}

	@Override
	protected int[] internalGetSectionIndices()
	{
		return new int[]{0, this.data.length}; // trivial section
	}

	@Override
	public Equalator<? super E> equality()
	{
		return Equalator.identity();
	}

	@Override
	protected int internalCountingAddAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingAddAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final E[] elements, final int offset, final int length)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}

	@Override
	protected int internalCountingPutAll(final XGettingCollection<? extends E> elements)
		throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(); // not supported
	}



	///////////////////////////////////////////////////////////////////////////
	// getting methods //
	////////////////////

	@Override
	public ConstLinearEnum<E> copy()
	{
		return new ConstLinearEnum<>(this);
	}

	@Override
	public ConstLinearEnum<E> immure()
	{
		return this;
	}

	@Override
	public ConstLinearEnum<E> toReversed()
	{
		final Object[] data = this.data;
		final Object[] rData = new Object[data.length];
		for(int i = data.length, r = 0; i-- > 0;)
		{
			rData[r++] = data[i];
		}
		return new ConstLinearEnum<>(rData, data.length);
	}

	@Override
	public E[] toArray(final Class<E> type)
	{
		final E[] array = X.Array(type, this.data.length);
		System.arraycopy(this.data, 0, array, 0, this.data.length);
		return array;
	}

	// executing //

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <P extends IndexedAcceptor<? super E>> P iterateIndexed(final P procedure)
	{
		AbstractArrayStorage.iterate((E[])this.data, this.data.length, procedure);
		return procedure;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		AbstractArrayStorage.join((E[])this.data, this.data.length, joiner, aggregate);
		return aggregate;
	}

	// count querying //

	@SuppressWarnings("unchecked")
	@Override
	public long count(final E element)
	{
		return AbstractArrayStorage.forwardCount((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long countBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalCount((E[])this.data, 0, this.data.length, predicate);
	}

	// index querying //

	@SuppressWarnings("unchecked")
	@Override
	public long indexOf(final E element)
	{
		return AbstractArrayStorage.forwardIndexOf((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long indexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardConditionalIndexOf((E[])this.data, 0, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexOf(final E element)
	{
		return AbstractArrayStorage.rangedIndexOF((E[])this.data, this.data.length, this.data.length - 1, -this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long lastIndexBy(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.lastIndexOf((E[])this.data, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long maxIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.maxIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long minIndex(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.minIndex((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public long scan(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardScan((E[])this.data, 0, this.data.length, predicate);
	}

	// element querying //

	@SuppressWarnings("unchecked")
	@Override
	public E get()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E first()
	{
		return (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E last()
	{
		return (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E poll()
	{
		return this.data.length == 0 ? null : (E)this.data[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E peek()
	{
		return this.data.length == 0 ? null : (E)this.data[this.data.length - 1];
	}

	@SuppressWarnings("unchecked")
	@Override
	public E seek(final E sample)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, sample) ? sample : null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public E search(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardQueryElement((E[])this.data, 0, this.data.length, predicate, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E max(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.max((E[])this.data, this.data.length, comparator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E min(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.min((E[])this.data, this.data.length, comparator);
	}

	// boolean querying //

	@Override
	public boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public boolean nullAllowed()
	{
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isSorted(final Comparator<? super E> comparator)
	{
		return AbstractArrayStorage.isSorted((E[])this.data, this.data.length, comparator);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean hasDistinctValues()
//	{
//		return AbstractArrayStorage.hasDistinctValues((E[])this.data, this.data.length);
//	}
//
//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean hasDistinctValues(final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.hasDistinctValues((E[])this.data, this.data.length, equalator);
//	}

	// boolean querying - applies //

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsSearched(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardContains((E[])this.data, 0, this.data.length, predicate);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean applies(final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardApplies((E[])this.data, 0, this.data.length, predicate);
	}

	// boolean querying - contains //

	@SuppressWarnings("unchecked")
	@Override
	public boolean nullContained()
	{
		return AbstractArrayStorage.forwardNullContained((E[])this.data, 0, this.data.length);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsId(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final E element)
	{
		return AbstractArrayStorage.forwardContainsSame((E[])this.data, 0, this.data.length, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return AbstractArrayStorage.containsAll((E[])this.data, this.data.length, elements);
	}

//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean containsAll(final XGettingCollection<? extends E> elements, final Equalator<? super E> equalator)
//	{
//		return AbstractArrayStorage.containsAll((E[])this.data, this.data.length, elements, equalator);
//	}

	// boolean querying - equality //

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == this)
		{
			return true;
		}
		if(samples == null || !(samples instanceof ConstLinearEnum<?>) || XTypes.to_int(samples.size()) != this.data.length)
		{
			return false;
		}

		// equivalent to equalsContent()
		return XArrays.equals(this.data, 0, ((ConstLinearEnum<?>)samples).data, 0, this.data.length, (Equalator<Object>)equalator);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || XTypes.to_int(samples.size()) != this.data.length)
		{
			return false;
		}
		if(samples == this)
		{
			return true;
		}
		return AbstractArrayStorage.equalsContent((E[])this.data, this.data.length, samples, equalator);
	}

	// data set procedures //

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.intersect((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.except((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		return AbstractArrayStorage.union((E[])this.data, this.data.length, samples, equalator, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copyTo(final C target)
	{
		return AbstractArrayStorage.forwardCopyTo((E[])this.data, 0, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C filterTo(final C target, final Predicate<? super E> predicate)
	{
		return AbstractArrayStorage.forwardCopyTo((E[])this.data, 0, this.data.length, target, predicate);
	}

	@SuppressWarnings("unchecked")
	public <T> T[] rngCopyTo(final int startIndex, final int length, final T[] target, final int offset)
	{
		return AbstractArrayStorage.rangedCopyTo((E[])this.data, this.data.length, startIndex, length,  target, offset);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		return AbstractArrayStorage.distinct((E[])this.data, this.data.length, target, equalator);
	}


	@SuppressWarnings("unchecked")
	@Override
	public <C extends Consumer<? super E>> C copySelection(final C target, final long... indices)
	{
		return AbstractArrayStorage.copySelection((E[])this.data, this.data.length, indices, target);
	}



	///////////////////////////////////////////////////////////////////////////
	// java.util.list and derivatives //
	///////////////////////////////////

	@Override
	public boolean isEmpty()
	{
		return this.data.length == 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new ReadOnlyListIterator<>(this);
	}

	@Override
	public long size()
	{
		return this.data.length;
	}

	@Override
	public long maximumCapacity()
	{
		return this.data.length;
	}

	@Override
	public boolean isFull()
	{
		return true;
	}

	@Override
	public long remainingCapacity()
	{
		return 0;
	}

	@Override
	public ConstLinearEnum<E> view()
	{
		return this;
	}

	@Override
	public ConstLinearEnum<E> view(final long lowIndex, final long highIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public ConstLinearEnum<E> range(final long fromIndex, final long toIndex)
	{
		// range check is done in constructor
		// (14.06.2011 TM)FIXME: SubConstEnum
		throw new one.microstream.meta.NotImplementedYetError();
//		return new SubListView<>(this, fromIndex, toIndex);
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.data.length);
	}

	@Override
	public Object[] toArray()
	{
		return this.data.clone();
	}

	@SuppressWarnings("unchecked")
	@Override
	public E at(final long index) throws ArrayIndexOutOfBoundsException
	{
		if(index >= this.data.length)
		{
			throw new IndexBoundsException(this.data.length, index);
		}
		return (E)this.data[(int)index];
	}



	@Deprecated
	@Override
	public boolean equals(final Object o)
	{
		//trivial escape conditions
		if(o == this)
		{
			return true;
		}
		if(o == null || !(o instanceof List<?>))
		{
			return false;
		}

		final List<?> list = (List<?>)o;
		if(this.data.length != list.size())
		{
			return false; //lists can only be equal if they have the same length
		}

		final Object[] data = this.data;
		int i = 0;
		for(final Object e2 : list)
		{
			//use iterator for passed list as it could be a non-random-access list
			final Object e1 = data[i++];
			if(e1 == null)
			{
				//null-handling escape conditions
				if(e2 != null)
				{
					return false;
				}
				continue;
			}
			if(!e1.equals(e2))
			{
				return false;
			}
		}
		return true; //no un-equal element found, so lists must be equal
	}

	@Deprecated
	@Override
	public int hashCode()
	{
		return XArrays.arrayHashCode(this.data, this.data.length);
	}



	@Override
	public OldConstEnum<E> old()
	{
		return new OldConstEnum<>(this);
	}

	public static final class OldConstEnum<E> extends AbstractOldGettingSet<E>
	{
		OldConstEnum(final ConstLinearEnum<E> list)
		{
			super(list);
		}

		@Override
		public ConstLinearEnum<E> parent()
		{
			return (ConstLinearEnum<E>)super.parent();
		}

	}

}
