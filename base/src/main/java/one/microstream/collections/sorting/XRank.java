package one.microstream.collections.sorting;

import one.microstream.collections.types.XBasicEnum;
import one.microstream.collections.types.XGettingCollection;

public interface XRank<E> extends XSortation<E>, XBasicEnum<E>
{
	public interface Factory<E> extends XSortation.Factory<E>, XBasicEnum.Creator<E>
	{
		@Override
		public XRank<E> newInstance();

	}



	@Override
	public XRank<E> copy();

	@Override
	public XRank<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XRank<E> addAll(E... elements);
	
	@Override
	public XRank<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XRank<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XRank<E> putAll(E... elements);
	
	@Override
	public XRank<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XRank<E> putAll(XGettingCollection<? extends E> elements);

}
