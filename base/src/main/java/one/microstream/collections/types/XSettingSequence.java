package one.microstream.collections.types;

import java.util.Comparator;

import one.microstream.collections.interfaces.ReleasingCollection;

/**
 * 
 *
 */
public interface XSettingSequence<E> extends XSortableSequence<E>, ReleasingCollection<E>
{
	public interface Creator<E> extends XSortableSequence.Creator<E>
	{
		@Override
		public XSettingSequence<E> newInstance();
	}


	public boolean set(long index, E element);

	public E setGet(long index, E element);

	// intentionally not returning old element for performance reasons. set(int, E) does that already.
	public void setFirst(E element);

	public void setLast(E element);

	@SuppressWarnings("unchecked")
	public XSettingSequence<E> setAll(long index, E... elements);

	public XSettingSequence<E> set(long index, E[] elements, int offset, int length);

	public XSettingSequence<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);



	@Override
	public XSettingSequence<E> swap(long indexA, long indexB);

	@Override
	public XSettingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XSettingSequence<E> reverse();

	@Override
	public XSettingSequence<E> sort(Comparator<? super E> comparator);

	@Override
	public XSettingSequence<E> copy();

	@Override
	public XSettingSequence<E> toReversed();

	@Override
	public XSettingSequence<E> range(long fromIndex, long toIndex);

}
