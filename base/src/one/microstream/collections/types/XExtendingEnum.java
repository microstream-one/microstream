package one.microstream.collections.types;

public interface XExtendingEnum<E> extends XExtendingSequence<E>, XAddingEnum<E>, XPrependingEnum<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>, XAddingEnum.Creator<E>, XPrependingEnum.Creator<E>
	{
		@Override
		public XExtendingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XExtendingEnum<E> addAll(E... elements);
	
	@Override
	public XExtendingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XExtendingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XExtendingEnum<E> prependAll(E... elements);
	
	@Override
	public XExtendingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XExtendingEnum<E> prependAll(XGettingCollection<? extends E> elements);

}
