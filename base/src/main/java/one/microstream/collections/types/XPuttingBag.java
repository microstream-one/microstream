package one.microstream.collections.types;


public interface XPuttingBag<E> extends XAddingBag<E>, XPuttingCollection<E>
{
	public interface Creator<E> extends XAddingBag.Factory<E>, XPuttingCollection.Creator<E>
	{
		@Override
		public XPuttingBag<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingCollection<E> addAll(E... elements);

	@Override
	public XAddingCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingCollection<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingCollection<E> putAll(E... elements);

	@Override
	public XPuttingCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingCollection<E> putAll(XGettingCollection<? extends E> elements);

}
