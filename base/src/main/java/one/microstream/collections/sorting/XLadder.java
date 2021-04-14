package one.microstream.collections.sorting;

import one.microstream.collections.types.XBasicList;
import one.microstream.collections.types.XGettingCollection;

public interface XLadder<E> extends XSortation<E>, XBasicList<E>
{
	public interface Factory<E> extends XSortation.Factory<E>, XBasicList.Creator<E>
	{
		@Override
		public XLadder<E> newInstance();

	}



	@Override
	public XLadder<E> copy();

	@Override
	public XLadder<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XLadder<E> putAll(E... elements);
	@Override
	public XLadder<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XLadder<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XLadder<E> addAll(E... elements);
	@Override
	public XLadder<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XLadder<E> addAll(XGettingCollection<? extends E> elements);

}
