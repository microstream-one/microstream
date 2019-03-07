package one.microstream.collections.types;

import java.util.Comparator;

public interface XIncreasingEnum<E> extends XInputtingEnum<E>, XSortableEnum<E>, XIncreasingSequence<E>
{
	public interface Factory<E>
	extends XInputtingEnum.Creator<E>, XSortableEnum.Creator<E>, XIncreasingSequence.Creator<E>
	{
		@Override
		public XIncreasingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingEnum<E> addAll(E... elements);

	@Override
	public XIncreasingEnum<E> addAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingEnum<E> putAll(E... elements);

	@Override
	public XIncreasingEnum<E> putAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingEnum<E> prependAll(E... elements);

	@Override
	public XIncreasingEnum<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XIncreasingEnum<E> preputAll(E... elements);

	@Override
	public XIncreasingEnum<E> preputAll(E[] elements, int offset, int length);

	@Override
	public XIncreasingEnum<E> preputAll(XGettingCollection<? extends E> elements);

	@Override
	public XIncreasingEnum<E> swap(long indexA, long indexB);

	@Override
	public XIncreasingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XIncreasingEnum<E> copy();

	@Override
	public XIncreasingEnum<E> toReversed();
	@Override
	public XIncreasingEnum<E> reverse();

	@Override
	public XIncreasingEnum<E> range(long fromIndex, long toIndex);

	@Override
	public XIncreasingEnum<E> sort(Comparator<? super E> comparator);

}
