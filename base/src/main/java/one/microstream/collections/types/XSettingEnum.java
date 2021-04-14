package one.microstream.collections.types;

import java.util.Comparator;

/**
 * 
 *
 */
public interface XSettingEnum<E> extends XSortableEnum<E>, XSettingSequence<E>
{
	public interface Creator<E> extends XSortableEnum.Creator<E>, XSettingSequence.Creator<E>
	{
		@Override
		public XSettingEnum<E> newInstance();
	}



	@Override
	public E setGet(long index, E element);

	// intentionally not returning old element for performance reasons. set(int, E) does that already.
	@Override
	public void setFirst(E element);

	@Override
	public void setLast(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XSettingEnum<E> setAll(long index, E... elements);

	@Override
	public XSettingEnum<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XSettingEnum<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);



	@Override
	public XSettingEnum<E> swap(long indexA, long indexB);

	@Override
	public XSettingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XSettingEnum<E> reverse();

	@Override
	public XSettingEnum<E> sort(Comparator<? super E> comparator);

	@Override
	public XSettingEnum<E> copy();

	@Override
	public XSettingEnum<E> toReversed();

	@Override
	public XSettingEnum<E> range(long fromIndex, long toIndex);

}
