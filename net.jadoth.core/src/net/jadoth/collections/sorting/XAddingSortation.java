package net.jadoth.collections.sorting;

import net.jadoth.collections.types.XAddingSequence;
import net.jadoth.collections.types.XGettingCollection;

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
