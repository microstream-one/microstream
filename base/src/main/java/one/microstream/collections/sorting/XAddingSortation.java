package one.microstream.collections.sorting;

import one.microstream.collections.types.XAddingSequence;
import one.microstream.collections.types.XGettingCollection;

public interface XAddingSortation<E> extends XAddingSequence<E>, Sorted<E>
{
	public interface Factory<E> extends XAddingSequence.Creator<E>
	{
		@Override
		public XAddingSortation<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingSortation<E> addAll(E... elements);
	@Override
	public XAddingSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	@Override
	public XAddingSortation<E> addAll(XGettingCollection<? extends E> elements);

}
