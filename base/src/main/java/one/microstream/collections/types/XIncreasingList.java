package one.microstream.collections.types;

import java.util.Comparator;

public interface XIncreasingList<E> extends XInputtingList<E>, XSettingList<E>, XIncreasingSequence<E>
{
	public interface Creator<E>
	extends XInputtingList.Factory<E>, XSettingList.Creator<E>, XIncreasingSequence.Creator<E>
	{
		@Override
		public XIncreasingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> addAll(E... elements);

	@Override
	public XIncreasingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> putAll(E... elements);

	@Override
	public XIncreasingList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> prependAll(E... elements);

	@Override
	public XIncreasingList<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> preputAll(E... elements);

	@Override
	public XIncreasingList<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> preputAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingList<E> setAll(long index, E... elements);

	@Override
	public XIncreasingList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XIncreasingList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XIncreasingList<E> swap(long indexA, long indexB);

	@Override
	public XIncreasingList<E> swap(long indexA, long indexB, long length);

	@Override
	public XIncreasingList<E> copy();

	@Override
	public XIncreasingList<E> toReversed();
	@Override
	public XIncreasingList<E> reverse();

	@Override
	public XIncreasingList<E> range(long fromIndex, long toIndex);

	@Override
	public XIncreasingList<E> fill(long offset, long length, E element);

	@Override
	public XIncreasingList<E> sort(Comparator<? super E> comparator);

}
