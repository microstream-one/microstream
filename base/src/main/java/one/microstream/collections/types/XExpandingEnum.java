package one.microstream.collections.types;

public interface XExpandingEnum<E> extends XExtendingEnum<E>, XPuttingEnum<E>, XPreputtingEnum<E>, XExpandingSequence<E>
{
	public interface Creator<E>
	extends
	XExtendingEnum.Creator<E>,
	XPuttingEnum.Creator<E>,
	XPreputtingEnum.Creator<E>,
	XExpandingSequence.Creator<E>
	{
		@Override
		public XExpandingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> addAll(E... elements);

	@Override
	public XExpandingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> putAll(E... elements);

	@Override
	public XExpandingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> prependAll(E... elements);

	@Override
	public XExpandingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExpandingEnum<E> preputAll(E... elements);

	@Override
	public XExpandingEnum<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XExpandingEnum<E> preputAll(XGettingCollection<? extends E> elements);

}
