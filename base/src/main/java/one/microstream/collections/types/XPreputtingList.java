package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedList;

public interface XPreputtingList<E> extends XPreputtingSequence<E>, ExtendedList<E>
{
	public interface Factory<E> extends XPreputtingSequence.Creator<E>
	{
		@Override
		public XPreputtingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingList<E> prependAll(E... elements);

	@Override
	public XPreputtingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingList<E> preputAll(E... elements);

	@Override
	public XPreputtingList<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPreputtingList<E> preputAll(XGettingCollection<? extends E> elements);

}
