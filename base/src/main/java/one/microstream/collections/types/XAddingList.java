package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedList;



/**
 * 
 *
 */
public interface XAddingList<E> extends XAddingSequence<E>, XAddingBag<E>, ExtendedList<E>
{
	public interface Creator<E> extends XAddingSequence.Creator<E>
	{
		@Override
		public XAddingList<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingList<E> addAll(E... elements);

	@Override
	public XAddingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XAddingList<E> addAll(XGettingCollection<? extends E> elements);

}
