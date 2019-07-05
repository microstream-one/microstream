package one.microstream.experimental;

import static one.microstream.math.XMath.positive;

import java.util.function.Consumer;

import one.microstream.chars.VarString;
import one.microstream.collections.AbstractArrayStorage;
import one.microstream.collections.types.XGettingCollection;
import one.microstream.typing.XTypes;

public final class OldVarList<E>
{
	// (15.12.2011)NOTE: this implementation could easily be changed/copied to work with long capacity

	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final int MIN_SEGMENT_SIZE = 8; // values below make arrayCopy inefficent and high memory overhead
	private static final int DEFAULT_SEG_SIZE = 8; // experiments showed good results for this in most situations



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	private static int segmentSize(final int desiredSegmentSize)
	{
		return desiredSegmentSize < MIN_SEGMENT_SIZE ? MIN_SEGMENT_SIZE : desiredSegmentSize;
	}

	private static <E> E forwardGet(Segment<E> seg, int index)
	{
		while((index -= (seg = seg.next).size) >= 0)
		{
			// Scrolling inlined. Significantly faster than conventional loop.
		}
		return seg.data[index + seg.size];
	}

	private static <E> E reverseGet(Segment<E> seg, int index)
	{
		while((index += (seg = seg.prev).size) < 0)
		{
			// Scrolling inlined. Significantly faster than conventional loop.
		}
		return seg.data[index];
	}

	private static int calculateCapacity(Segment<?> seg)
	{
		long c = 0;
		while((seg = seg.next) != null)
		{
			c += seg.data.length;
		}
		return c > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)c;
	}


	private static boolean alreadyOptimized(final Segment<?> head, final int nss)
	{
		Segment<?> seg = head;
		while((seg = seg.next) != null)
		{
			if(seg.data.length != nss)
			{
				return false;
			}
			if(seg.size < seg.data.length)
			{
				return seg == head.prev;
			}
		}
		return true;
	}

	private static int align(final int size, final int segmentSize)
	{
		int i = segmentSize;
		while(i < size)
		{
			i += segmentSize;
		}
		return i - segmentSize;
	}

	private static <E> E[] copyToArray(Segment<?> seg, final E[] array)
	{
		for(int i = 0; (seg = seg.next) != null; i += seg.size)
		{
			System.arraycopy(seg.data, 0, array, i, seg.size); // could be easily parallelized
		}
		return array;
	}

	static <E> void insertBefore(final Segment<E> nextSegment, final E[] data)
	{
		nextSegment.prev = nextSegment.prev.next = new Segment<>(data, data.length, nextSegment.prev, nextSegment);
	}

	static <E> void insertBefore(final Segment<E> nextSegment, final E[] data, final int size)
	{
		nextSegment.prev = nextSegment.prev.next = new Segment<>(data, size, nextSegment.prev, nextSegment);
	}

	private static <E> void insertAfter(final Segment<E> segment, final E[] data, final int size)
	{
		segment.next = segment.next.prev = new Segment<>(data, size, segment, segment.next);
	}

	private static void clear(final Object[] data, int i, final int bound)
	{
		while(++i < bound)
		{
			data[i] = null;
		}
	}

	private void internalInsert(Segment<E> segment, int index, final E element)
	{
		while(index >= (segment = segment.next).size)
		{
			index -= segment.size;
		}
		// (16.12.2011 TM)FIXME: reverse scrolling
		if(index == 0 && segment.prev.size < segment.prev.data.length){ // excludes the head entry automatically
			segment.prev.data[segment.prev.size++] = element;
		}
		else if(segment.size < segment.data.length){
			System.arraycopy(segment.data, index, segment.data, index+1, segment.size - index);
			segment.data[index] = element;
			segment.size++;
		}
		else
		{
			if(segment.next == null)
			{
				this.head.prev = segment.next = new Segment<>(
					this.newData(segment.data, index, segment.size - index),
					segment.size - index,
					segment
				);
			}
			else
			{
				insertAfter(segment, this.newData(segment.data, index, segment.size - index), segment.size - index);
			}
			clear(segment.data, index, segment.size); // nullify unused buckets to avoid memory leaks
			segment.data[index] = element;
			segment.size = index + 1; // odd, but correct
		}
		this.size++;
	}

	void internalInsert(final Segment<E> seg, final int index, final E[] elements)
	{
		// empty
	}

	public boolean insert(final int index, final E element)
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new IndexOutOfBoundsException("Reached maximum capacity");
		}
		if(index < 0 || index > this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		if(index == 0 && this.size == 0)
		{
			this.internalAdd(element);
		}
		this.internalInsert(this.head, index, element);
		return true;
	}


	private E internalRemove(Segment<E> segment, int index)
	{
		while(index >= (segment = segment.next).size)
		{
			index -= segment.size;
		}
		final E oldElement = segment.data[index];
		System.arraycopy(segment.data, index+1, segment.data, index, segment.size - index - 1);
		segment.data[segment.size--] = null;
		this.size--;
		return oldElement;
	}

	public E remove(final int index)
	{
		checkIndex(index, this.size);
		return this.internalRemove(this.head, index);
	}



	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	@SuppressWarnings("unchecked")
	final Segment<E> head = new Segment<>((E[])new Object[0], 0, null); // nasty: defSegSize is still 0 here
	{
		this.head.prev = this.head;
	}

	int dataCapacity;
	transient int size = 0;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public OldVarList()
	{
		super();
		this.dataCapacity = DEFAULT_SEG_SIZE;
	}

	public OldVarList(final int segmentSize)
	{
		super();
		this.dataCapacity = segmentSize(segmentSize);
	}


	///////////////////////////////////////////////////////////////////////////
	// getters //
	////////////

	public int getNewSegmentSize()
	{
		return this.dataCapacity;
	}



	///////////////////////////////////////////////////////////////////////////
	// setters //
	////////////

	/**
	 * Experiments showed that around sqrt(targetSize) is a good value for segment sizes.
	 * <p>
	 * Example: Segment size of 1000 is well suited for a target size fo 1 million elements.
	 *
	 * @param element
	 * @return
	 */
	public OldVarList<E> setSegmentSize(final int segmentSize)
	{
		this.dataCapacity = segmentSize(segmentSize);
		return this;
	}

	public OldVarList<E> setSegmentSizeRelative(final int pow2Fraction)
	{
		this.dataCapacity = segmentSize(this.size >>> pow2Fraction);
		return this;
	}



	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	@SuppressWarnings("unchecked")
	private E[] newData()
	{
		return (E[])new Object[this.dataCapacity];
	}

	@SuppressWarnings("unchecked")
	private E[] newData(final Object[] array)
	{
		final Object[] data;
		System.arraycopy(
			array,
			0,
			data = new Object[array.length < this.dataCapacity ? this.dataCapacity : array.length],
			0,
			array.length
		);
		return (E[])data;
	}

	@SuppressWarnings("unchecked")
	private E[] newData(final Object[] array, final int srcPos, final int length)
	{
		final Object[] data;
		System.arraycopy(
			array,
			srcPos,
			data = new Object[length < this.dataCapacity ? this.dataCapacity : length],
			0,
			length
		);
		return (E[])data;
	}

	private static void checkIndex(final int index, final int size)
	{
		if(index < 0 || index >= size)
		{
			throw new IndexOutOfBoundsException();
		}
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	public E get(final int index)
	{
		checkIndex(index, this.size);
//		return forwardGet(this.head, index);
		return index < this.size>>>1
			? forwardGet(this.head, index)
			: reverseGet(this.head, index - this.size)
		;
	}

	public OldVarList<E> execute(final Consumer<? super E> procedure)
	{
		Segment<E> seg = this.head;
		while((seg = seg.next) != null)
		{
			AbstractArrayStorage.iterate(seg.data, seg.size, procedure);
		}
		return this;
	}


	private void internalAdd(final E element)
	{
		final Segment<E> tail = this.head.prev;
		if(tail.size < tail.data.length)
		{
			tail.data[tail.size++] = element;
		}
		else
		{
			(this.head.prev = tail.next = new Segment<>(this.newData(), 1, tail)).data[0] = element;
		}
		this.size++;
	}


	public boolean add(final E element)
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new IndexOutOfBoundsException();
		}
		this.internalAdd(element);
		return true;
	}

	private void internalAddArray(final Object[] arr)
	{
		this.head.prev = this.head.prev.next = new Segment<>(this.newData(arr), arr.length, this.head.prev);
		this.size += arr.length;
	}

	private void internalAddArray(final Object[] arr, final int srcPos, final int length)
	{
		this.head.prev = this.head.prev.next = new Segment<>(
			this.newData(arr, srcPos, length), length, this.head.prev
		);
		this.size += length;
	}

	@SafeVarargs
	public final OldVarList<E> addAll(final E... elements)
	{
		if(elements == null || elements.length == 0)
		{
			return this;
		}
		if(Integer.MAX_VALUE - elements.length < this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		this.internalAddArray(elements);
		return this;
	}

	public OldVarList<E> addAll(final E[] elements, final int offset, final int length)
	{
		if(length == 0)
		{
			return this;
		}
		else if(length < 0){
			throw new one.microstream.meta.NotImplementedYetError(); // FIXME VarList#addAll(length < 0)
		}

		if(Integer.MAX_VALUE - length < this.size)
		{
			throw new IndexOutOfBoundsException();
		}
		this.internalAddArray(elements, offset, length);
		return this;
	}

	public OldVarList<E> addAll(final XGettingCollection<? extends E> elements)
	{
		if(elements.isEmpty())
		{
			return this;
		}
		if(Integer.MAX_VALUE - XTypes.to_int(elements.size()) < this.size)
		{
			throw new IndexOutOfBoundsException();
		}

		this.internalAddArray(elements.toArray());
		return this;
	}

	@SuppressWarnings("unchecked")
	public int optimize()
	{
		final int segmentSize;

		// escape case: already optimized, prevent any unnecessary writing to storage
		if(alreadyOptimized(this.head, segmentSize = this.dataCapacity))
		{
			return calculateCapacity(this.head);
		}

		// special case: segment size equals collection size (single segment storage)
		if(this.size == segmentSize)
		{
			this.head.next = new Segment<>((E[])this.toArray(), segmentSize, this.head);
			return segmentSize;
		}

		// normal case: cut off old storage, redistribute old elements into optimized storage
		// (16.12.2011 TM)XXX: VarList: no buffer or at least keeping fitting segments at the beginning would be better
		final Object[] buffer = this.toArray();
		final int bound = align(this.size, segmentSize);
		(this.head.prev = this.head).next = null;
		for(int i = 0; i < bound; i++)
		{
			this.internalAddArray(buffer, i, segmentSize);
		}
		this.internalAddArray(buffer, bound, this.size - bound);
		return bound + segmentSize;
	}

	public void truncate()
	{
		(this.head.prev = this.head).next = null;
		this.size = 0;
	}

	public void clear()
	{
		(this.head.prev = this.head).next = null;
		this.size = 0;
	}

	public Object[] toArray()
	{
		return copyToArray(this.head, new Object[this.size]);
	}

	@Override
	public String toString()
	{
		if(this.size == 0)
		{
			return "[]";
		}
		final VarString vc = VarString.New((int)(this.size * 5.0f)).append('[');
		Segment<E> seg = this.head;
		while((seg = seg.next) != null)
		{
			final E[] data = seg.data;
			final int bound = seg.size;
			for(int i = 0; i < bound; i++)
			{
				vc.add(data[i]).add(',', ' ');
			}
		}
		vc.deleteLast().setLast(']');
		return vc.toString();
	}








	int idxSpan = 8192;
	int idxSize = 0;
	Index<E> iHead;
	Index<E> iTail = this.iHead = new Index<>(this.head);

	public E get2(int index)
	{
		checkIndex(index, this.size);

//		Index<E> idx = this.iHead.znext;
//		for(;index >= this.idxSpan; index -= this.idxSpan)
//		{
//			idx = idx.znext;
//		}
//		return forwardGet(idx.segment.prev, index + idx.offset);

		if(this.idxSpan >= index){ // also automatically disables Index use when span < 0
			return forwardGet(this.head, index);
		}
		Index<E> idx = this.iHead;
		do {
			idx = idx.next;
		}
		while((index -= this.idxSpan) >= 0);
		return forwardGet(idx.segment.prev, index + this.idxSpan + idx.offset);
	}

	void updateIndexRight1(final Index<E> idx)
	{
		if(++idx.offset >= idx.segment.size)
		{
			idx.segment = idx.segment.next;
			idx.offset = 0;
		}
		// (17.12.2011)FIXME: construction site
	}

	private void internalAdd2(final E element)
	{
		final Segment<E> tail = this.head.prev;
		if(tail.size < tail.data.length)
		{
			tail.data[tail.size++] = element;
		}
		else
		{
			(this.head.prev = tail.next = new Segment<>(this.newData(), 1, tail)).data[0] = element;
		}
//		++this.size;
		if(++this.size == this.idxSize){ // also automatically disables Index use when span < 0
			this.iTail = this.iTail.next = new Index<>(this.head.prev);
			this.idxSize += this.idxSpan;
		}
	}

	public boolean add2(final E element)
	{
		if(this.size >= Integer.MAX_VALUE)
		{
			throw new IndexOutOfBoundsException();
		}
		this.internalAdd2(element);
		return true;
	}

	public OldVarList<E> optimizeIndex()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME VarList#optimizeIndex
	}

	private void rebuildIndex()
	{
		throw new one.microstream.meta.NotImplementedYetError(); // FIXME VarList#rebuildIndex
	}

	/* (17.12.2011)NOTE: funny thing about disabled index:
	 * It showed hardly better adding performance for proper index spans.
	 * Of course disabling the index still saves memory (which also is negligible as well for proper index spans)
	 *
	 */
	public OldVarList<E> disableIndex()
	{
		this.idxSpan = this.idxSize = -1;
		(this.iTail = this.iHead).next = null;
		return this;
	}

	public OldVarList<E> enableIndex()
	{
		return this.optimizeIndex();
	}

	public OldVarList<E> setIndexSpan(final int indexSpan)
	{
		if(this.idxSpan == indexSpan)
		{
			return this; // no-op
		}
		this.idxSpan = positive(indexSpan) < this.dataCapacity ? this.dataCapacity : indexSpan;
		this.rebuildIndex();
		return this;
	}

}



final class Segment<E>
{
	final E[] data;
	int size;
	Segment<E> prev, next;

	Segment(final E[] data, final int size, final Segment<E> prev, final Segment<E> next)
	{
		super();
		this.data = data;
		this.size = size;
		this.prev = prev;
		this.next = next;
	}

	Segment(final E[] data, final int size, final Segment<E> prev)
	{
		super();
		this.data = data;
		this.size = size;
		this.prev = prev;
		this.next = null;
	}

	@Override
	public String toString()
	{
		return AbstractArrayStorage.toString(this.data, this.size);
	}

}


final class Index<E>
{
	Segment<E> segment;
	int offset;
	Index<E> next;

	public Index(final Segment<E> segment)
	{
		super();
		this.segment = segment;
		this.offset = segment.size;
		this.next = null;
	}

	@Override
	public String toString()
	{
		return this.segment.toString() + "+" + this.offset;
	}

}



//final class test
//{
//	static <E> void sort(final E[][] data, final int[] indices, final Comparator<E> cmp, final VarList<E> list)
//	{
//		final int size = data.length;
//		int h1i;
//		final int h2i = h1i = 0;
//		E h1e;
//		final E h2e = h1e = data[0][indices[0]];
//
//		for(int i = 0; i < size; i++)
//		{
////			if(indices[i] < 0) continue;
//			if(cmp.compare(currentLowest, data[i][indices[i]]) < 0)
//			{
////				currentLowest = data[i][indices[i]];
////				currentLowestIdx = i;
////			}
////		}
//
//	}
//}


