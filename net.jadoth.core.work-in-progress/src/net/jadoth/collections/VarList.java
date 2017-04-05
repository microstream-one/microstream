package net.jadoth.collections;

import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import net.jadoth.collections.old.OldList;
import net.jadoth.collections.types.IdentityEqualityLogic;
import net.jadoth.collections.types.XGettingCollection;
import net.jadoth.collections.types.XGettingList;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.collections.types.XList;
import net.jadoth.exceptions.IndexBoundsException;
import net.jadoth.functional.IndexProcedure;
import net.jadoth.hash.JadothHash;
import net.jadoth.math.JadothMath;
import net.jadoth.util.Composition;
import net.jadoth.util.Equalator;
import net.jadoth.util.branching.ThrowBreak;


/**
 * This implementation is intended to be a general purpose ("care free") list, combining the flexibility of a
 * linked list with the efficiency of an array-backed list.<p>
 * More specifically:<br>
 * - good random index insertion performance
 * - good prepend and append/add performance
 * - high memory efficiency / low memory overhead
 * - low rebuild cost
 * - high iteration performance due to few cache misses
 *
 * @author Thomas Muenz
 *
 * @param <E>
 */
public final class VarList<E> implements Composition, XList<E>, IdentityEqualityLogic
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/*
	 * Intentionally no minimum segment length because of the following rationale:
	 * While anything below 8 or so (or actually 500) makes the concept very inefficient and overhead
	 * pretty high, there are reasons against it:
	 * - It is technically unnecessary
	 * - Some application might need to create a lot of instances but only fill some of them.
	 */

	// 500 means one memory page of usual size (4096) can keep one segment with 8 byte references plus overhead.
	static final int DEFAULT_SEGMENT_LENGTH = 500;



	///////////////////////////////////////////////////////////////////////////
	// static methods   //
	/////////////////////

	@SuppressWarnings("unchecked")
	static final <E> E[] newArray(final int size)
	{
		return (E[])new Object[size];
	}



	public final static <E> VarList<E> New()
	{
		return NewCustom(DEFAULT_SEGMENT_LENGTH);
	}

	public final static <E> VarList<E> New(final long initialCapacity)
	{
		final VarList<E> newInstance = New();

		return newInstance.ensureCapacity(initialCapacity);
	}

	public final static <E> VarList<E> NewCustom(final int segmentLength)
	{
		final VarList<E> newInstance = new VarList<>(
			JadothMath.positive(segmentLength)
		);
		newInstance.initializeEmpty();

		return newInstance;
	}

	public final static <E> VarList<E> NewCustom(final long initialCapacity, final int segmentLength)
	{
		final VarList<E> newInstance = New(segmentLength);

		return newInstance.ensureCapacity(initialCapacity);
	}



	static final class Segment<E>
	{
		      int        size      ;
		      Segment<E> prev, next;
		final E[]        elements  ;

		Segment(final int capacity, final Segment<E> prev, final Segment<E> next)
		{
			super();
			this.size = 0;
			this.prev = prev;
			this.next = next;
			this.elements = newArray(capacity);
		}

	}


	///////////////////////////////////////////////////////////////////////////
	// instance fields  //
	/////////////////////

	private final int        segmentLength             ;
	              int        headSize, tailSize        ;
	              long       restSize, capacity        ;
	              Segment<E> head, tail                ;
	              E[]        headElements, tailElements;



	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	VarList(final int segmentLength)
	{
		super();
		this.segmentLength = segmentLength;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	final void initializeEmpty()
	{
		final Segment<E> head = new Segment<>(this.segmentLength, null, null);
		final Segment<E> tail = new Segment<>(this.segmentLength, head, null);
		head.next = tail;

		this.initialize(0, 2 * this.segmentLength, head, tail);
	}

	final void initialize(
		final long       restSize,
		final long       capacity,
		final Segment<E> head    ,
		final Segment<E> tail
	)
	{
		this.restSize      = restSize     ;
		this.head          = head         ;
		this.tail          = tail         ;
		this.headElements  = head.elements;
		this.tailElements  = tail.elements;
		this.headSize      = head.size    ;
		this.tailSize      = tail.size    ;
		this.capacity      = capacity     ;
	}

	final void appendSegment()
	{
		// inlined assignments for performance reasons
		this.tailElements = (this.tail = this.tail.next = new Segment<>(this.segmentLength, this.tail, null)).elements;
		this.restSize += this.tailSize;
		this.capacity += this.segmentLength;
		this.tailSize = 0;
	}

	final void prependSegment()
	{
		// inlined assignments for performance reasons
		this.headElements = (this.head = this.head.prev = new Segment<>(this.segmentLength, null, this.head)).elements;
		this.restSize += this.headSize;
		this.capacity += this.segmentLength;
		this.headSize = 0;
	}

	final void internalAdd(final E element)
	{
		if(this.tailSize >= this.segmentLength)
		{
			this.appendSegment();
		}

		this.tailElements[this.tailSize++] = element;
	}

	final void internalPrepend(final E element)
	{
		if(this.headSize >= this.segmentLength)
		{
			this.prependSegment();
		}
		else
		{
			System.arraycopy(this.headElements, 0, this.headElements, 1, this.headSize);
		}
		this.headElements[0] = element;
		this.headSize++;
	}

	final void internalValidateIndex(final long index)
	{
		if(index >= 0 && index < this.size())
		{
			// index is inside bounds
			return;
		}

		// index is outside bounds
		throw new IndexBoundsException(this.size(), index);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public final E at(final long index)
	{
		return index < this.size() >>> 1
			? this.atLow(index)
			: this.atHigh(index)
		;
	}

	public final E atLow(final long lowIndex)
	{
		this.internalValidateIndex(lowIndex);

		long idx = lowIndex;
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			if(idx < segment.size)
			{
				return segment.elements[(int)idx];
			}
			idx -= segment.size;
		}

		// getting here means an error in the logic above
		throw new Error();
	}

	public final E atHigh(final long highIndex)
	{
		this.internalValidateIndex(highIndex);

		long idx = highIndex;
		for(Segment<E> segment = this.tail; segment != null; segment = segment.prev)
		{
			if(idx < segment.size)
			{
				return segment.elements[(int)idx];
			}
			idx -= segment.size;
		}

		// getting here means an error in the logic above
		throw new Error();
	}


	@Override
	public final E first()
	{
		if(this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}
		return this.headElements[0];
	}

	@Override
	public final E last()
	{
		// simple case: the tail segment is not empty
		if(this.tailSize > 0)
		{
			return this.tailElements[this.tailSize - 1];
		}

		// for the complex case, first check if the collection contains any elements at all
		if(this.restSize == 0 && this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}

		// complex (and rather rare) case. Moved to a separate method to help hotspot optimizing.
		return this.complexLast();
	}

	private E complexLast()
	{
		// complex case: search for the applicable segment
		Segment<E> s = this.tail;
		while(s.size == 0)
		{
			// the check above guarantees that a NPE can never happen here. If it does, the list was inconsistent.
			s = s.prev;
		}

		return s.elements[s.size - 1];
	}

	@Override
	public final E poll()
	{
		return this.headSize == 0
			? null
			: this.headElements[0]
		;
	}

	@Override
	public final E peek()
	{
		// simple case: the tail segment is not empty
		if(this.tailSize > 0)
		{
			return this.tailElements[this.tailSize - 1];
		}

		// for the complex case, first check if the collection contains any elements at all
		if(this.restSize == 0 && this.headSize == 0)
		{
			return null;
		}

		// complex (and rather rare) case. Moved to a separate method to help hotspot optimizing.
		return this.complexPeek();
	}

	private E complexPeek()
	{
		// complex case: search for the applicable segment
		Segment<E> s = this.tail;
		while(s.size == 0)
		{
			// the check above guarantees that a NPE can never happen here. If it does, the list was inconsistent.
			s = s.prev;
		}

		return s.elements[s.size - 1];
	}

	@Override
	public final void accept(final E element)
	{
		this.internalAdd(element);
	}

	@Override
	public final boolean add(final E element)
	{
		this.internalAdd(element);
		return true;
	}

	@Override
	public final boolean nullAdd()
	{
		this.internalAdd(null);
		return true;
	}

	@SafeVarargs
	@Override
	public final VarList<E> addAll(final E... elements)
	{
		for(final E e : elements)
		{
			this.internalAdd(e);
		}

		return this;
	}

	@Override
	public final VarList<E> addAll(final E[] elements, final int offset, final int length)
	{
		final int bound = offset + length;
		JadothArrays.checkBounds(elements, offset, bound);

		for(int i = offset; i < bound; i++)
		{
			this.internalAdd(elements[i]);
		}

		return this;
	}

	@Override
	public final VarList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(this);
		return this;
	}

	@SafeVarargs
	@Override
	public final VarList<E> putAll(final E... elements)
	{
		this.addAll(elements);
		return this;
	}

	@Override
	public final VarList<E> putAll(final E[] elements, final int offset, final int length)
	{
		this.addAll(elements, offset, length);
		return this;
	}

	@Override
	public final VarList<E> putAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(this);
		return this;
	}

	@SafeVarargs
	@Override
	public final VarList<E> prependAll(final E... elements)
	{
		for(final E e : elements)
		{
			this.internalPrepend(e);
		}

		return this;
	}

	@Override
	public final VarList<E> prependAll(final E[] elements, final int offset, final int length)
	{
		final int bound = offset + length;
		JadothArrays.checkBounds(elements, offset, bound);

		for(int i = offset; i < bound; i++)
		{
			this.internalPrepend(elements[i]);
		}

		return this;
	}

	@Override
	public final VarList<E> prependAll(final XGettingCollection<? extends E> elements)
	{
		elements.iterate(this::internalPrepend);
		return this;
	}

	@SafeVarargs
	@Override
	public final VarList<E> preputAll(final E... elements)
	{
		this.prependAll(elements);
		return this;
	}

	@Override
	public final VarList<E> preputAll(final E[] elements, final int offset, final int length)
	{
		this.prependAll(elements, offset, length);
		return this;
	}

	@Override
	public final VarList<E> preputAll(final XGettingCollection<? extends E> elements)
	{
		this.prependAll(elements);
		return this;
	}

	@Override
	public final boolean nullAllowed()
	{
		return true;
	}

	@Override
	public final boolean hasVolatileElements()
	{
		return false;
	}

	@Override
	public final VarList<E> ensureFreeCapacity(final long minimalFreeCapacity)
	{
		this.ensureCapacity(this.size() + minimalFreeCapacity);
		return this;
	}

	@Override
	public final VarList<E> ensureCapacity(final long minimalCapacity)
	{
		while(this.capacity < minimalCapacity)
		{
			this.appendSegment();
		}

		return this;
	}

	@Override
	public final long currentCapacity()
	{
		return this.capacity;
	}

	@Override
	public final long maximumCapacity()
	{
		return Long.MAX_VALUE;
	}

	@Override
	public final long remainingCapacity()
	{
		return this.maximumCapacity() - this.size();
	}

	@Override
	public final long size()
	{
		return this.headSize + this.restSize + this.tailSize;
	}

	@Override
	public final boolean isEmpty()
	{
		// this is faster than adding all values and then checking for 0.
		return this.headSize == 0 && this.restSize == 0 && this.tailSize == 0;
	}

	@Override
	public final long optimize()
	{
		// (25.11.2016 TM)TODO: test!

		final int segmentLength = this.segmentLength;

		Segment<E> target = this.head;

		master:
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			while(target.size < segmentLength)
			{
				if(target == segment)
				{
					continue master;
				}

				if(segmentLength - target.size < segment.size)
				{
					if(target.size < segmentLength)
					{
						System.arraycopy(segment.elements, 0, target.elements, target.size, segmentLength - target.size);
						segment.size -= segmentLength - target.size;
						target.size = segmentLength;
					}
					target = target.next;
				}
				else
				{
					if(segment.size > 0)
					{
						System.arraycopy(segment.elements, 0, target.elements, target.size, segment.size);
						target.size += segment.size;
						segment.size = 0;
					}
					continue master;
				}
			}
		}

		if(target != this.tail)
		{
			long truncatedCapacity = 0;

			for(Segment<E> s = target; (s = s.next) != null; )
			{
				truncatedCapacity += segmentLength;
			}

			this.tailElements = (this.tail = target).elements;
			this.tailSize = target.size;
			target.next = null;
			this.capacity -= truncatedCapacity;
		}

		return this.capacity;
	}

	@Override
	public final boolean put(final E element)
	{
		this.internalAdd(element);
		return true;
	}

	@Override
	public final boolean nullPut()
	{
		this.internalAdd(null);
		return true;
	}

	@Override
	public final E get()
	{
		if(this.isEmpty())
		{
			throw new NoSuchElementException();
		}
		return this.headElements[0];
	}

	final class SegmentIterator implements Iterator<E>
	{
		Segment<E> currentSegment  = VarList.this.head;
		E[]        currentElements = this.currentSegment.elements;
		int        currentIndex    = 0;
		int        currentSize     = this.currentSegment.size;

		@Override
		public boolean hasNext()
		{
			return this.currentIndex < this.currentSize;
		}

		@Override
		public E next()
		{
			final E element = this.currentElements[this.currentIndex];

			if(++this.currentIndex >= this.currentSize && this.currentSegment.next != null)
			{
				this.currentSegment = this.currentSegment.next;
				this.currentElements = this.currentSegment.elements;
				this.currentSize = this.currentSegment.size;
				this.currentIndex = 0;
			}

			return element;
		}

	}

	@Override
	public final Iterator<E> iterator()
	{
		return new SegmentIterator();
	}

	@Override
	public final Equalator<? super E> equality()
	{
		return JadothHash.hashEqualityIdentity();
	}

	@Override
	public final boolean equals(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == null || !(samples instanceof VarList<?>))
		{
			return false;
		}

		// this check inside
		return this.equalsContent(samples, equalator);
	}

	@Override
	public final boolean equalsContent(final XGettingCollection<? extends E> samples, final Equalator<? super E> equalator)
	{
		if(samples == this)
		{
			return true;
		}

		// quick-check for equal size.
		if(this.size() != samples.size())
		{
			return false;
		}

		final Iterator<? extends E> sampleIterator = samples.iterator();

		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(!sampleIterator.hasNext())
				{
					// can't have equal content if one collection has elements and the other one not
					return false;
				}

				if(!equalator.equal(elements[i], sampleIterator.next()))
				{
					// equalator found a mismatch
					return false;
				}
			}
		}

		// can't have equal content if one collection has elements and the other one not
		return !sampleIterator.hasNext();
	}

	@Override
	public final boolean nullContained()
	{
		return this.contains(null);
	}

	@Override
	public final boolean containsId(final E element)
	{
		return this.contains(element);
	}

	@Override
	public final boolean contains(final E element)
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(elements[i] == element)
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public final boolean containsSearched(final Predicate<? super E> predicate)
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(predicate.test(elements[i]))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public final boolean applies(final Predicate<? super E> predicate)
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(!predicate.test(elements[i]))
				{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public final long count(final E element)
	{
		long count = 0;
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(elements[i] == element)
				{
					count++;
				}
			}
		}

		return count;
	}

	@Override
	public final long countBy(final Predicate<? super E> predicate)
	{
		long count = 0;
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(predicate.test(elements[i]))
				{
					count++;
				}
			}
		}

		return count;
	}

	@Override
	public final E search(final Predicate<? super E> predicate)
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(predicate.test(elements[i]))
				{
					return elements[i];
				}
			}
		}

		return null;
	}

	@Override
	public final E seek(final E sample)
	{
		return this.contains(sample) ? sample : null;
	}

	@Override
	public final E max(final Comparator<? super E> comparator)
	{
		E currentMax = this.peek();

		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(comparator.compare(elements[i], currentMax) >= 0)
				{
					currentMax = elements[i];
				}
			}
		}

		return currentMax;
	}

	@Override
	public final E min(final Comparator<? super E> comparator)
	{
		E currentMin = this.peek();

		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(comparator.compare(elements[i], currentMin) < 0)
				{
					currentMin = elements[i];
				}
			}
		}

		return currentMin;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target)
	{
		final HashEnum<E> distincts = HashEnum.New();

		// manual loop for passing elements on the fly, not in the end
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(distincts.add(elements[i]))
				{
					target.accept(elements[i]);
				}
			}
		}

		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T distinct(final T target, final Equalator<? super E> equalator)
	{
		// a set would have to fully iterate as well for a custom equalator
		final E[] distincts = newArray(this.intSize());
		int k = 0;

		mainLoop:
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				final E element = elements[i];
				for(int j = 0; j < k; j++)
				{
					if(equalator.equal(distincts[j], element))
					{
						continue mainLoop;
					}
				}
				distincts[k++] = element;
				target.accept(element);
			}
		}

		return target;
	}

	@Override
	public final <T extends Consumer<? super E>> T copyTo(final T target)
	{
		return this.iterate(target);
	}

	@Override
	public final <T extends Consumer<? super E>> T filterTo(final T target, final Predicate<? super E> predicate)
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(predicate.test(elements[i]))
				{
					target.accept(elements[i]);
				}
			}
		}

		return target;
	}

	@Override
	public final <P extends Consumer<? super E>> P iterate(final P procedure)
	{
		try
		{
			for(Segment<E> segment = this.head; segment != null; segment = segment.next)
			{
				final int bound    = segment.size    ;
				final E[] elements = segment.elements;
				for(int i = 0; i < bound; i++)
				{
					procedure.accept(elements[i]);
				}
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}

		return procedure;
	}

	@Override
	public final <IP extends IndexProcedure<? super E>> IP iterateIndexed(final IP procedure)
	{
		try
		{
			long index = 0;
			for(Segment<E> segment = this.head; segment != null; segment = segment.next)
			{
				final int bound    = segment.size    ;
				final E[] elements = segment.elements;
				for(int i = 0; i < bound; i++)
				{
					procedure.accept(elements[i], index + i);
				}
				index += bound;
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}

		return procedure;
	}

	@Override
	public final void clear()
	{
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				elements[i] = null;
			}
			segment.size = 0;
		}

		this.headSize = 0;
		this.tailSize = 0;
	}

	@Override
	public final void truncate()
	{
		this.initializeEmpty();
	}

	@Override
	public final long consolidate()
	{
		return 0;
	}

	@Override
	public final E fetch()
	{
		if(this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}

		final E firstElement = this.headElements[0];
		System.arraycopy(this.headElements, 1, this.headElements, 0, this.headSize - 1);
		this.headSize--;

		return firstElement;
	}

	@Override
	public final E pinch()
	{
		// simple case: the tail segment is not empty
		if(this.tailSize > 0)
		{
			return this.tailElements[--this.tailSize];
		}

		// for the complex case, first check if the collection contains any elements at all
		if(this.restSize == 0 && this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}

		// complex (and rather rare) case. Moved to a separate method to help hotspot optimizing.
		return this.complexPinch();
	}

	private E complexPinch()
	{
		// complex case: search for the applicable segment
		Segment<E> s = this.tail;
		while(s.size == 0)
		{
			// the check above guarantees that a NPE can never happen here. If it does, the list was inconsistent.
			s = s.prev;
		}

		return s.elements[--s.size];
	}

	@Override
	public final long nullRemove()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#nullRemove()
	}

	@Override
	public final boolean removeOne(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeOne()
	}

	@Override
	public final long remove(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#remove()
	}


	@Override
	public final long removeAll(final XGettingCollection<? extends E> elements)
	{
		if(elements.isEmpty())
		{
			return 0;
		}

		// use a random (the "first") element to be removed as the remove marker, may even be null
//		final E removeMarker = elements.get();

		// for each segment
		// from start to end
		// while element fits increment copy source offset, increment shrinkcount
		// while element not fits increment copy range
		// execute copy
		// if shrink count equals segment size (segment is empty), remove segment. Otherwise shink segment size

		// what about previous segment with spare slots? Could be consolidated on the fly

		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeAll()
	}

	@Override
	public final long retainAll(final XGettingCollection<? extends E> elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#retainAll()
	}

	@Override
	public final long removeDuplicates()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingCollection<E>#removeDuplicates()
	}

	@Override
	public final E retrieve(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#retrieve()
	}

	@Override
	public final E retrieveBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#retrieve()
	}

	@Override
	public final long removeDuplicates(final Equalator<? super E> equalator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#removeDuplicates()
	}

	@Override
	public final long removeBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#remove()
	}

	@Override
	public final <C extends Consumer<? super E>> C moveTo(final C target, final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingCollection<E>#moveTo()
	}

	@Override
	public final <P extends Consumer<? super E>> P process(final P processor)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME Processable<E>#process()
	}

	@Override
	public final long maxIndex(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#maxIndex()
	}

	@Override
	public final long minIndex(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#minIndex()
	}

	@Override
	public final long indexOf(final E element)
	{
		long idx = 0;
		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
		{
			final int bound    = segment.size    ;
			final E[] elements = segment.elements;
			for(int i = 0; i < bound; i++)
			{
				if(elements[i] == element)
				{
					return (int)(idx + i);
				}
			}
			idx += bound;
		}

		return -1;
	}

	@Override
	public final long indexBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#indexOf()
	}

	@Override
	public final long lastIndexOf(final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public final long lastIndexBy(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#lastIndexOf()
	}

	@Override
	public final long scan(final Predicate<? super E> predicate)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#scan()
	}

	@Override
	public final boolean isSorted(final Comparator<? super E> comparator)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#isSorted()
	}

	@Override
	public final <T extends Consumer<? super E>> T copySelection(final T target, final long... indices)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingSequence<E>#copySelection()
	}

	@Override
	public final E removeAt(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#remove()
	}

	@Override
	public final E pop()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#pop()
	}

	@Override
	public final E pick()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#pick()
	}

	@Override
	public final <C extends Consumer<? super E>> C moveSelection(final C target, final long... indices)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XProcessingSequence<E>#moveSelection()
	}

	@Override
	public final VarList<E> removeRange(final long offset, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingSequence<E>#removeRange()
	}

	@Override
	public final long removeSelection(final long[] indices)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XRemovingSequence<E>#removeSelection()
	}

	@Override
	public final ConstList<E> immure()
	{
		return ConstList.New(this);
	}

	@Override
	public final ListIterator<E> listIterator()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator()
	}

	@Override
	public final ListIterator<E> listIterator(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingList<E>#listIterator()
	}

	@Override
	public final OldList<E> old()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingList<E>#old()
	}

	@Override
	public final XGettingList<E> view()
	{
		return new ListView<>(this);
	}

	@Override
	public final XGettingList<E> view(final long lowIndex, final long highIndex)
	{
		return new SubListView<>(this, lowIndex, highIndex); // range check is done in constructor
	}

	@Override
	public final boolean input(final long index, final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#input()
	}

	@Override
	public final boolean nullInput(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#nullInput()
	}

	@SafeVarargs
	@Override
	public final long inputAll(final long index, final E... elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#inputAll()
	}

	@Override
	public final long inputAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#inputAll()
	}

	@Override
	public final long inputAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInputtingSequence<E>#inputAll()
	}

	@Override
	public final boolean insert(final long index, final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInsertingSequence<E>#insert()
	}

	@Override
	public final boolean nullInsert(final long index)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInsertingSequence<E>#nullInsert()
	}

	@SafeVarargs
	@Override
	public final long insertAll(final long index, final E... elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInsertingSequence<E>#insertAll()
	}

	@Override
	public final long insertAll(final long index, final E[] elements, final int offset, final int length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInsertingSequence<E>#insertAll()
	}

	@Override
	public final long insertAll(final long index, final XGettingCollection<? extends E> elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XInsertingSequence<E>#insertAll()
	}

	@Override
	public final boolean prepend(final E element)
	{
		this.internalPrepend(element);
		return true;
	}

	@Override
	public final boolean nullPrepend()
	{
		this.internalPrepend(null);
		return true;
	}

	@Override
	public final boolean preput(final E element)
	{
		this.internalPrepend(element);
		return true;
	}

	@Override
	public final boolean nullPreput()
	{
		this.internalPrepend(null);
		return true;
	}

	@Override
	public final boolean replaceOne(final E element, final E replacement)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#replaceOne()
	}

	@Override
	public final long replace(final E element, final E replacement)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#replace()
	}

	@Override
	public final long replaceAll(final XGettingCollection<? extends E> elements, final E replacement)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#replaceAll()
	}

	@Override
	public final boolean replaceOne(final Predicate<? super E> predicate, final E substitute)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#replaceOne()
	}

	@Override
	public final long replace(final Predicate<? super E> predicate, final E substitute)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#replace()
	}

	@Override
	public final long substitute(final Function<E, E> mapper)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#modify()
	}

	@Override
	public final long substitute(final Predicate<? super E> predicate, final Function<E, E> mapper)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XReplacingBag<E>#modify()
	}

	@Override
	public final boolean set(final long index, final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#set()
	}

	@Override
	public final E setGet(final long index, final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XSettingSequence<E>#setGet()
	}

	@Override
	public final void setFirst(final E element)
	{
		if(this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}
		this.headElements[0] = element;
	}

	@Override
	public final void setLast(final E element)
	{
		// simple case: the tail segment is not empty
		if(this.tailSize > 0)
		{
			this.tailElements[this.tailSize - 1] = element;
			return;
		}

		// for the complex case, first check if the collection contains any elements at all
		if(this.restSize == 0 && this.headSize == 0)
		{
			throw new IndexBoundsException(0);
		}

		// complex (and rather rare) case. Moved to a separate method to help hotspot optimizing.
		this.complexSetLast(element);
	}

	private void complexSetLast(final E element)
	{
		// complex case: search for the applicable segment
		Segment<E> s = this.tail;
		while(s.size == 0)
		{
			// the check above guarantees that a NPE can never happen here. If it does, the list was inconsistent.
			s = s.prev;
		}
		s.elements[s.size - 1] = element;
	}

	@SafeVarargs
	@Override
	public final VarList<E> setAll(final long index, final E... elements)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#setAll()
	}

	@Override
	public final VarList<E> set(final long index, final E[] elements, final int offset, final int length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#set()
	}

	@Override
	public final VarList<E> set(final long index, final XGettingSequence<? extends E> elements, final long offset, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#set()
	}

	@Override
	public final VarList<E> swap(final long indexA, final long indexB)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#swap()
	}

	@Override
	public final VarList<E> swap(final long indexA, final long indexB, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#swap()
	}

	@Override
	public final VarList<E> retainRange(final long offset, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#retainRange()
	}

	@Override
	public final VarList<E> copy()
	{
		final VarList<E> copy = new VarList<E>(this.segmentLength)
			.addAll(this)
		;

		return copy;
	}

	@Override
	public final VarList<E> toReversed()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#toReversed()
	}

	@Override
	public final VarList<E> reverse()
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#reverse()
	}

	@Override
	public final VarList<E> range(final long fromIndex, final long toIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#range()
	}

	@Override
	public final VarList<E> fill(final long offset, final long length, final E element)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#fill()
	}

	@Override
	public final VarList<E> sort(final Comparator<? super E> comparator)
	{
		// check for trivial case before doing lots of work
		if(this.size() <= 1)
		{
			return this;
		}


		/* (25.01.2017 TM)TODO: optimize VarList sort
		 * An efficient sorting algorithm is not a trivial thing to implement for this data structure.
		 * For now, this rather inefficient and naive approach is used that required three times the instance's
		 * memory (yes, three times: local buffer array, merge sort buffer array, newly created segments at the end).
		 *
		 * See below for thoughts about a direct implementation without redundant copying.
		 */
		final E[] buffer2 = newArray(this.intSize());
		JadothArrays.copyTo(this, buffer2);
		JadothSort.sort(buffer2, comparator);
		this.truncate();
		this.addAll(buffer2);

		return this;

//		// sort every segment on its own
//		final E[] buffer = newArray(this.segmentLength);
//		for(Segment<E> segment = this.head; segment != null; segment = segment.next)
//		{
//			JadothSort.bufferedAdaptiveMergesort(buffer, segment.elements, 0, segment.size, comparator);
//		}
//
//		// once every segment is sorted in itself, a complete already-sorted check becomes trivial, so it is done.
//		alreadySortedCheck:
//		{
//			final E last = this.headElements[0];
//			for(Segment<E> segment = this.head; segment != null; segment = segment.next)
//			{
//				if(segment.size >= 1 && comparator.compare(last, segment.elements[0]) > 0)
//				{
//					break alreadySortedCheck;
//				}
//			}
//
//			// reaching this point means the list is already sorted
//			return this;
//		}
//
//		/* (09.05.2016) merge sorted segments properly.
//		 * This is not as simple as it might sound at first.
//		 * But given the amount of established structure in the data and the already existing buffer instance,
//		 * a good algorithm should exist. Maybe even O(n).
//		 *
//		 * Must be stable sorting.
//		 *
//		 * Ideas:
//		 * - Maybe pre-sort segments by comparing highest of one with lowest of the other.
//		 *   But that should hardly have much effect on random data ...
//		 */
//		throw new net.jadoth.meta.NotImplementedYetError();
	}

	@Override
	public final VarList<E> shiftTo(final long sourceIndex, final long targetIndex)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#shiftTo()
	}

	@Override
	public final VarList<E> shiftTo(final long sourceIndex, final long targetIndex, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#shiftTo()
	}

	@Override
	public final VarList<E> shiftBy(final long sourceIndex, final long distance)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#shiftBy()
	}

	@Override
	public final VarList<E> shiftBy(final long sourceIndex, final long distance, final long length)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XList<E>#shiftBy()
	}

	@Override
	public final <T extends Consumer<? super E>> T union(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#union()
	}

	@Override
	public final <T extends Consumer<? super E>> T intersect(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#intersect()
	}

	@Override
	public final <T extends Consumer<? super E>> T except(
		final XGettingCollection<? extends E> other    ,
		final Equalator<? super E>            equalator,
		final T                               target
	)
	{
		throw new net.jadoth.meta.NotImplementedYetError(); // FIXME XGettingCollection<E>#except()
	}

	@Override
	public final String toString()
	{
		return JadothCollections.toString(this);
	}

}
