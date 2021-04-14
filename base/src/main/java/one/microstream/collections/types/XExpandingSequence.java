package one.microstream.collections.types;

public interface XExpandingSequence<E> extends XExtendingSequence<E>, XPuttingSequence<E>, XPreputtingSequence<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>, XPuttingSequence.Creator<E>, XPreputtingSequence.Creator<E>
	{
		@Override
		public XExpandingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExpandingSequence<E> addAll(E... elements);

	@Override
	public XExpandingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingSequence<E> putAll(E... elements);

	@Override
	public XExpandingSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingSequence<E> prependAll(E... elements);

	@Override
	public XExpandingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingSequence<E> preputAll(E... elements);

	@Override
	public XExpandingSequence<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
