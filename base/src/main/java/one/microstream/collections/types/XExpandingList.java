package one.microstream.collections.types;

public interface XExpandingList<E> extends XExtendingList<E>, XPuttingList<E>, XPreputtingList<E>, XExpandingSequence<E>
{
	public interface Factory<E>
	extends
	XExtendingList.Creator<E>,
	XPuttingList.Creator<E>,
	XPreputtingList.Factory<E>,
	XExpandingSequence.Creator<E>
	{
		@Override
		public XExpandingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExpandingList<E> addAll(E... elements);

	@Override
	public XExpandingList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingList<E> putAll(E... elements);

	@Override
	public XExpandingList<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingList<E> prependAll(E... elements);

	@Override
	public XExpandingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingList<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingList<E> preputAll(E... elements);

	@Override
	public XExpandingList<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingList<E> preputAll(XGettingCollection<? extends E> elements);

}
