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
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XSettingList;


public class SubListAccessor<E> extends SubListView<E> implements XSettingList<E>
{
	/* (12.07.2012 TM)FIXME: complete SubListAccessor implementation
	 * See all "FIXME"s
	 */

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public SubListAccessor(final XSettingList<E> list, final long fromIndex, final long toIndex)
	{
		super(list, fromIndex, toIndex);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public  long replace(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  boolean replaceOne(final E element, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  long substitute(final Function<? super E, ? extends E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  SubListAccessor<E> range(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListAccessor<>(
			(XSettingList<E>)this.list,
			this.startIndex + fromIndex * this.d,
			this.startIndex + toIndex * this.d
		);
	}

	@Override
	public  SubListAccessor<E> fill(final long offset, final long length, final E element)
	{
		this.checkVector(offset, length);
		((XSettingList<E>)this.list).fill(this.startIndex + offset * this.d, length * this.d, element);
		return this;
	}

	@Override
	public  boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  SubListAccessor<E> reverse()
	{
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SubListAccessor<E> setAll(final long offset, final E... elements)
	{
		this.checkVector(offset, elements.length);
		if(this.d > 0)
		{
			((XSettingList<E>)this.list).setAll(this.startIndex + offset, elements);
		}
		else
		{
			((XSettingList<E>)this.list).setAll(this.startIndex - offset, XArrays.toReversed(elements));
		}
		return this;
	}

	@Override
	public  SubListAccessor<E> set(final long offset, final E[] src, final int srcIndex, final int srcLength)
	{
		this.checkVector(offset, srcLength);
		if(this.d > 0)
		{
			((XSettingList<E>)this.list).set(this.startIndex + offset * +1, src, srcIndex, srcLength);
		}
		else
		{
			final int revElementsStartIndex;
			if(srcLength == 0)
			{
				revElementsStartIndex = srcIndex;
			}
			else if(srcLength > 0)
			{
				revElementsStartIndex = srcIndex + srcLength - 1;
			}
			else
			{
				revElementsStartIndex = srcIndex + srcLength + 1;
			}
			((XSettingList<E>)this.list).set(this.startIndex + offset * -1, src, revElementsStartIndex, -srcLength);
		}
		return this;
	}

	@Override
	public  SubListAccessor<E> set(
		final long                          offset        ,
		final XGettingSequence<? extends E> elements      ,
		final long                          elementsOffset,
		final long                          elementsLength
	)
	{
		this.checkVector(offset, elementsLength);
		if(this.d > 0)
		{
			((XSettingList<E>)this.list).set(this.startIndex + offset, elements, elementsOffset, elementsLength);
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
			((XSettingList<E>)this.list).set(
				this.startIndex - offset,
				elements,
				revElementsStartIndex,
				-elementsLength
			);
		}
		return this;
	}

	@Override
	public  void setFirst(final E element)
	{
		((XSettingList<E>)this.list).setGet(this.startIndex, element);
	}

	@Override
	public  void setLast(final E element)
	{
		((XSettingList<E>)this.list).setGet(this.getEndIndex(), element);
	}

	@Override
	public  SubListAccessor<E> sort(final Comparator<? super E> comparator)
	{
		XUtilsCollection.rngSort((XSettingList<E>)this.list, this.startIndex, this.length, comparator);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		this.checkIndex(sourceIndex);
		this.checkIndex(targetIndex);
		((XSettingList<E>)this.list).shiftTo(sourceIndex, targetIndex);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		this.checkVector(sourceIndex, length);
		this.checkVector(targetIndex, length);
		((XSettingList<E>)this.list).shiftTo(sourceIndex, targetIndex, length);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftBy(final long sourceIndex, final long distance)
	{
		this.checkIndex(sourceIndex);
		this.checkIndex(sourceIndex + distance);
		((XSettingList<E>)this.list).shiftTo(sourceIndex, distance);
		return this;
	}

	@Override
	public  SubListAccessor<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		this.checkVector(sourceIndex, length);
		this.checkVector(sourceIndex + distance, length);
		((XSettingList<E>)this.list).shiftTo(sourceIndex, distance, length);
		return this;
	}

	@Override
	public  SubListAccessor<E> swap(final long indexA, final long indexB)
	{
		this.checkIndex(indexA);
		this.checkIndex(indexB);
		((XSettingList<E>)this.list).swap(this.startIndex + indexA * this.d, this.startIndex + indexB * this.d);
		return this;
	}

	@Override
	public  SubListAccessor<E> swap(final long indexA, final long indexB, final long length)
	{
		this.checkVector(indexA, length);
		this.checkVector(indexB, length);
		((XSettingList<E>)this.list).swap(
			this.startIndex + indexA * this.d,
			this.startIndex + indexB * this.d,
			length * this.d
		);
		return this;
	}

	@Override
	public  long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}


	@Override
	public  long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public  boolean set(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XSettingList<E>)this.list).set(this.startIndex + index * this.d, element);
	}

	@Override
	public  E setGet(final long index, final E element)
	{
		this.checkIndex(index);
		return ((XSettingList<E>)this.list).setGet(this.startIndex + index * this.d, element);
	}

	@Override
	public  SubListView<E> view(final long fromIndex, final long toIndex)
	{
		this.checkRange(fromIndex, toIndex);
		return new SubListView<>(this.list, this.startIndex + fromIndex * this.d, this.startIndex + toIndex * this.d);
	}

	@Override
	public  SubListAccessor<E> toReversed()
	{
		return new SubListAccessor<>((XSettingList<E>)this.list, this.getEndIndex(), this.startIndex);
	}

	@Override
	public  SubListAccessor<E> copy()
	{
		return new SubListAccessor<>((XSettingList<E>)this.list, this.startIndex, this.getEndIndex());
	}

	@Override
	public  OldSubListAccessor<E> old()
	{
		return new OldSubListAccessor<>(this);
	}

	static class OldSubListAccessor<E> extends OldSubListView<E>
	{
		OldSubListAccessor(final SubListAccessor<E> list)
		{
			super(list);
		}

		@Override
		public  SubListAccessor<E> parent()
		{
			return (SubListAccessor<E>)super.parent();
		}

	}

}
