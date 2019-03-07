package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedList;

public interface XPrependingList<E> extends XPrependingSequence<E>, ExtendedList<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPrependingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPrependingList<E> prependAll(E... elements);

	@Override
	public XPrependingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPrependingList<E> prependAll(XGettingCollection<? extends E> elements);

}
