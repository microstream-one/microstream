package one.microstream.collections.sorting;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XPuttingSequence;

public interface XPuttingSortation<E> extends XAddingSortation<E>, XPuttingSequence<E>
{
	public interface Factory<E> extends XAddingSortation.Factory<E>, XPuttingSequence.Creator<E>
	{
		@Override
		public XPuttingSortation<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSortation<E> addAll(E... elements);
	@Override
	public XPuttingSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XPuttingSortation<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSortation<E> putAll(E... elements);
	@Override
	public XPuttingSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XPuttingSortation<E> putAll(XGettingCollection<? extends E> elements);

}
