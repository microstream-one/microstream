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

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.branching.ThrowBreak;
import one.microstream.chars.VarString;
import one.microstream.chars.XChars;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.equality.Equalator;
import one.microstream.functional.Aggregator;
import one.microstream.functional.IndexedAcceptor;
import one.microstream.reference.ReferenceType;
import one.microstream.typing.XTypes;


public class ChainStorageStrong<E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
extends AbstractChainStorage<E, K, V, EN>
{
	static final String exceptionRange(final long size, final long offset, final long length)
	{
		return "Range [" + (length < 0 ? offset + length + 1 + ";" + offset
			: offset + ";" + (offset + length - 1)) + "] not in [0;" + (size - 1) + "]";
	}

	static final String exceptionIndexOutOfBounds(final long size, final long index)
	{
		return "Index: " + index + ", Size: " + size;
	}

	static final String exceptionIllegalSwapBounds(final long indexA, final long indexB, final long length)
	{
		return "Illegal swap bounds: (" + indexA + " [" + length + "] -> " + indexB + " [" + length + "])";
	}

	static final int validateArrayIteration(final Object[] array, final int offset, final int length)
	{
		// elements array range checking
		if(length >= 0)
		{
			if(offset < 0 || offset + length > array.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
			}
			if(length == 0)
			{
				return 0;
			}
			return +1; // incrementing direction
		}
		else if(length < 0)
		{
			if(offset + length < -1 || offset >= array.length)
			{
				throw new IndexOutOfBoundsException(exceptionRange(array.length, offset, length));
			}
			return -1; // decrementing direction
		}
		else if(offset < 0 || offset >= array.length)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(array.length, offset));
		}
		else
		{
			// handle length 0 special case not as escape condition but as last case to ensure index checking
			return 0;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// sorting internals //
	//////////////////////

	static <E, K, V, EN extends AbstractChainEntry<E, K, V, EN>>
	void mergesortHead(final EN head, final Comparator<? super E> comparator)
	{
		EN last, entry;
		try
		{
			entry = mergesort0(head.next, comparator); // sort
		}
		catch(final Throwable e)
		{
			// rollback 8-), even works on OOME
			for(entry = head.prev; (entry = (last = entry).prev) != head;)
			{
				entry.next = last;
			}
			throw e;
		}

		// reattach sorted chain to head and rebuild prev direction
		(head.next = entry).prev = head;              // entry is new start entry
		while((entry = (last = entry).next) != null)
		{
			entry.prev = last;                        // rebuild prev references
		}
		head.prev = last;                             // last entry now is new end entry (obviously)
	}

	private static <E, K, V, EN extends AbstractChainEntry<E, K, V, EN>> EN mergesort0(
		final EN                    chain     ,
		final Comparator<? super E> comparator
	)
	{
		// special case handling for empty or trivial chain
		if(chain == null || chain.next == null)
		{
			return chain;
		}

		// inlined iterative splitting
		EN chain2, t1, t2 = chain2 = (t1 = chain).next;
		while(t2 != null && (t1 = t1.next = t2.next) != null)
		{
			t2 = t2.next = t1.next;
		}

		// merging
		return merge1(mergesort0(chain, comparator), mergesort0(chain2, comparator), comparator);
	}

	// merge iterative
	private static <E, K, V, EN extends AbstractChainEntry<E, K, V, EN>> EN merge1(
		EN                          c1 ,
		EN                          c2 ,
		final Comparator<? super E> cmp
	)
	{
		if(c1 == null)
		{
			return c2;
		}
		if(c2 == null)
		{
			return c1;
		}

		final EN c;
		if(cmp.compare(c1.element(), c2.element()) < 0)
		{
			c1 = (c = c1).next;
		}
		else
		{
			c2 = (c = c2).next;
		}

		for(EN t = c;;)
		{
			if(c1 == null)
			{
				t.next = c2;
				break;
			}
			else if(c2 == null)
			{
				t.next = c1;
				break;
			}
			else if(cmp.compare(c1.element(), c2.element()) < 0)
			{
				c1 = (t = t.next = c1).next;
			}
			else
			{
				c2 = (t = t.next = c2).next;
			}
		}
		return c;
	}




	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final AbstractChainCollection<E, K, V, EN> parent;
	final EN head;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ChainStorageStrong(final AbstractChainCollection<E, K, V, EN> parent, final EN head)
	{
		super();
		this.parent = parent;
		(this.head = head).prev = head; // intentionally no setting of head.next! (supposed to remain null)
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////


	@Override
	protected final EN head()
	{
		return this.head;
	}

	@Override
	protected void disjoinEntry(final EN entry)
	{
		((entry.next == null ? this.head : entry.next).prev = entry.prev).next = entry.next;
	}
	
	@Override
	protected void replace(final EN doomedEntry, final EN keptEntry)
	{
		(doomedEntry.next == null ? this.head : doomedEntry.next).prev = keptEntry;
		doomedEntry.prev.next = keptEntry;
		keptEntry.prev = doomedEntry.prev;
		keptEntry.next = doomedEntry.next;
	}
	
	@Override
	protected long substitute(final Function<? super E, ? extends E> mapper, final BiConsumer<EN, E> callback)
	{
		long count = 0;
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				final E newElement = mapper.apply(entry.element());
				if(newElement != entry.element())
				{
					callback.accept(entry, newElement);
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return count;
	}

	@Override
	protected final boolean moveToStart(final EN entry)
	{
		if(entry.prev == this.head)
		{
			return false; // already at the start, do nothing
		}
		if(entry.next == null)
		{
			// entry at end special case
			(this.head.prev = entry.prev).next = null;
		}
		else
		{
			// disjoin from current position
			entry.prev.next = entry.next;
			entry.next.prev = entry.prev;
		}

		// join in at first position
		entry.next = this.head.next;
		(entry.prev = this.head).next = entry;

		return true;
	}

	@Override
	protected final boolean moveToEnd(final EN entry)
	{
		if(entry.next == null)
		{
			return false; // already at the end, do nothing
		}

		// disjoin from current position
		entry.prev.next = entry.next;
		entry.next.prev = entry.prev;

		// join in at head position and mark as end of chain
		(entry.prev = this.head.prev).next = this.head.prev = entry;
		entry.next = null;
		return true;
	}

	@Override
	public final void appendEntry(final EN entry)
	{
		(entry.prev = this.head.prev).next = this.head.prev = entry;
	}

	@Override
	public final void prependEntry(final EN entry)
	{
		entry.next = this.head.next;
		(entry.prev = this.head).next = entry;
	}

	@Override
	public void clear()
	{
		// reset singleton fields
		(this.head.prev = this.head).next = null;
	}

	@Override
	public final void shiftBy(final long sourceIndex, final long distance)
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME not implemented yet
	}

	@Override
	public final void shiftBy(final long sourceIndex, final long distance, final long length)
	{
		// FIXME not implemented yet
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public final void shiftTo(final long sourceIndex, final long targetIndex)
	{
		// FIXME not implemented yet
		throw new one.microstream.meta.NotImplementedYetError();
	}

	@Override
	public final void shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		// FIXME not implemented yet
		throw new one.microstream.meta.NotImplementedYetError();
	}

	private void swapEntries(final EN entryA, final EN entryB)
	{
		//  / A \---1-->/ A \---3-->/ A \ ... / B \---5-->/ B \---7-->/ B \
		//  \prv/<--2---\   /<--4---\nxt/     \prv/<--6---\   /<--8---\nxt/

		EN temp;

		// step 1: swap next references
		((temp = entryA.next) != null ? temp : this.head).prev = entryB; // 4
		((entryA.next = entryB.next)                                     // 3
			!= null ? entryB.next : this.head).prev = entryA;            // 8
		entryB.next = temp;                                              // 7

		// step 2: swap prev references (prev can never be null, only head)
		(temp = entryA.prev).next = entryB; // 1
		entryB.prev.next = entryA;          // 5
		entryA.prev = entryB.prev;          // 2
		entryB.prev = temp;                 // 6
	}




	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	// removing - indexed //

	@Override
	public final void removeRange(final long offset, long length)
	{
		EN e;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		if(length > 0)
		{
			e = this.getRangeChainEntry(offset + length, -length);
		}
		else
		{
			e = this.getRangeChainEntry(offset, length);
			length = -length;
		}
		for(; length-- > 0; e = e.prev)
		{
			e.removeFrom(parent);
		}
	}

	@Override
	public final void retainRange(final long offset, final long length)
	{
		// FIXME ChainStrongStrongStorage#retainRange()
		throw new one.microstream.meta.NotImplementedYetError();
	}

	/**
	 * Removes all entries at the indices (offsets) given in the passed {@code int} array.
	 * <p>
	 * Note that the indices array gets presorted to increase algorithm performance. Pass a clone if the original
	 * {@code int} array shall be unchanged.
	 *
	 * @param indices the indices (offsets) of the entries to be removed.
	 */
	@Override
	public final long removeSelection(final long... indices)
	{
		long s = this.parent.size() - 1;
		XSort.sort(indices);

		// validate indices before copying the first element
		if(indices[0] < 0)
		{
			// check lowest index against 0 bound.
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(s + 1, indices[0]));
		}
		if(indices[indices.length - 1] > s)
		{
			// check highest index against size bound.
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(s + 1, indices[indices.length - 1]));
		}

		// remove all entries in a single reverse direction pass
		EN e = this.head.prev;
		long idx;
		long removeCount = 0;
		final long lastIdx = -1;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		for(int i = indices.length - 1; i > 0; i--)
		{
			// iterate over all (sorted) indices in reverse order
			if((idx = indices[i]) == lastIdx)
			{
				continue; // skip duplicate indices
			}
			while(--s > idx)
			{
				e = e.prev; // scroll to next entry to remove
			}
			e.removeFrom(parent); // remove entry, continue loop with next index
			removeCount++;
		}

		return removeCount;
	}

	@Override
	public final Iterator<E> iterator()
	{
		return new KeyItr();
	}

	@Override
	public final boolean equalsContent(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator
	)
	{
		if(this.parent.size() != other.size())
		{
			return false;
		}

		if(other instanceof AbstractSimpleArrayCollection<?>)
		{
			final long otherSize = other.size();
			final E[] otherData = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)other);
			EN entry = this.head.next;
			for(int i = 0; i < otherSize; i++, entry = entry.next)
			{
				if(!equalator.equal(entry.element(), otherData[i]))
				{
					return false;
				}
			}
			return true;
		}

		final Aggregator<E, Boolean> agg = new Aggregator<E, Boolean>()
		{
			private EN entry = ChainStorageStrong.this.head;
			private boolean notEqual; // false by default

			@Override
			public final void accept(final E element)
			{
				if((this.entry = this.entry.next) == null)
				{
					this.notEqual = true;
					throw X.BREAK(); // chain is too short
				}
				if(!equalator.equal(element, this.entry.element()))
				{
					this.notEqual = true;
					throw X.BREAK(); // unequal element found
				}
			}

			@Override
			public final Boolean yield()
			{
				/*
				 * no explicitly unequal pair may have been found (obviously)
				 * current entry may not be null (otherwise chain was too short)
				 * but next entry in chain must be null (otherwise chain is too long)
				 */
				return this.notEqual || this.entry == null || (this.entry = this.entry.next) != null
					? FALSE
					: TRUE
				;
			}
		};

		other.iterate(agg);
		return agg.yield();
	}



	///////////////////////////////////////////////////////////////////////////
	// chain navigation //
	/////////////////////

	@Override
	public EN getChainEntry(long index) throws IndexOutOfBoundsException
	{
		if(index < 0 || index >= this.parent.size())
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(this.parent.size(), index));
		}
		EN e = this.head;
		if(this.parent.size() >>> 1 < index)
		{
			for(final long size = this.parent.size(); index < size; index++)
			{
				e = e.prev;
			}
		}
		else
		{
			for(; index >= 0; index--)
			{
				e = e.next;
			}
		}
		return e;
	}

	@Override
	public EN getRangeChainEntry(long offset, final long length) throws IndexOutOfBoundsException
	{
		final long size = this.parent.size();

		// validate length
		if(length >= 0)
		{
			if(offset + length > size)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			if(length == 0)
			{
				return this.head;
			}
			// fall through to offset scrolling
		}
		else if(length < 0)
		{
			if(offset + length < -1)
			{
				throw new IndexOutOfBoundsException(exceptionRange(size, offset, length));
			}
			// fall through to offset scrolling
		}
		else if(offset < 0 || offset >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
		}
		else
		{
			return null; // special case length 0: skip offset scrolling and return null (length 0 chain).
		}

		// scroll to offset
		EN entry = this.head;

		if(offset <= size >>> 1)
		{
			// <= to cover cases like offset 0, size 1
			if(offset < 0)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
			}
			while(offset-- >= 0)
			{
				entry = entry.next;
			}
		}
		else
		{
			if(offset >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, offset));
			}
			while(offset++ < size)
			{
				entry = entry.prev;
			}
		}
		return entry; // entry at valuesid index offset with valuesid scrollable +/- length.
	}

	@Override
	public EN getIntervalLowChainEntry(long lowIndex, final long highIndex) throws IndexOutOfBoundsException
	{
		if(lowIndex < 0)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(this.parent.size(), lowIndex));
		}
		if(highIndex >= this.parent.size())
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(this.parent.size(), highIndex));
		}

		EN entry = this.head;
		while(lowIndex-- >= 0)
		{
			entry = entry.next;
		}
		return entry;
	}



	///////////////////////////////////////////////////////////////////////////
	// content info //
	/////////////////

	@Override
	public final long size()
	{
		return this.parent.size();
	}

	@Override
	public final long consolidate()
	{
		return 0;
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final ReferenceType getReferenceType()
	{
		return ReferenceType.STRONG;
	}



	///////////////////////////////////////////////////////////////////////////
	// containing //
	///////////////

	// containing - null //

	@Override
	public final boolean containsNull()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullElement())
			{
				return true;
			}
		}
		return false;
	}

	// containing - identity //

	@Override
	public final boolean containsId(final E element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				return true;
			}
		}
		return false;
	}

	// containing - logical //

	@Override
	public final boolean contains(final E element)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean contains(final E sample, final Equalator<? super E> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				return true;
			}
		}
		return false;
	}

	// containing - all array //

	@Override
	public final boolean containsAll(final E[] elements, final int elementsOffset, final int elementsLength)
	{
		final EN first;
		if((first = this.head.next) == null)
		{
			return false; // size 0
		}
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return true;
		}
		final int elementsBound = elementsOffset + elementsLength;

		main:
		for(int ei = elementsOffset; ei != elementsBound; ei += d)
		{
			final E element = elements[ei];
			for(EN e = first; e != null; e = e.next)
			{
				if(e.element() == element)
				{
					continue main;
				}
			}
			return false;  // one element was not found in this list, return false
		}
		return true; // all elements have been found, return true
	}



	// containing - all collection //

	@Override
	public final boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.containsAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size())
			);
		}

		// iterate by predicate function
		return elements.applies(this::contains);
	}



	///////////////////////////////////////////////////////////////////////////
	// applying //
	/////////////

	// applying - single //

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					return true;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return false;
	}

	// applying - all //

	@Override
	public final boolean appliesAll(final Predicate<? super E> predicate)
	{
		try
		{
			EN e = this.head;
			if(e.next == null)
			{
				// must check for the special case of no entries (predicate cannot apply).
				return false;
			}
			
			while((e = e.next) != null)
			{
				if(!predicate.test(e.element()))
				{
					return false;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return true;
	}



	///////////////////////////////////////////////////////////////////////////
	// counting //
	/////////////

	// counting - element //

	@Override
	public final long count(final E element)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				count++;
			}
		}
		return count;
	}

	@Override
	public final long count(final E sample, final Equalator<? super E> equalator)
	{
		int count = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				count++;
			}
		}
		return count;
	}

	// counting - predicate //

	@Override
	public final long count(final Predicate<? super E> predicate)
	{
		int count = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					count++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return count;
	}



	///////////////////////////////////////////////////////////////////////////
	// data arithmetic //
	////////////////////

	// data - data sets //

	@Override
	public final <C extends Consumer<? super E>> C intersect(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = XTypes.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final E element = entry.element();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						target.accept(element);
						continue ch;
					}
				}
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.element();
			if(samples.containsSearched(equalCurrentElement))
			{
				target.accept(equalCurrentElement.sample);
			}
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super E>> C except(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = XTypes.to_int(samples.size());
			ch:
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				final E element = entry.element();
				for(int i = 0; i < size; i++)
				{
					if(equalator.equal(element, array[i]))
					{
						continue ch;
					}
				}
				target.accept(element);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		ch:
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			equalCurrentElement.sample = entry.element();
			if(samples.containsSearched(equalCurrentElement))
			{
				continue ch;
			}
			target.accept(equalCurrentElement.sample);
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super E>> C union(
		final XGettingCollection<? extends E> samples,
		final Equalator<? super E> equalator,
		final C target
	)
	{
		this.copyTo(target);
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			final E[] array = AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples);
			final int size = XTypes.to_int(samples.size());
			ch:
			for(int i = 0; i < size; i++)
			{
				final E sample = array[i];
				for(EN entry = this.head.next; entry != null; entry = entry.next)
				{
					if(equalator.equal(entry.element(), sample))
					{
						continue ch;
					}
				}
				target.accept(sample);
			}
			return target;
		}

		/* has to be the long way around because:
		 * - can't directly pass an instance of type E to a collection of type ? extends E.
		 * - chain's equal element instances must be added, not samples'.
		 */
		samples.iterate(e ->
		{
			// local reference to AIC field
			final Equalator<? super E> equalator2 = equalator;

			for(EN entry = ChainStorageStrong.this.head.next; entry != null; entry = entry.next)
			{
				if(equalator2.equal(e, entry.element()))
				{
					return;
				}
			}
			target.accept(e);
		});
		return target;
	}

	// data - copying //

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			target.accept(e.element());
		}
		return target;
	}

	@Override
	public final <C extends Consumer<? super E>> C copySelection(final C target, final long[] indices)
	{
		final long length = indices.length, size = this.parent.size();

		// validate all indices before copying the first element
		for(int i = 0; i < length; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		// actual copying. Note: can't sort indices as copying order might be relevant
		for(int i = 0; i < length; i++)
		{
			target.accept(this.getChainEntry(indices[i]).element()); // scrolling is pretty inefficient here :(
		}

		return target;
	}

	@Override
	public final int copyToArray(final long offset, int length, final Object[] target, final int targetOffset)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}

		if(targetOffset < 0)
		{
			throw new ArrayIndexOutOfBoundsException(targetOffset);
		}
		if((length < 0 ? -length : length) + targetOffset > target.length)
		{
			throw new ArrayIndexOutOfBoundsException((length < 0 ? -length : length) + targetOffset);
		}

		int t = targetOffset;
		if(length > 0)
		{
			for(; length-- > 0; e = e.next)
			{
				target[t++] = e.element();
			}
		}
		else
		{
			for(; length++ < 0; e = e.prev)
			{
				target[t++] = e.element();
			}
		}
		return t - targetOffset;
	}

	// data - conditional copying //

	@Override
	public final <C extends Consumer<? super E>> C copyTo(final C target, final Predicate<? super E> predicate)
	{
		try
		{
			for(EN entry = this.head.next; entry != null; entry = entry.next)
			{
				if(predicate.test(entry.element()))
				{
					target.accept(entry.element());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return target;
	}

	// data - array transformation //

	@Override
	public final Object[] toArray()
	{
		final Object[] array;
		this.copyToArray(0, XTypes.to_int(this.parent.size()), array = new Object[XTypes.to_int(this.parent.size())], 0);
		return array;
	}

	@Override
	public final      E[] toArray(final Class<E> type)
	{
		final E[] array;
		this.copyToArray(0, XTypes.to_int(this.parent.size()), array = X.Array(type, XTypes.to_int(this.parent.size())), 0);
		return array;
	}



	///////////////////////////////////////////////////////////////////////////
	// querying //
	/////////////

	@Override
	public final E first()
	{
		return this.head.next == null ? null : this.head.next.element();
	}

	@Override
	public final E last()
	{
		return this.head.prev == this.head ? null : this.head.prev.element();
	}

	@Override
	public final E get(final long index)
	{
		return this.getChainEntry(index).element();
	}



	///////////////////////////////////////////////////////////////////////////
	// searching //
	//////////////

	// searching - sample //

	@Override
	public final E seek(final E sample, final Equalator<? super E> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				return e.element();
			}
		}
		return null;
	}

	// searching - predicate //

	@Override
	public final E seek(final E sample)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(sample == e.element())
			{
				return e.element();
			}
		}
		return null;
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					return e.element();
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return null;
	}

	// searching - min max //

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		E element, loopMinElement = e.element();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMinElement, element = e.element()) > 0)
			{
				loopMinElement = element;
			}
		}
		return loopMinElement;
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return null;
		}

		E element, loopMaxElement = e.element();
		for(e = e.next; e != null; e = e.next)
		{
			if(comparator.compare(loopMaxElement, element = e.element()) < 0)
			{
				loopMaxElement = element;
			}
		}
		return loopMaxElement;
	}



	///////////////////////////////////////////////////////////////////////////
	// executing //
	//////////////

	// executing - procedure //

	@Override
	public final void iterate(final Consumer<? super E> procedure)
	{
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				procedure.accept(entry.element());
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	@Override
	public final <A> void join(final BiConsumer<? super E, A> joiner, final A aggregate)
	{
		try
		{
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				joiner.accept(entry.element(), aggregate);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// executing - indexed procedure //

	@Override
	public final void iterateIndexed(final IndexedAcceptor<? super E> procedure)
	{
		try
		{
			int i = -1;
			for(EN entry = this.head; (entry = entry.next) != null;)
			{
				procedure.accept(entry.element(), ++i);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}

	// executing - conditional //

	@Override
	public final void iterate(final Predicate<? super E> predicate, final Consumer<? super E> procedure)
	{
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					procedure.accept(e.element());
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// indexing //
	/////////////

	// indexing - single //

	@Override
	public final long indexOf(final E element)
	{
		long i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(e.element() == element)
			{
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public final long indexOf(final E sample, final Equalator<? super E> equalator)
	{
		long i = 0;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(equalator.equal(e.element(), sample))
			{
				return i;
			}
		}
		
		return -1;
	}
	

	@Override
	public final long lastIndexOf(final E element)
	{
		long i = this.size();
		for(EN e = this.head.prev; e != this.head; e = e.prev, i--)
		{
			if(e.element() == element)
			{
				return i;
			}
		}
		
		return -1;
	}

	@Override
	public final long lastIndexOf(final E sample, final Equalator<? super E> equalator)
	{
		long i = this.size();
		for(EN e = this.head.prev; e != this.head; e = e.prev, i--)
		{
			if(equalator.equal(e.element(), sample))
			{
				return i;
			}
		}
		
		return -1;
	}

	// indexing - predicate //

	@Override
	public final long indexOf(final Predicate<? super E> predicate)
	{
		long i = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next, i++)
			{
				if(predicate.test(e.element()))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return -1;
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		long i = this.size();
		try
		{
			for(EN e = this.head.prev; e != this.head; e = e.prev, i--)
			{
				if(predicate.test(e.element()))
				{
					return i;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return -1;
	}

	// indexing - min max //

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		E loopMinElement = e.element();
		long loopMinIndex = 0;
		long i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final E element;
			if(comparator.compare(loopMinElement, element = e.element()) > 0)
			{
				loopMinElement = element;
				loopMinIndex = i;
			}
		}
		return loopMinIndex;
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return -1;
		}

		E loopMaxElement = e.element();
		long loopMaxIndex = 0;
		long i = 1;
		for(e = e.next; e != null; e = e.next, i++)
		{
			final E element;
			if(comparator.compare(loopMaxElement, element = e.element()) < 0)
			{
				loopMaxElement = element;
				loopMaxIndex = i;
			}
		}

		return loopMaxIndex;
	}

	// indexing - scan //

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		long i = 0;
		long foundIndex = -1;
		for(EN e = this.head.next; e != null; e = e.next, i++)
		{
			if(predicate.test(e.element()))
			{
				foundIndex = i;
			}
		}
		return foundIndex;
	}



	///////////////////////////////////////////////////////////////////////////
	// distinction //
	////////////////

	@Override
	public final boolean hasDistinctValues()
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.element())
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public final boolean hasDistinctValues(final Equalator<? super E> equalator)
	{
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.element()))
				{
					return false;
				}
			}
		}
		return true;
	}

	// distinction copying //

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target)
	{
		mainLoop: // find last distinct element in reverse order: means put first distinct element to target
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.element())
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}

	@Override
	public final <C extends Consumer<? super E>> C distinct(final C target, final Equalator<? super E> equalator)
	{
		mainLoop: // find last distinct element in reverse order: means put first distinct element to target
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.element()))
				{
					continue mainLoop;
				}
			}
			target.accept(element);
		}

		return target;
	}



	///////////////////////////////////////////////////////////////////////////
	// VarString appending //
	////////////////////////

	@Override
	public final VarString appendTo(final VarString vc)
	{
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			vc.add(entry.element());
		}
		return vc;
	}

	@Override
	public final VarString appendTo(final VarString vc, final char separator)
	{
		if(this.head.next == null)
		{
			return vc;
		}
		for(EN entry = this.head; (entry = entry.next) != null;)
		{
			entry.assembleElement(vc).add(separator);
		}
		return vc.deleteLast();
	}

	@Override
	public final VarString appendTo(final VarString vc, final String separator)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.appendTo(vc);
		}

		if(this.head.next == null)
		{
			return vc;
		}

		final char[] sepp = XChars.readChars(separator);
		for(EN entry = this.head; (entry = entry.next) != null;)
		{
			entry.assembleElement(vc).add(sepp);
		}
		return vc.deleteLast(sepp.length);
	}

	@Override
	public final VarString appendTo(final VarString vc, final BiConsumer<VarString, ? super E> appender)
	{
		for(EN entry = this.head.next; entry != null; entry = entry.next)
		{
			appender.accept(vc, entry.element());
		}
		return vc;
	}

	@Override
	public final VarString appendTo(final VarString vc, final BiConsumer<VarString, ? super E> appender, final char separator)
	{
		EN entry;
		if((entry = this.head.next) == null)
		{
			return vc;
		}
		appender.accept(vc, entry.element());
		while((entry = entry.next) != null)
		{
			appender.accept(vc.append(separator), entry.element());
		}
		return vc;
	}

	@Override
	public final VarString appendTo(final VarString vc, final BiConsumer<VarString, ? super E> appender, final String separator)
	{
		if(separator == null || separator.isEmpty())
		{
			return this.appendTo(vc, appender);
		}

		EN entry;
		if((entry = this.head.next) == null)
		{
			return vc;
		}
		appender.accept(vc, entry.element());
		final char[] sepp = XChars.readChars(separator);
		while((entry = entry.next) != null)
		{
			appender.accept(vc.add(sepp), entry.element());
		}
		return vc;
	}

	@Override
	public final String toString()
	{
		final VarString vc = VarString.New((int)(XTypes.to_int(this.parent.size()) * 5.0f));
		for(EN e = this.head.next; e != null; e = e.next)
		{
			vc.append('(').add(e.element()).add(')', '-');
		}
		if(vc.isEmpty())
		{
			vc.add('(', ')');
		}
		else
		{
			vc.deleteLast();
		}
		return vc.toString();
	}



	///////////////////////////////////////////////////////////////////////////
	// removing //
	/////////////

	// removing - indexed //

	@Override
	public final E remove(final long index)
	{
		final EN e;
		(e = this.getChainEntry(index)).removeFrom(this.parent);
		return e.element();
	}

	// removing - null //

	@Override
	public final long removeNull()
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.hasNullElement())
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	// removing - one single //

	@Override
	public final E retrieve(final E element)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				e.removeFrom(parent);
				return e.element();
			}
		}
		return null;
	}


	@Override
	public final E retrieve(final E sample, final Equalator<? super E> equalator)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				e.removeFrom(parent);
				return e.element();
			}
		}
		return null;
	}

	@Override
	public final E retrieve(final Predicate<? super E> predicate)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.element()))
			{
				e.removeFrom(parent);
				return e.element();
			}
		}
		return null;
	}

	@Override
	public final boolean removeOne(final E element)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				e.removeFrom(parent);
				return true;
			}
		}
		return false;
	}

	@Override
	public final boolean removeOne(final E sample, final Equalator<? super E> equalator)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				e.removeFrom(parent);
				return true;
			}
		}
		return false;
	}



	// removing - multiple single //

	@Override
	public final long remove(final E element)
	{
		final int oldSize = XTypes.to_int(this.parent.size());
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - XTypes.to_int(this.parent.size());
	}

	@Override
	public final long remove(final E sample, final Equalator<? super E> equalator)
	{
		final int oldSize = XTypes.to_int(this.parent.size());
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(equalator.equal(e.element(), sample))
			{
				e.removeFrom(parent);
			}
		}
		return oldSize - XTypes.to_int(this.parent.size());
	}

	// removing - multiple all array //

	@Override
	public final long removeAll(final E[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}
		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.removeFrom(parent);
					removeCount++;
					break;
				}
			}
		}
		return removeCount;
	}

	// removing - multiple all collection //

	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.removeAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size())
			);
		}

		return elements.iterate(new Consumer<E>()
		{
			int removeCount;

			@Override
			public void accept(final E e)
			{
				this.removeCount += ChainStorageStrong.this.remove(e);
			}

		}).removeCount;
	}



	// removing - duplicates //

	@Override
	public final long removeDuplicates()
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(element == lookAhead.element())
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(EN lookAhead = e.next; lookAhead != null; lookAhead = lookAhead.next)
			{
				if(equalator.equal(element, lookAhead.element()))
				{
					lookAhead.removeFrom(parent);
					removeCount++;
				}
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	// reducing //
	/////////////

	// reducing - predicate //

	@Override
	public final long reduce(final Predicate<? super E> predicate)
	{
		int removeCount = 0;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.element()))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	// retaining //
	//////////////

	// retaining - array //

	@Override
	public final long retainAll(final E[] elements, final int elementsOffset, final int elementsLength)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}
		final int elementsBound = elementsOffset + elementsLength;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	public final int retainAll(
		final E[] samples,
		final int samplesOffset,
		final int samplesLength,
		final Equalator<? super E> equalator
	)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(samples, samplesOffset, samplesLength)) == 0)
		{
			return 0;
		}
		final int samplesBound = samplesOffset + samplesLength;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		int removeCount = 0;
		main:
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(int i = samplesOffset; i != samplesBound; i += d)
			{
				if(equalator.equal(element, samples[i]))
				{
					continue main;
				}
			}
			e.removeFrom(parent);
			removeCount++;
		}
		return removeCount;
	}

	// retaining - collection //

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.retainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size())
			);
		}

		final ElementIsContained<E> currentElement = new ElementIsContained<>();
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			currentElement.element = e.element();
			if(!elements.containsSearched(currentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples instanceof AbstractSimpleArrayCollection<?>)
		{
			// directly check array against array without predicate function or method calls
			return this.retainAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)samples),
				0,
				XTypes.to_int(samples.size()),
				equalator
			);
		}

		final CachedSampleEquality<E> equalCurrentElement = new CachedSampleEquality<>(equalator);
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		int removeCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			equalCurrentElement.sample = e.element();
			if(!samples.containsSearched(equalCurrentElement))
			{
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}



	///////////////////////////////////////////////////////////////////////////
	// processing //
	///////////////

	@Override
	public final long process(final Consumer<? super E> procedure)
	{
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		int removeCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				procedure.accept(e.element());
				e.removeFrom(parent);
				removeCount++;
			}
		}
		catch(final ThrowBreak b)
		{
			removeCount += parent.internalClear();
		}
		return removeCount;
	}




	///////////////////////////////////////////////////////////////////////////
	// moving //
	///////////

	@Override
	public final long moveRange(long offset, final long length, final Consumer<? super E> target)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return 0;
		}
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		final long bound = offset + length;

		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				target.accept(e.element());
				e.removeFrom(parent);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				target.accept(e.element());
				e.removeFrom(parent);
			}
		}
		return length < 0 ? -length : length;
	}

	@Override
	public final long moveSelection(final Consumer<? super E> target, final long... indices)
	{
		final int indicesLength = indices.length, size = XTypes.to_int(this.parent.size());

		// validate all indices before copying the first element
		for(int i = 0; i < indicesLength; i++)
		{
			if(indices[i] < 0 || indices[i] >= size)
			{
				throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indices[i]));
			}
		}

		final AbstractChainCollection<E, K, V, EN> parent = this.parent;

		// actual copying. Note: can't sort indices as copying order might be relevant
		EN e;
		for(int i = 0; i < indicesLength; i++)
		{
			target.accept((e = this.getChainEntry(indices[i])).element()); // pretty inefficient scrolling here
			e.removeFrom(parent); // remove not until adding to target has been successful
		}

		return indicesLength; // removeCount is equal to index count if no exception occured
	}

	// moving - conditional //

	@Override
	public final long moveTo(final Consumer<? super E> target, final Predicate<? super E> predicate)
	{
		int removeCount = 0;
		final AbstractChainCollection<E, K, V, EN> parent = this.parent;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element;
			if(predicate.test(element = e.element()))
			{
				target.accept(element);
				e.removeFrom(parent);
				removeCount++;
			}
		}
		return removeCount;
	}

	


	///////////////////////////////////////////////////////////////////////////
	// sorting //
	////////////

	@Override
	public final void sort(final Comparator<? super E> comparator)
	{
		// validate comparator before the chain gets splitted
		if(comparator == null)
		{
			throw new NullPointerException();
		}
		if(XTypes.to_int(this.parent.size()) <= 1)
		{
			return; // empty or trivial chain is always sorted
		}
		mergesortHead(this.head, comparator);
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		EN e;
		if((e = this.head.next) == null)
		{
			return true; // empty chain is sorted
		}

		E loopLastElement = e.element();
		while((e = e.next) != null)
		{
			final E element;
			if(comparator.compare(loopLastElement, element = e.element()) > 0)
			{
				return false;
			}
			loopLastElement = element;
		}
		return true;
	}
	
	@Override
	public final void shuffle()
	{
		// (04.04.2016 TM)NOTE: chain storage shuffling is currently not correct (and has never been used/tested/needed)
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME ChainStorageStrong#shuffle()
	}



	///////////////////////////////////////////////////////////////////////////
	// setting //
	////////////

	@Override
	public final void set(final long offset, final E[] elements)
	{
		EN e = this.getRangeChainEntry(offset, elements.length);
		for(int i = 0; i < elements.length; i++)
		{
			e.setElement0(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void set(final long offset, final E[] elements, final int elementsOffset, final int elementsLength)
	{
		EN e = this.getRangeChainEntry(offset, elementsLength);
		final int d = XArrays.validateArrayRange(elements, elementsOffset, elementsLength);
		for(int i = elementsOffset, bound = elementsOffset + elementsLength; i != bound; i += d)
		{
			e.setElement0(elements[i]);
			e = e.next;
		}
	}

	@Override
	public final void fill(long offset, final long length, final E element)
	{
		EN e;
		if((e = this.getRangeChainEntry(offset, length)) == null)
		{
			return;
		}
		final long bound = offset + length;
		if(length > 0)
		{
			for(; offset != bound; e = e.next, offset++)
			{
				e.setElement0(element);
			}
		}
		else
		{
			for(; offset != bound; e = e.prev, offset--)
			{
				e.setElement0(element);
			}
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// replacing //
	//////////////

	// replacing - one single //

	@Override
	public final long replaceOne(final E element, final E replacement)
	{
		int i = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				e.setElement0(replacement);
				return i;
			}
			i++;
		}
		return -1;
	}

	// replacing - multiple single //

	@Override
	public final long replace(final E element, final E replacement)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(e.element() == element)
			{
				e.setElement0(replacement);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	// replacing - multiple all array //

	@Override
	public final long replaceAll(final E[] elements, final int elementsOffset, final int elementsLength, final E replacement)
	{
		final int d;
		if((d = ChainStorageStrong.validateArrayIteration(elements, elementsOffset, elementsLength)) == 0)
		{
			return 0;
		}
		final int elementsBound = elementsOffset + elementsLength;

		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E element = e.element();
			for(int i = elementsOffset; i != elementsBound; i += d)
			{
				if(element == elements[i])
				{
					e.setElement0(replacement);
					replaceCount++;
					break;
				}
			}
		}
		return replaceCount;
	}

	// replacing - multiple all collection //

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		if(elements instanceof AbstractSimpleArrayCollection<?>)
		{
			return this.replaceAll(
				AbstractSimpleArrayCollection.internalGetStorageArray((AbstractSimpleArrayCollection<?>)elements),
				0,
				XTypes.to_int(elements.size()),
				replacement
			);
		}

		return elements.iterate(new Consumer<E>()
		{
			int replaceCount;

			@Override
			public void accept(final E e)
			{
				this.replaceCount += ChainStorageStrong.this.replace(e, replacement);
			}

		}).replaceCount;
	}



	///////////////////////////////////////////////////////////////////////////
	// substituting //
	/////////////////

	@Override
	public final long replaceOneBy(final Predicate<? super E> predicate, final E substitute)
	{
		try
		{
			int i = 0;
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					e.setElement0(substitute);
					return i;
				}
				i++;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return -1;
	}

	// substituting - multiple //

	@Override
	public final long replaceBy(final Predicate<? super E> predicate, final E substitute)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			if(predicate.test(e.element()))
			{
				e.setElement0(substitute);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	// replacing - mapped //

	@Override
	public final long substitute(final Function<E, E> mapper)
	{
		int replaceCount = 0;
		for(EN e = this.head.next; e != null; e = e.next)
		{
			final E replacement;
			if((replacement = mapper.apply(e.element())) != e.element())
			{
				e.setElement0(replacement);
				replaceCount++;
			}
		}
		return replaceCount;
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		int replaceCount = 0;
		try
		{
			for(EN e = this.head.next; e != null; e = e.next)
			{
				if(predicate.test(e.element()))
				{
					e.setElement0(mapper.apply(e.element()));
					replaceCount++;
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		return replaceCount;
	}

	

	///////////////////////////////////////////////////////////////////////////
	// inner classes //
	//////////////////

	final class KeyItr implements Iterator<E>
	{
		private EN current = ChainStorageStrong.this.head;

		@Override
		public final boolean hasNext()
		{
			return this.current.next != null;
		}

		@Override
		public final E next()
		{
			if(this.current.next == null)
			{
				throw new NoSuchElementException();
			}
			return (this.current = this.current.next).element();
		}

		@Override
		public final void remove()
		{
			/* (02.12.2011 TM)NOTE:
			 * Dropped support for removal stuff because it would prevent using the iterator in read-only delegates.
			 * As it is an optional operation, no proper code can rely on it anyway and tbh: just use proper
			 * internal iteration means in the first place.
			 */
			throw new UnsupportedOperationException();
		}

	}



	///////////////////////////////////////////////////////////////////////////
	// swapping //
	/////////////

	@Override
	public final void swap(final long indexA, final long indexB)
	{
		final long size;
		if(indexA >= (size = this.parent.size()) || indexA < 0)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indexA));
		}
		if(indexB < 0 || indexB >= size)
		{
			throw new IndexOutOfBoundsException(exceptionIndexOutOfBounds(size, indexB));
		}

		EN eA, eB;
		long i;
		if((i = indexA - indexB) >= 0)
		{
			if(i == 0)
			{
				return;
			}
			else if(i < size >>> 1)
			{
				eB = eA = this.getChainEntry(indexB);
				while(i-- > 0)
				{
					eA = eA.next;
				}
			}
			else
			{
				eA = this.getChainEntry(indexA);
				eB = this.getChainEntry(indexB);
			}
		}
		else
		{
			if(-i < size >>> 1)
			{
				eB = eA = this.getChainEntry(indexA);
				while(i++ < 0)
				{
					eB = eB.next;
				}
			}
			else
			{
				eA = this.getChainEntry(indexA);
				eB = this.getChainEntry(indexB);
			}
		}
		this.swapEntries(eA, eB);
	}

	@Override
	public final void swap(long indexA, long indexB, final long length)
	{
		if(length == 0 || indexA == indexB)
		{
			return;
		}
		else if(indexA > indexB)
		{
			final long t = indexA;
			indexA = indexB;
			indexB = t;
		}

		final long bound;
		if(length < 0 || (bound = indexA + length) >= indexB)
		{
			throw new IndexOutOfBoundsException(exceptionIllegalSwapBounds(indexA, indexB, length));
		}

		EN eA, eB, nextA, nextB;
		eB = this.getRangeChainEntry(indexB, length);
		eA = this.getChainEntry(indexA);

		for(; indexA < bound; indexA++)
		{
			nextA = eA.next;
			nextB = eB.next;
			this.swapEntries(eA, eB);
			eA = nextA;
			eB = nextB;
		}
	}

	@Override
	public final void reverse()
	{
		EN eA, eB, nextA, nextB;
		if((eA = this.head.next) == null)
		{
			return;
		}
		eB = this.head.prev;
		for(int i = XTypes.to_int(this.parent.size()) >>> 1; i != 0; i--)
		{
			nextA = eA.next;
			nextB = eB.prev;
			this.swapEntries(eA, eB);
			eA = nextA;
			eB = nextB;
		}
	}



	///////////////////////////////////////////////////////////////////////////
	// inner classes //
	//////////////////

	final class Itr implements Iterator<E>
	{
		// first is head dummy entry
		private EN current = ChainStorageStrong.this.head;

		@Override
		public final boolean hasNext()
		{
			return this.current.next != null;
		}

		@Override
		public final E next()
		{
			return (this.current = this.current.next).element();
		}

		@Override
		public final void remove()
		{
			// fu overcomplicated operations in silly Iterator concept
			throw new UnsupportedOperationException();
		}

	}

}

