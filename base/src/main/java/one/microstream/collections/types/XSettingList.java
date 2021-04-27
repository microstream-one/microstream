package one.microstream.collections.types;

import java.util.Comparator;

public interface XSettingList<E> extends XReplacingBag<E>, XSettingSequence<E>, XGettingList<E>
{
	public interface Creator<E> extends XReplacingBag.Factory<E>, XSettingSequence.Creator<E>, XGettingList.Factory<E>
	{
		@Override
		public XSettingList<E> newInstance();
	}

	/**
	 * Fills all slots from the offset to the offset+length with the given element,
	 * regardless of whether or not a slot is {@code null}.
	 * 
	 * @param offset from the start of the collection (start index)
	 * @param length of how many slots should be filled
	 * @param element to use for filling of slots
	 * @return this
	 */
	public XSettingList<E> fill(long offset, long length, E element);



	@SuppressWarnings("unchecked")
	@Override
	public XSettingList<E> setAll(long index, E... elements);

	@Override
	public XSettingList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XSettingList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XSettingList<E> swap(long indexA, long indexB);

	@Override
	public XSettingList<E> swap(long indexA, long indexB, long length);

	@Override
	public XSettingList<E> reverse();

	@Override
	public XSettingSequence<E> sort(Comparator<? super E> comparator);

	@Override
	public XSettingList<E> copy();

	@Override
	public XSettingList<E> toReversed();

	@Override
	public XSettingList<E> range(long fromIndex, long toIndex);

}
