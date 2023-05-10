package one.microstream.collections.lazy;

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

import static one.microstream.collections.AbstractExtendedCollection.validateIndex;
import static one.microstream.math.XMath.positive;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Spliterator;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;
import one.microstream.reference.ControlledLazyReference;
import one.microstream.reference.Lazy;
import one.microstream.reference.LazyClearController;


/**
 * {@link LazyList} implementation according to the specifications of {@link ArrayList}.
 * <p>
 * Lazy-loaded segments are used internally to achieve the partial loading.
 * The maximum size of these segments can be specified within the constructor.
 * <br>
 * This implementation requires an active microstream storage with specialized
 * type handlers. Without those handles a correct behavior is not guaranteed.
 * The required handlers are:
 * BinaryHandlerLazyArrayList
 * BinaryHandlerControlledLazy
 * <br>
 * This list tries to unload segments depending on the provided {@link LazySegmentUnloader}.
 * By default, it will use the {@link LazySegmentUnloader.Default} that keeps two segments loaded.
 *
 * @param <E> the type of elements in this collection
 */
public final class LazyArrayList<E> extends AbstractList<E> implements LazyList<E>, RandomAccess
{
	private static final int MAX_SEGMENT_SIZE_DEFAULT = 1000;
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	private final int                 maxSegmentSize;
	private final ArrayList<Segment>  segments      ;
	private int                       size          ;
	private final LazySegmentUnloader unloader      ;
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	
	/**
	 * Creates a new {@link LazyArrayList} with a maximum segment size of 1000.
	 */
	public LazyArrayList()
	{
		super();
		this.maxSegmentSize = MAX_SEGMENT_SIZE_DEFAULT         ;
		this.segments       = new ArrayList<>()                ;
		this.unloader       = new LazySegmentUnloader.Default();
	}
	
	
	/**
	 * Creates a new {@link LazyArrayList} with a defined maximum segment size.
	 * 
	 * @param maxSegmentSize maximum segment size, must be positive
	 */
	public LazyArrayList(final int maxSegmentSize)
	{
		super();
		this.maxSegmentSize = positive(maxSegmentSize)         ;
		this.segments       = new ArrayList<>()                ;
		this.unloader       = new LazySegmentUnloader.Default();
	}
	
	/**
	 * Creates a new {@link LazyArrayList} with a defined maximum segment size.
	 * 
	 * @param maxSegmentSize maximum segment size, must be positive
	 * @param lazySegmentUnloader LazySegmentUnloader instance
	 */
	public LazyArrayList(final int maxSegmentSize, final LazySegmentUnloader lazySegmentUnloader)
	{
		super();
		this.maxSegmentSize = positive(maxSegmentSize);
		this.segments       = new ArrayList<>()       ;
		this.unloader       = lazySegmentUnloader     ;
	}
	
	/**
	 * Creates a new copy from the supplied {@link LazyArrayList}.
	 * <br>
	 * The Objects of the source list will not be copied,
	 * both lists will reference the object instances.
	 * 
	 * @param list to be copied.
	 */
	public LazyArrayList(final LazyArrayList<E> list)
	{
		super();
		this.maxSegmentSize = list.maxSegmentSize ;
		this.segments       = new ArrayList<>()   ;
		this.unloader       = list.unloader.copy();
		this.addAll(list);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	/**
	 * Returns the current number of internal segments.
	 * 
	 * @return the current number of internal segments.
	 */
	public long getSegmentCount()
	{
		return this.segments.size();
	}
	
	/**
	 * Returns an Iterable over the Segment in this list.
	 * 
	 * @return an Iterable over the Segment in this list
	 */
	public Iterable<? extends Segment> segments()
	{
		return this.segments;
	}
	
	/**
	 * Returns the maximum segment size of this {@link LazyArrayList}.
	 * 
	 * @return the maximum segment size of this {@link LazyArrayList}
	 */
	public int getMaxSegmentSize()
	{
		return this.maxSegmentSize;
	}

	@Override
	public int size()
	{
		return this.size;
	}
	
	@Override
	public boolean isEmpty()
	{
		return this.size == 0;
	}
	
	@Override
	public boolean contains(final Object element)
	{
		return this.indexOf(element) >= 0;
	}
	
	@Override
	public int indexOf(final Object element)
	{
		return this.indexOfRange(element, 0, this.size);
	}
	
	@Override
	public int lastIndexOf(final Object element)
	{
		return this.lastIndexOfRange(element, 0, this.size);
	}
	
	@Override
	public Object[] toArray()
	{
		final Object[] array = new Object[this.size];
		this.copySegmentsToArray(array);
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final T[] array)
	{
		if(array.length < this.size)
		{
			final Object[] newArray = this.createNewArray(array.getClass(), this.size);
			this.copySegmentsToArray(newArray);
			return (T[]) newArray;
		}
		
		this.copySegmentsToArray(array);
				
		/*
		 * Behavior according to method contract.
		 */
		if(array.length > this.size)
		{
			array[this.size] = null;
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	private <T> T[] createNewArray(final Class<? extends T[]> newType, final int newLength)
	{
		return (Object)newType == (Object)Object[].class
			? (T[]) new Object[newLength]
			: (T[]) Array.newInstance(newType.getComponentType(), newLength);
	}
	
	private void copySegmentsToArray(final Object[] array)
	{
		for(final Segment segment : this.segments)
		{
			final Object[] segmentArray = segment.getData().toArray();
			System.arraycopy(segmentArray, 0, array, segment.offset, segment.segmentSize);
			this.unloader.unload(segment);
		}
	}
		
	@Override
	public E get(final int index)
	{
		validateIndex(this.size, index);
		final Segment segment = this.segmentForIndex(index);
		return segment.getData().get(index - segment.offset);
	}
	
	@Override
	public E set(final int index, final E element)
	{
		validateIndex(this.size, index);
		final Segment segment = this.segmentForIndex(index);
		return segment.set(index - segment.offset, element);
	}
	
	@Override
	public boolean add(final E element)
	{
		this.modCount++;
		final Segment segment = this.ensureSlots();
		segment.add(element);
		this.size++;
		return true;
	}
	
	@Override
	public boolean addAll(final Collection<? extends E> elements)
	{
		final int newElementsSize = elements.size();
		if(newElementsSize == 0)
		{
			return false;
		}

		this.modCount++;
		Segment segment  = null;
		for (final E element : elements) {
			if (segment == null || segment.segmentSize >= this.maxSegmentSize) {
				segment = this.ensureSlots();
			}
			segment.add(element);
		}
		this.size += newElementsSize;
		this.updateOffsets();
		return true;
	}
	
	@Override
	public void add(final int index, final E element)
	{
		if(index == this.size)
		{
			/*
			 * simply add at the end
			 */
			this.add(element);
			return;
		}
		
		validateIndex(this.size + 1, index);
		this.modCount++;
		
		final Segment segment = this.segmentForIndex(index);
		if(segment.segmentSize < this.maxSegmentSize)
		{
			/*
			 * segment has space left
			 */
			segment.add(index - segment.offset, element);
		}
		else
		{
			/*
			 * segment has no space left
			 */
			Segment next = this.segmentForIndex(index + 1);
			if(next == null || next.segmentSize >= this.maxSegmentSize)
			{
				/*
				 * if there is no next or next has no space left
				 * add a new segment
				 */
				final int newSegmentIndex = this.segments.indexOf(segment) + 1;
				next = this.createSegment();
				this.segments.add(newSegmentIndex, next);
			}
			/*
			 * move last element to next segment, add
			 */
			next.add(0, segment.remove(segment.segmentSize - 1));
			segment.add(index - segment.offset, element);
		}
		
		this.size++;
		this.updateOffsets();
	}
	
	@Override
	public boolean addAll(final int index, final Collection<? extends E> elements)
	{
		if(index == this.size)
		{
			/*
			 * simply add at the end
			 */
			return this.addAll(elements);
		}

		validateIndex(this.size + 1, index);
		this.modCount++;
		
		final int newElementsSize = elements.size();
		if(newElementsSize == 0)
		{
			return false;
		}
		
		final Segment segment = this.segmentForIndex(index);
		if(segment.segmentSize + newElementsSize <= this.maxSegmentSize)
		{
			/*
			 * segment has enough space left
			 */
			segment.addAll(index - segment.offset, elements);
		}
		else if(index == 0)
		{
			/*
			 * add new segment(s) at beginning
			 */
			this.addSegments(0, elements.iterator());
		}
		else
		{
			/*
			 * split segment and add new segment(s)
			 */
			final int          indexInSegment = index - segment.offset;
			final ArrayList<E> segmentData    = segment.getData();
			final ArrayList<E> newElements    = new ArrayList<>(elements.size() + segment.segmentSize);
			newElements.addAll(elements);
			newElements.addAll(segmentData.subList(indexInSegment, segment.segmentSize));
			while(segmentData.size() > indexInSegment)
			{
				segment.remove(segment.segmentSize-1);
			}
			final Iterator<E> iterator = newElements.iterator();
			while(iterator.hasNext() && segment.segmentSize < this.maxSegmentSize)
			{
				segment.add(iterator.next());
			}
			this.addSegments(
				this.segments.indexOf(segment) + 1,
				iterator
			);
		}
		
		this.size += newElementsSize;
		this.updateOffsets();
		return true;
	}
	
	@Override
	public E remove(final int index)
	{
		validateIndex(this.size, index);
		this.modCount++;
		
		final Segment segment = this.segmentForIndex(index);
		final E element = segment.remove(index - segment.offset);
		this.removeSegmentIfEmpty(segment);
		this.size--;
		this.updateOffsets();
		return element;
	}
	
	@Override
	public boolean remove(final Object element)
	{
		Segment changedSegment = null;
		for(final Segment segment : this.segments)
		{
			if(segment.remove(element))
			{
				this.modCount++;
				changedSegment = segment;
				break;
			}
		}
		if(changedSegment != null)
		{
			this.removeSegmentIfEmpty(changedSegment);
			this.size--;
			this.updateOffsets();
			return true;
		}
		return false;
	}
		
	@Override
	public boolean removeAll(final Collection<?> elements)
	{
		return this.batchUpdate(
			segment -> segment.removeAll(elements)
		);
	}
	
	@Override
	public boolean removeIf(final Predicate<? super E> filter)
	{
		return this.batchUpdate(
			segment -> segment.removeIf(filter)
		);
	}
	
	@Override
	public boolean retainAll(final Collection<?> elements)
	{
		return this.batchUpdate(
			segment -> segment.retainAll(elements)
		);
	}
	
	@Override
	public boolean consolidate()
	{
		final int segmentsSize;
		if((segmentsSize = this.segments.size()) <= 1)
		{
			// none or just a single segment, nothing to do
			return false;
		}
		
		int firstSegmentWithGaps = -1;
		for(int i = 0; i < segmentsSize - 1; i++)
		{
			final Segment segment = this.segments.get(i);
			if(segment.segmentSize < this.maxSegmentSize)
			{
				firstSegmentWithGaps = i;
				break;
			}
		}
		if(firstSegmentWithGaps < 0)
		{
			// no segment (except the last one) has gaps, nothing to do
			return false;
		}
		
		this.modCount++;
		final ArrayList<Segment> consolidatedSegments = new ArrayList<>();
		if(firstSegmentWithGaps > 0)
		{
			// keep gap-less leading segments
			consolidatedSegments.addAll(this.segments.subList(0, firstSegmentWithGaps));
		}
		
		Segment newSegment = null;
		for(int i = firstSegmentWithGaps; i < segmentsSize; i++)
		{
			for(final E element : this.segments.get(i).getData())
			{
				if(newSegment == null || newSegment.segmentSize >= this.maxSegmentSize)
				{
					newSegment = this.createSegment();
					consolidatedSegments.add(newSegment);
				}
				newSegment.add(element);
			}
		}
		
		this.clear();
		this.segments.addAll(consolidatedSegments);
		consolidatedSegments.clear();
		
		this.updateOffsets();
		return true;
	}
	
	@Override
	public <P extends Consumer<Lazy<?>>> P iterateLazyReferences(final P procedure)
	{
		try
		{
			for(final Segment segment : this.segments)
			{
				procedure.accept(segment.data);
			}
		}
		catch(final ThrowBreak b)
		{
			// abort iteration
		}
		
		return procedure;
	}
		
	@Override
	public void clear()
	{
		this.modCount++;
		for(final Segment segment : this.segments)
		{
			final ArrayList<E> data;
			if((data = segment.data.peek()) != null)
			{
				data.clear();
			}
			
			this.unloader.remove(segment);
		}
		this.segments.clear();
		this.size = 0;
	}
	
	private boolean removeSegmentIfEmpty(final Segment segment)
	{
		if(segment.segmentSize == 0)
		{
			this.segments.remove(segment);
			this.unloader.remove(segment);
			return true;
		}
		return false;
	}
			
	private interface IterationLogic<E, R>
	{
		R apply(E element, int index);
	}
		
	private <R> R iterateForward(
		final int                  startInclusive,
		final int                  endExclusive  ,
		final IterationLogic<E, R> logic
	)
	{
		for(final Segment segment : this.segments)
		{
			if(segment.offset >= endExclusive)
			{
				break;
			}
			if(segment.intersectsRange(startInclusive, endExclusive))
			{
				final ArrayList<E> data                  = segment.getData()                                      ;
				final int          segmentStartInclusive = Math.max(0           , startInclusive - segment.offset);
				final int          segmentEndExclusive   = Math.min(segment.segmentSize, endExclusive   - segment.offset);
				for(int i = segmentStartInclusive; i < segmentEndExclusive; i++)
				{
					final R result = logic.apply(data.get(i), i + segment.offset);
					if(result != null)
					{
						return result;
					}
				}
			}
		}
		
		return null;
	}
	
	private <R> R iterateBackward(
		final int                  startInclusive,
		final int                  endExclusive  ,
		final IterationLogic<E, R> logic
	)
	{
		for(int si = this.segments.size(); --si >= 0; )
		{
			final Segment segment = this.segments.get(si);
			if(segment.offset + segment.segmentSize - 1 < startInclusive)
			{
				break;
			}
			if(segment.intersectsRange(startInclusive, endExclusive))
			{
				final ArrayList<E> data                  = segment.getData()                                         ;
				final int          segmentStartInclusive = Math.max(0           , startInclusive - segment.offset);
				final int          segmentEndExclusive   = Math.min(segment.segmentSize, endExclusive   - segment.offset);
				for(int i = segmentEndExclusive - 1; i >= segmentStartInclusive; i--)
				{
					final R result = logic.apply(data.get(i), i + segment.offset);
					if(result != null)
					{
						return result;
					}
				}
				
				this.unloader.unload(segment);
			}
		}
		
		return null;
	}

	private int indexOfRange(
		final Object element       ,
		final int    startInclusive,
		final int    endExclusive
	)
	{
		final IterationLogic<E, Integer> logic = element == null
			? (e, index) -> e == null         ? index : null
			: (e, index) -> e.equals(element) ? index : null
		;
		final Integer index = this.iterateForward(startInclusive, endExclusive, logic);
		return index != null
			? index
			: -1
		;
	}
	
	private int lastIndexOfRange(
		final Object element       ,
		final int    startInclusive,
		final int    endExclusive
	)
	{
		final IterationLogic<E, Integer> logic = element == null
			? (e, index) -> e == null         ? index : null
			: (e, index) -> e.equals(element) ? index : null
		;
		final Integer index = this.iterateBackward(startInclusive, endExclusive, logic);
		return index != null
			? index
			: -1
		;
	}
	 
	/**
	 * Search the segment that contains the supplied index.
	 * <br><b>
	 * The Index is not validated, this must be done before calling this method!
	 * </b></br>
	 * 
	 * @param index index to be searched for.
	 * @return Segment containing the index or null.
	 */
	private Segment segmentForIndex(final int index)
	{
		final int firstGuess = index / this.maxSegmentSize;
		
		if(this.segments.size() > firstGuess)
		{
			final Segment segment = this.segments.get(firstGuess);
			
			if(segment.containsIndex(index))
			{
				return segment;
			}
	
			if(index >= segment.offset + segment.segmentSize)
			{
				return this.segmentForIndex(index, firstGuess + 1, this.segments.size());
			}
			else if(index < segment.offset)
			{
				return this.segmentForIndex(index, 0, firstGuess -1);
			}
		}
		
		throw new NoSuchElementException("Index " + index + "not found!");
	}
	
	/**
	 * Do a binary search for the segment that contains the supplied index.
	 * <br><b>
	 * The Index is not validated, this must be done before calling this method!
	 * </b></br>
	 * @param index index to be searched for.
	 * @param lowSegmentIndex lower limit to search within.
	 * @param highSegmentIndex upper limit to search within.
	 * @return Segment containing the index or null.
	 */
	private Segment segmentForIndex(final int index, final int lowSegmentIndex, final int highSegmentIndex)
	{
		int hi = highSegmentIndex;
		int lo = lowSegmentIndex;
	
		while (lo <= hi)
		{
			final int mid = lo  + (hi - lo) / 2;

			final Segment midSegment = this.segments.get(mid);
			if(index >= midSegment.offset + midSegment.segmentSize)
			{
				lo = mid + 1;
			}
			else if(index < midSegment.offset)
			{
				hi = mid - 1;
			}
			else
			{
				return midSegment;
			}
		}
		return null;
	}
	
	private Segment ensureSlots()
	{
		Segment segment;
		if(this.segments.isEmpty())
		{
			this.segments.add(segment = this.createSegment());
		}
		else
		{
			segment = this.segments.get(this.segments.size() - 1);
			if(segment.segmentSize >= this.maxSegmentSize)
			{
				segment = this.createSegment();
				segment.offset = this.size;
				this.segments.add(segment);
			}
	}
		return segment;
    }
	
	/**
	 * Adds segments to fit the new elements at the specified segment index.
	 * The caller is responsible to update the offsets afterwards.
	 */
	private void addSegments(final int index, final Iterator<? extends E> elements)
	{
		int     newSegmentIndex = index;
		Segment newSegment      = null;
		while(elements.hasNext())
		{
			if(newSegment == null || newSegment.segmentSize >= this.maxSegmentSize)
			{
				newSegment = this.createSegment();
				this.segments.add(newSegmentIndex++, newSegment);
			}
			newSegment.add(elements.next());
		}
	}
	
	private boolean batchUpdate(final Predicate<Segment> segmentOperation)
	{
		boolean changed = false;
		for(final Segment segment : this.segments)
		{
			final int oldSize = segment.segmentSize;
			if(segmentOperation.test(segment))
			{
				this.modCount++;
				this.size -= oldSize - segment.segmentSize;
				changed = true;
			}
		}
		if(changed)
		{
			this.segments.removeIf(segment -> {
				if(segment.segmentSize == 0)
				{
					this.unloader.remove(segment);
					return true;
				}
					return false;
				});
			
			this.updateOffsets();
		}
		return changed;
	}
	
	private void updateOffsets()
	{
		int offset = 0;
		for(final Segment segment : this.segments)
		{
			if(segment.offset != offset)
			{
				segment.setOffset(offset);
			}
			offset += segment.segmentSize;
		}
	}

	private E removeFromSegment( final Segment segment, final int index)
	{
		final E element = segment.remove(index);
		this.removeSegmentIfEmpty(segment);
		this.size--;
		this.updateOffsets();
		return element;
	}
	
	private Segment createSegment()
	{
		return new Segment(this.maxSegmentSize);
	}
	
	//required by BinaryHandlerLazyArrayList
	@SuppressWarnings({ "unchecked", "unused" })
	private void addSegment(final int segmentOffset, final int segmentSize, final Object data)
	{
		this.segments.add(new Segment(segmentOffset, segmentSize, (ControlledLazyReference<ArrayList<E>>)data));
		
	}
	
	private int modCount()
	{
		return this.modCount;
	}
		
	/**
	 * Creates a {@link Spliterator} that splits at the LazyArrayList segments borders.
	 * 
	 * @return a {@link Spliterator} instance.
	 */
	@Override
	public Spliterator<E> spliterator()
	{
		return this.segmentSpliterator();
	}

	/**
	 * Creates a {@link Spliterator} that splits at the LazyArrayList segments borders.
	 * 
	 * @return a {@link Spliterator} instance.
	 */
	public Spliterator<E> segmentSpliterator()
	{
		return new SegmentSpliterator(0, -1, 0);
	}
	
	
	/**
	 * Creates a new {@link Iterator} that will iterate all loaded entries first,
	 * it does not define any other specific ordering.
	 * 
	 * @return a new loadedFirstIterator.
	 */
	public Iterator<E> loadedFirstIterator()
	{
		return new LoadedFirstIterator();
	}
	

	/**
	 * An @link Iterator} that will iterate all loaded entries first,
	 * it does not guarantee any other specific ordering.
	 *
	 */
	private class LoadedFirstIterator implements Iterator<E>
	{
		int segmentIndexCursor = 0;
		int totalCursor = 0;
		int expectedModCount = LazyArrayList.this.modCount();
		Segment currentSegment = null;
		
		final Stack<Segment> loadedSegments;
		final Stack<Segment> unloadedSegments;
		
		int lastRet = -1;
		Segment lastSegment = null;
				
		public LoadedFirstIterator()
		{
			this.loadedSegments = new Stack<>();
			this.unloadedSegments = new Stack<>();
		}
		
		@Override
		public boolean hasNext()
		{
			return this.totalCursor < LazyArrayList.this.size();
		}

		@Override
		public E next()
		{
			if(!this.hasNext())
			{
				throw new IndexOutOfBoundsException(this.totalCursor);
			}
			
			if (LazyArrayList.this.modCount() != this.expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
			
			if(this.currentSegment == null)
			{
				this.init();
				this.currentSegment = this.nextSegment();
			}
						
			final E next =  this.currentSegment.getData().get(this.segmentIndexCursor);
			this.totalCursor++;
			this.lastRet = this.segmentIndexCursor;
			this.lastSegment = this.currentSegment;
						
			if(++this.segmentIndexCursor >= this.currentSegment.segmentSize)
			{
				this.segmentIndexCursor = 0;
				this.currentSegment = this.nextSegment();
			}
						
			return next;
		}
		
		@Override
		public void remove()
		{
			if(this.lastRet < 0)
			{
				 throw new IllegalStateException();
			}
			
			if (LazyArrayList.this.modCount() != this.expectedModCount)
			{
				throw new ConcurrentModificationException();
			}
			
			LazyArrayList.this.removeFromSegment(this.lastSegment, this.lastRet);
			this.expectedModCount = LazyArrayList.this.modCount();
			if (this.lastRet < this.totalCursor)
			{
				this.totalCursor--;
				if(this.segmentIndexCursor > 0)
				{
					this.segmentIndexCursor--;
				}
			}
			this.lastRet = -1;
		}

		private void init()
		{
			for (final Segment segment : LazyArrayList.this.segments)
			{
				if(segment.isLoaded())
				{
					this.loadedSegments.push(segment);
				}
				else
				{
					this.unloadedSegments.push(segment);
				}
			}
		}
		
		private Segment nextSegment()
		{
			if(!this.loadedSegments.empty())
			{
				return this.loadedSegments.pop();
			}
			
			if(!this.unloadedSegments.empty())
			{
				return this.unloadedSegments.pop();
			}
			
			return null;
		}
	}
	
	public final class Segment implements LazyClearController, LazySegment<ArrayList<E>>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private int                                 offset     ;
		private int                                 segmentSize;
		private transient boolean                   modified   ;
		private final ControlledLazyReference<ArrayList<E>> data ;
		boolean allowUnloading = true;
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////
		
		/**
		 * Default constructor
		 */
		Segment(final int initialCapacity)
		{
			super();
			this.offset = 0;
			this.segmentSize   = 0;
			this.data = Lazy.register(new  ControlledLazyReference.Default<>(
				 new ArrayList<>(initialCapacity),
				 this
				));
		}
				
		/**
		 * Constructor
		 */
		private Segment(final int offset, final int size, final ControlledLazyReference<ArrayList<E>> data)
		{
			super();
			this.offset = offset;
			this.segmentSize = size;
			this.data = data;
			this.data.setLazyClearController(this);
		}
		
		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////
		
		@Override
		public int size()
		{
			return this.segmentSize;
		}
		
		@Override
		public void allowUnload(final boolean allow)
		{
			this.allowUnloading = allow;
		}
		
		@Override
		public boolean unloadAllowed()
		{
			return this.allowUnloading;
		}
		
		public int getOffset()
		{
			return this.offset;
		}

		public int getSize()
		{
			return this.segmentSize;
		}
		
		@Override
		public boolean isLoaded()
		{
			return this.data.isLoaded();
		}
		
		@Override
		public boolean isModified()
		{
			return this.modified;
		}
		
		@Override
		public void unloadSegment()
		{
			this.data.clear();
		}
		
		@Override
		public boolean allowClear()
		{
			return !this.modified;
		}

		//required by BinaryHandlerLazyArrayList
		@SuppressWarnings("unused")
		private void cleanModified()
		{
			this.modified = false;
		}
		
		private void setOffset(final int offset)
		{
			this.offset = offset;
		}

		//required by BinaryHandlerLazyArrayList
		@SuppressWarnings("unused")
		private Lazy<ArrayList<E>> getLazy()
		{
			return this.data;
		}
		
		private boolean retainAll(final Collection<?> elements)
		{
			if(this.getData().retainAll(elements)) {
				this.segmentSize = this.getData().size();
				this.modified = true;
				return true;
			}
			return false;
		}

		private boolean removeIf(final Predicate<? super E> filter)
		{
			if(this.getData().removeIf(filter)) {
				this.segmentSize = this.getData().size();
				this.modified = true;
				return true;
			}
			return false;
		}

		private boolean removeAll(final Collection<?> elements)
		{
			if(this.getData().removeAll(elements)) {
				this.segmentSize = this.getData().size();
				this.modified = true;
				return true;
			}
			return false;
		}

		private boolean remove(final Object element)
		{
			if(this.getData().remove(element))
			{
				this.modified = true;
				this.segmentSize--;
				return true;
			}
			return false;
		}
		
		private E remove(final int index)
		{
			final E element = this.getData().remove(index);
			this.modified = true;
			this.segmentSize--;
			return element;
		}

		private boolean addAll(final int index, final Collection<? extends E> elements)
		{
			if(this.getData().addAll(index, elements)) {
				this.segmentSize = this.getData().size();
				this.modified = true;
				return true;
			}
			return false;
		}

		private boolean add(final E element)
		{
			this.getData().add(element);
			this.modified = true;
			this.segmentSize++;
			return true;
		}

		private void add(final int index, final E element)
		{
			this.getData().add(index, element);
			this.modified = true;
			this.segmentSize++;
		}

		private E set(final int index, final E element)
		{
			final E previous = this.getData().set(index, element);
			this.modified = true;
			return previous;
		}
			
		@Override
		public ArrayList<E> getData()
		{
			LazyArrayList.this.unloader.unload(this);
			return this.data.get();
		}
		
		//required by BinaryHandlerLazyArrayList
		@SuppressWarnings("unused")
		private ArrayList<E> getLazyData()
		{
			return this.data.get();
		}
				
		private boolean containsIndex(final int index)
		{
			return index >= this.offset
				&& index < this.offset + this.segmentSize
			;
		}
		
		private boolean intersectsRange(final int startInclusive, final int endExclusive)
		{
			final int startOffset = this.offset                ;
			final int endOffset   = this.offset + this.segmentSize - 1;
			return startOffset >= startInclusive && startOffset < endExclusive
				|| endOffset   >= startInclusive && endOffset   < endExclusive
			;
		}
				
		@Override
		public String toString() {
			return "Segment "     +
					"[offset="    + this.offset +
					", size="     + this.segmentSize +
					", modified=" + this.modified +
					", data="     + this.data +
					"]";
		}
	}
	
	/**
	 * A late-binding {@link Spliterator} that splits the LazyArrayList at the internal lazy loaded segments borders.
	 * If all elements are in the same segment this Spliterator doesn't allow further splitting.
	 * This implementation throws {@link ConcurrentModificationException} if a concurrent modification is detected.
	 *	
	 * <p>The Spliterator reports the characteristics {@link Spliterator#SIZED},
	 *  {@link Spliterator#SUBSIZED}, and {@link Spliterator#ORDERED}.
	 */
	final class SegmentSpliterator implements Spliterator<E>
	{
		private int fence;
		private int index;
		private int expectedModCount;
		private Segment lastSegment = null;
		
		public SegmentSpliterator(final int index, final int fence, final int expectedModCount)
		{
			super();
			this.index = index;
			this.fence = fence;
			this.expectedModCount = expectedModCount;
		}
				
		/**
		 * allow late binding
		 */
		@SuppressWarnings("synthetic-access")
		private int getFence()
		{
			int hi;
			if ((hi = this.fence) < 0)
			{
				this.expectedModCount = LazyArrayList.this.modCount;
				hi = this.fence = LazyArrayList.this.size;
			}
			return hi;
		}
		
		@SuppressWarnings("synthetic-access")
		@Override
		public boolean tryAdvance(final Consumer<? super E> action)
		{
			if (action == null)
			{
				throw new NullPointerException();
			}
			
			final int hi = this.getFence(), i = this.index;
			if(i < hi)
			{
				this.index = i + 1;
				
				final LazyArrayList<E>.Segment nextSegment = LazyArrayList.this.segmentForIndex(i);
				nextSegment.allowUnload(false);
				
				if(this.lastSegment != nextSegment)
				{
					if(this.lastSegment != null)
					{
						this.lastSegment.allowUnload(true);
					}
					this.lastSegment = nextSegment;
				}
				else
				{
					this.lastSegment.allowUnload(false);
				}
								
				action.accept(LazyArrayList.this.get(i));
				if (LazyArrayList.this.modCount != this.expectedModCount)
				{
					throw new ConcurrentModificationException();
				}
				return true;
			}
					
			if(this.lastSegment != null)
			{
				this.lastSegment.allowUnload(true);
			}
			
			return false;
		}

		@Override
		public LazyArrayList<E>.SegmentSpliterator trySplit()
		{
			final int hi = this.getFence();
			final int lo = this.index;
			final int mid = lo + hi >>> 1;
			
			final Segment midSegment = LazyArrayList.this.segmentForIndex(mid);
			
			//return null if no more split possible
			//because all elements are in same segment
			if(hi <= midSegment.offset + midSegment.segmentSize &&
			   lo >= midSegment.offset)
			{
				return null;
			}
							
			final int dist_lo  = mid - midSegment.offset;
			final int dist_hi  = midSegment.offset + midSegment.segmentSize - mid;
			
			//put midSegment to left or right?
			int splitIndex;
			if(dist_lo < dist_hi)
			{
				splitIndex = midSegment.offset;
			}
			else
			{
				splitIndex = midSegment.offset + midSegment.segmentSize;
			}
			 
			this.index = splitIndex;
			
			return new SegmentSpliterator(lo, this.index, this.expectedModCount);
		}

		@Override
		public long estimateSize()
		{
			return this.getFence() - this.index;
		}

		@Override
		public int characteristics()
		{
			return Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
		}
		
	}

}
