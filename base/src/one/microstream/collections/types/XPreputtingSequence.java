package one.microstream.collections.types;

public interface XPreputtingSequence<E> extends XPrependingSequence<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPreputtingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPreputtingSequence<E> prependAll(E... elements);

	@Override
	public XPreputtingSequence<E> prependAll(E[] elements, int offset, int length);

	@Override
	public XPreputtingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	public boolean preput(E element);

	public boolean nullPreput();

	@SuppressWarnings("unchecked")
	public XPreputtingSequence<E> preputAll(E... elements);

	public XPreputtingSequence<E> preputAll(E[] elements, int offset, int length);

	public XPreputtingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
