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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XDecreasingList;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XProcessingList;
import one.microstream.collections.types.XSortableSequence;
import one.microstream.equality.Equalator;
import one.microstream.typing.XTypes;

public class SubListProcessor<E> extends SubListView<E> implements XDecreasingList<E>
{
	/* (12.07.2012 TM)FIXME: complete SubListProcessor implementation
	 * See all "FIXME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubListProcessor(final XProcessingList<E> list, final long fromIndex, final long toIndex)
	{
		super(list, fromIndex, toIndex);
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	private void internalClear()
	{
		this.size = 0;
		this.length = 0;
		this.d = 1;
	}

	private void decrement()
	{
		this.size -= 1;
		this.length -= this.d;
	}

	private void decrement(final long amount)
	{
		this.size -= amount;
		this.length -= amount * this.d;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final void clear()
	{
		((XProcessingList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		final long oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(oldListSize - ((XProcessingList<E>)this.list).size());
		return procedure;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long remove(final E element)
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> samples)
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeDuplicates()
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final E retrieve(final E element)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		final E e = XUtilsCollection.rngRetrieve((XProcessingList<E>)this.list, this.startIndex, this.length, element);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return e;
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		final E e = XUtilsCollection.rngRetrieve(
			(XProcessingList<E>)this.list,
			this.startIndex,
			this.length,
			predicate
		);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return e;
	}

	@Override
	public final boolean removeOne(final E element)
	{
		if(XUtilsCollection.rngRemoveOne((XProcessingList<E>)this.list, this.startIndex, this.length, element))
		{
			this.decrement();
			return true;
		}
		return false;
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> samples)
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final void truncate()
	{
		((XProcessingList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}

	@Override
	public final SubListProcessor<E> range(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListProcessor<>(
			(XProcessingList<E>)this.list,
			this.startIndex + fromIndex * this.d,
			this.startIndex + toIndex * this.d
		);
	}

	@Override
	public final long consolidate()
	{
		return ((XProcessingList<E>)this.list).consolidate() > 0 ? 1 : 0;
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		final long oldListSize = ((XProcessingList<E>)this.list).size();
		this.decrement(oldListSize - ((XProcessingList<E>)this.list).size());
		return target;
	}

	@Override
	public final long optimize()
	{
		return ((XProcessingList<E>)this.list).optimize();
	}

	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		final long oldListSize = ((XProcessingList<E>)this.list).size();
		((XProcessingList<E>)this.list).moveSelection(target, this.shiftIndices(indices));
		this.decrement(oldListSize - ((XProcessingList<E>)this.list).size());
		return target;
	}

	@Override
	public final E removeAt(final long index) throws UnsupportedOperationException
	{
		this.checkIndex(index);
		final E element = ((XProcessingList<E>)this.list).removeAt(index);
		this.decrement();
		return element;
	}

	@Override
	public final E fetch()
	{
		return this.removeAt(0);
	}

	@Override
	public final E pop()
	{
		return this.removeAt(this.getEndIndex());
	}

	@Override
	public final E pinch()
	{
		return this.size == 0 ? null : this.removeAt(0);
	}

	@Override
	public final E pick()
	{
		return this.size == 0 ? null : this.removeAt(this.getEndIndex());
	}

	@Override
	public final SubListProcessor<E> removeRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XProcessingList<E>)this.list).removeRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final SubListProcessor<E> retainRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XProcessingList<E>)this.list).retainRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		final int removeCount, oldListSize = XTypes.to_int(this.list.size());
		((XProcessingList<E>)this.list).removeSelection(this.shiftIndices(indices));
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubListProcessor<E> toReversed()
	{
		return new SubListProcessor<>((XProcessingList<E>)this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public final SubListProcessor<E> copy()
	{
		return new SubListProcessor<>((XProcessingList<E>)this.list, this.startIndex, this.getEndIndex());
	}

	@Override
	public final long nullRemove()
	{
		final long removeCount, oldListSize = ((XProcessingList<E>)this.list).size();

		this.decrement(removeCount = oldListSize - ((XProcessingList<E>)this.list).size());
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubListView<E> view(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListView<>(this.list, this.startIndex + fromIndex * this.d, this.startIndex + toIndex * this.d);
	}


	@Override
	public final OldSubListProcessor<E> old()
	{
		return new OldSubListProcessor<>(this);
	}

	static class OldSubListProcessor<E> extends OldSubListView<E>
	{
		OldSubListProcessor(final SubListProcessor<E> list)
		{
			super(list);
		}

		@Override
		public final SubListProcessor<E> parent()
		{
			return (SubListProcessor<E>)super.parent();
		}

	}

	@Override
	public final boolean replaceOne(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public final long replace(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public final boolean set(final long index, final E element)
		throws IndexOutOfBoundsException, ArrayIndexOutOfBoundsException
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void setFirst(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void setLast(final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XSortableSequence<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XSortableSequence<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XSortableSequence<E> shiftBy(final long sourceIndex, final long distance)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XSortableSequence<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@SafeVarargs
	@Override
	public final XDecreasingList<E> setAll(final long index, final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> set(final long index, final E[] elements, final int offset, final int length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> set(
		final long                           index   ,
		final XGettingSequence<? extends E> elements,
		final long                           offset  ,
		final long                           length
	)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> swap(final long indexA, final long indexB)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> swap(final long indexA, final long indexB, final long length)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> reverse()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> fill(final long offset, final long length, final E element)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final XDecreasingList<E> sort(final Comparator<? super E> comparator)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

}
