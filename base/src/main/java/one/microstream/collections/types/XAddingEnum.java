package one.microstream.collections.types;


public interface XAddingEnum<E> extends XAddingSet<E>, XAddingSequence<E>
{
	public interface Creator<E> extends XAddingSet.Creator<E>, XAddingSequence.Creator<E>
	{
		@Override
		public XAddingEnum<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingEnum<E> addAll(E... elements);

	@Override
	public XAddingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingEnum<E> addAll(XGettingCollection<? extends E> elements);

}
