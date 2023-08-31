
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
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XProcessingList;
import one.microstream.collections.types.XSettingList;
import one.microstream.equality.Equalator;
import one.microstream.typing.XTypes;


public final class SubList<E> extends SubListAccessor<E> implements XList<E>
{
	/* (12.07.2012 TM)FIXME: complete SubList implementation
	 * See all "FIXME"s
	 * remove redundant method implementations
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubList(final XList<E> list, final long fromIndex, final long toIndex)
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

	private void increment()
	{
		this.size += 1;
		this.length += this.d;
	}

	private void increment(final long amount)
	{
		this.size += amount;
		this.length += amount * this.d;
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
	public final long currentCapacity()
	{
		return ((XList<E>)this.list).currentCapacity();
	}

	@Override
	public final void clear()
	{
		((XList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}




	///////////////////////////////////////////////////////////////////////////
	// adding //
	///////////

	@Override
	public final void accept(final E element)
	{
		this.add(element);
	}

	@Override
	public final boolean add(final E e)
	{
		((XList<E>)this.list).input(this.startIndex + this.length, e);
		this.increment();
		return true;
	}

	@SafeVarargs
	@Override
	public final SubList<E> addAll(final E... elements)
	{
		((XList<E>)this.list).inputAll(this.startIndex + this.length, elements);
		this.increment(elements.length);
		return this;
	}

	@Override
	public final SubList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).inputAll(this.startIndex + this.length, elements);
		this.increment(((XList<E>)this.list).size() - oldListSize);
		return this;
	}

	@Override
	public final SubList<E> addAll(final E[] elements, final int offset, final int length)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).inputAll(this.getEndIndex(), elements, offset, length);
		this.increment(((XList<E>)this.list).size() - oldListSize);
		return this;
	}

	@Override
	public final boolean nullAdd()
	{
		((XList<E>)this.list).input(this.startIndex + this.length, (E)null);
		this.increment();
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// putting //
	////////////

	@Override
	public final boolean put(final E element)
	{
		return this.add(element);
	}

	@SafeVarargs
	@Override
	public final SubList<E> putAll(final E... elements)
	{
		return this.addAll(elements);
	}

	@Override
	public final SubList<E> putAll(final E[] elements, final int offset, final int length)
	{
		return this.addAll(elements, offset, length);
	}

	@Override
	public final SubList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		return elements.iterate(this);
	}

	@Override
	public final boolean nullPut()
	{
		return this.nullAdd();
	}



	///////////////////////////////////////////////////////////////////////////
	// prepending //
	///////////////

	@Override
	public final boolean prepend(final E element)
	{
		if(this.d > 0)
		{
			if(((XList<E>)this.list).insert(this.startIndex, element))
			{
				this.startIndex--;
				this.increment();
				return true;
			}

		}
		else
		{
			if(((XList<E>)this.list).insert(this.startIndex + 1, element))
			{
				this.startIndex++;
				this.increment();
				return true;
			}
		}
		return false;
	}

	@SafeVarargs
	@Override
	public final SubList<E> prependAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubList<E> prependAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullPrepend()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// preputting //
	///////////////

	@Override
	public final boolean preput(final E element)
	{
		if(this.d > 0)
		{
			((XList<E>)this.list).input(this.startIndex, element);
			this.startIndex--;

		}
		else
		{
			((XList<E>)this.list).input(this.startIndex + 1, element);
			this.startIndex++;
		}
		this.increment();
		return true;
	}

	@SafeVarargs
	@Override
	public final SubList<E> preputAll(final E... elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubList<E> preputAll(final E[] elements, final int srcStartIndex, final int srcLength)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final boolean nullPreput()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// inserting //
	//////////////

	@Override
	public final boolean insert(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XList<E>)this.list).insert(this.startIndex + index * this.d, element);
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements)
	{
		this.checkIndex(index);
		final int oldListSize = XTypes.to_int(this.list.size());

		if(this.d > 0)
		{
			((XList<E>)this.list).insertAll(this.startIndex + index, elements);
		}
		else
		{
			((XList<E>)this.list).insertAll(this.startIndex - index, XArrays.toReversed(elements));
		}
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		this.checkIndex(index);
		final int oldListSize = XTypes.to_int(this.list.size());

		if(this.d > 0)
		{
			((XList<E>)this.list).insertAll(this.startIndex + index, elements, offset, length);
		}
		else
		{
			((XList<E>)this.list).insertAll(this.startIndex - index, XArrays.toReversed(elements, offset, length));
		}
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).insertAll(this.startIndex + index * this.d, elements);
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final boolean nullInsert(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}



	///////////////////////////////////////////////////////////////////////////
	// inputting //
	//////////////

	@Override
	public final boolean input(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XList<E>)this.list).input(this.startIndex + index * this.d, element);
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		this.checkIndex(index);
		final int oldListSize = XTypes.to_int(this.list.size());

		if(this.d > 0)
		{
			((XList<E>)this.list).inputAll(this.startIndex + index, elements);
		}
		else
		{
			((XList<E>)this.list).inputAll(this.startIndex - index, XArrays.toReversed(elements));
		}
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		this.checkIndex(index);
		final int oldListSize = XTypes.to_int(this.list.size());

		if(this.d > 0)
		{
			((XList<E>)this.list).inputAll(this.startIndex + index, elements, offset, length);
		}
		else
		{
			((XList<E>)this.list).inputAll(this.startIndex - index, XArrays.toReversed(elements, offset, length));
		}
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).inputAll(this.startIndex + index * this.d, elements);
		final long increase;
		this.increment(increase = ((XList<E>)this.list).size() - oldListSize);
		return XTypes.to_int(increase);
	}

	@Override
	public final boolean nullInput(final long index)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P procedure)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		XUtilsCollection.rngProcess((XProcessingList<E>)this.list, this.startIndex, this.length, procedure);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return procedure;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRemoveDuplicates((XProcessingList<E>)this.list, this.startIndex, this.length, equalator);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
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
			(XProcessingList<E>)this.list, this.startIndex, this.length, predicate
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
	public final long remove(final E element)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRemove((XProcessingList<E>)this.list, this.startIndex, this.length, element);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRemoveAll((XProcessingList<E>)this.list, this.startIndex, this.length, elements);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeDuplicates()
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRemoveDuplicates((XProcessingList<E>)this.list, this.startIndex, this.length);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRetainAll((XProcessingList<E>)this.list, this.startIndex, this.length, elements);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngReduce((XProcessingList<E>)this.list, this.startIndex, this.length, predicate);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final void truncate()
	{
		((XList<E>)this.list).removeRange(this.startIndex, this.length);
		this.internalClear();
	}

	@Override
	public final SubList<E> range(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubList<>(
			(XList<E>)this.list,
			this.startIndex + fromIndex * this.d,
			this.startIndex + toIndex * this.d
		);
	}





	@Override
	public final SubList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		((XList<E>)this.list).ensureFreeCapacity(minimalFreeCapacity);
		return this;
	}

	@Override
	public final SubList<E> ensureCapacity(final long minimalCapacity)
	{
		((XList<E>)this.list).ensureCapacity(minimalCapacity + this.size);
		return this;
	}

	@Override
	public final long consolidate()
	{
		return ((XList<E>)this.list).consolidate() > 0 ? 1 : 0;
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		XUtilsCollection.rngMoveTo((XProcessingList<E>)this.list, this.startIndex, this.length, target, predicate);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return target;
	}

	@Override
	public final long optimize()
	{
		return ((XList<E>)this.list).optimize();
	}





	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).moveSelection(target, this.shiftIndices(indices));
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return target;
	}

	@Override
	public final E removeAt(final long index) throws UnsupportedOperationException
	{
		this.checkIndex(index);
		final E element = ((XList<E>)this.list).removeAt(index);
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
	public final SubList<E> removeRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).removeRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final SubList<E> retainRange(final long startIndex, final long length)
	{
		this.checkVector(startIndex, length);
		final int oldListSize = XTypes.to_int(this.list.size());
		((XList<E>)this.list).retainRange(this.startIndex + startIndex * this.d, length * this.d);
		this.decrement(oldListSize - XTypes.to_int(this.list.size()));
		return this;
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		((XList<E>)this.list).removeSelection(this.shiftIndices(indices));
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubList<E> toReversed()
	{
		return new SubList<>((XList<E>)this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public final SubList<E> copy()
	{
		return new SubList<>((XList<E>)this.list, this.startIndex, this.getEndIndex());
	}

	@Override
	public final long nullRemove()
	{
		final long removeCount, oldListSize = ((XList<E>)this.list).size();
		XUtilsCollection.rngRemoveNull((XProcessingList<E>)this.list, this.startIndex, this.length);
		this.decrement(removeCount = oldListSize - XTypes.to_int(this.list.size()));
		return XTypes.to_int(removeCount);
	}

	@Override
	public final SubList<E> sort(final Comparator<? super E> comparator)
	{
		XUtilsCollection.rngSort((XSettingList<E>)this.list, this.startIndex, this.length, comparator);
		return this;
	}

	@Override
	public final SubList<E> swap(final long indexA, final long indexB)
	{
		this.checkIndex(indexA);
		this.checkIndex(indexB);
		((XList<E>)this.list).swap(this.startIndex + indexA * this.d, this.startIndex + indexB * this.d);
		return this;
	}

	@Override
	public final SubList<E> swap(final long indexA, final long indexB, final long length)
	{
		this.checkVector(indexA, length);
		this.checkVector(indexB, length);
		((XList<E>)this.list).swap(
			this.startIndex + indexA * this.d,
			this.startIndex + indexB * this.d,
			length * this.d
		);
		return this;
	}

	@SafeVarargs
	@Override
	public final SubList<E> setAll(final long offset, final E... elements)
	{
		super.setAll(offset, elements);
		return this;
	}

	@Override
	public final SubList<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		super.set(offset, src, srcIndex, srcLength);
		return this;
	}

	@Override
	public final SubList<E> set(
		final long                           offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                           elementsOffset,
		final long                           elementsLength
	)
	{
		this.checkVector(offset, elementsLength);
		if(this.d > 0)
		{
			((XList<E>)this.list).set(this.startIndex + offset, elements, elementsOffset, elementsLength);
		}
		else
		{
			final long revElementsStartIndex;
			if(elementsLength == 0)
			{
				revElementsStartIndex = elementsOffset;
			}
			else if(elementsLength > 0)
			{
				revElementsStartIndex = elementsOffset + elementsLength - 1;
			}
			else
			{
				revElementsStartIndex = elementsOffset + elementsLength + 1;
			}
			((XList<E>)this.list).set(this.startIndex - offset, elements, revElementsStartIndex, -elementsLength);
		}
		return this;
	}

	@Override
	public final SubList<E> fill(final long offset, final long length, final E element)
	{
		this.checkVector(offset, length);
		((XList<E>)this.list).fill(this.startIndex + offset * this.d, length * this.d, element);
		return this;
	}


	@Override
	public final OldSubList<E> old()
	{
		return new OldSubList<>(this);
	}

	static class OldSubList<E> extends OldSubListAccessor<E>
	{
		OldSubList(final SubList<E> list)
		{
			super(list);
		}

		@Override
		public final SubList<E> parent()
		{
			return (SubList<E>)super.parent();
		}

	}

	@Override
	public final SubList<E> reverse()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final SubList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		super.shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public final SubList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		super.shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public final SubList<E> shiftBy(final long sourceIndex, final long distance)
	{
		super.shiftBy(sourceIndex, distance);
		return this;
	}

	@Override
	public final SubList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		super.shiftBy(sourceIndex, distance, length);
		return this;
	}

}
