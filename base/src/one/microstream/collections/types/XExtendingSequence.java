package one.microstream.collections.types;

public interface XExtendingSequence<E> extends XAddingSequence<E>, XPrependingSequence<E>
{
	public interface Creator<E> extends XAddingSequence.Creator<E>, XPrependingSequence.Creator<E>
	{
		@Override
		public XExtendingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingSequence<E> addAll(E... elements);

	@Override
	public XExtendingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingSequence<E> prependAll(E... elements);

	@Override
	public XExtendingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExtendingSequence<E> prependAll(XGettingCollection<? extends E> elements);

}
