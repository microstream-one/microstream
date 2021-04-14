package one.microstream.collections.types;

import java.util.Comparator;

import one.microstream.collections.sorting.SortableProcedure;

public interface XIncreasingSequence<E> extends XInputtingSequence<E>, XSortableSequence<E>, SortableProcedure<E>
{
	public interface Creator<E> extends XInputtingSequence.Creator<E>, XSortableSequence.Creator<E>
	{
		@Override
		public XIncreasingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> addAll(E... elements);

	@Override
	public XIncreasingSequence<E> addAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> putAll(E... elements);

	@Override
	public XIncreasingSequence<E> putAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> prependAll(E... elements);

	@Override
	public XIncreasingSequence<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingSequence<E> preputAll(E... elements);

	@Override
	public XIncreasingSequence<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingSequence<E> preputAll(XGettingCollection<? extends E> elements);

	@Override
	public XIncreasingSequence<E> swap(long indexA, long indexB);

	@Override
	public XIncreasingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XIncreasingSequence<E> copy();

	@Override
	public XIncreasingSequence<E> toReversed();
	@Override
	public XIncreasingSequence<E> reverse();

	@Override
	public XIncreasingSequence<E> range(long fromIndex, long toIndex);

	@Override
	public XIncreasingSequence<E> sort(Comparator<? super E> comparator);

}
