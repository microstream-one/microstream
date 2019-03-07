package one.microstream.collections.types;

public interface XExtendingList<E> extends XExtendingSequence<E>, XAddingList<E>, XPrependingList<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>, XAddingList.Creator<E>, XPrependingList.Creator<E>
	{
		@Override
		public XExtendingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingList<E> addAll(E... elements);

	@Override
	public XExtendingList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingList<E> prependAll(E... elements);

	@Override
	public XExtendingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingList<E> prependAll(XGettingCollection<? extends E> elements);

}
