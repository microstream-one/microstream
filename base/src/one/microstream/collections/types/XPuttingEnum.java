package one.microstream.collections.types;


public interface XPuttingEnum<E> extends XPuttingSet<E>, XPuttingSequence<E>, XAddingEnum<E>
{
	public interface Creator<E> extends XPuttingSet.Creator<E>, XPuttingSequence.Creator<E>, XAddingEnum.Creator<E>
	{
		@Override
		public XPuttingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPuttingEnum<E> putAll(E... elements);

	@Override
	public XPuttingEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingEnum<E> addAll(E... elements);

	@Override
	public XPuttingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingEnum<E> addAll(XGettingCollection<? extends E> elements);

}
