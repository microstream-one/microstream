package one.microstream.collections.types;

public interface XPrependingEnum<E> extends XPrependingSequence<E>
{
	public interface Creator<E> extends XPrependingSequence.Creator<E>
	{
		@Override
		public XPrependingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPrependingEnum<E> prependAll(E... elements);

	@Override
	public XPrependingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPrependingEnum<E> prependAll(XGettingCollection<? extends E> elements);

}
