package one.microstream.collections.types;




/**
 * 
 *
 */
public interface XPuttingList<E> extends XAddingList<E>, XPuttingBag<E>, XPuttingSequence<E>
{
	public interface Creator<E> extends XAddingList.Creator<E>, XPuttingBag.Creator<E>, XPuttingSequence.Creator<E>
	{
		@Override
		public XPuttingList<E> newInstance();
	}


	@SuppressWarnings("unchecked")
	@Override
	public XPuttingList<E> addAll(E... elements);

	@Override
	public XPuttingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XPuttingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPuttingList<E> putAll(E... elements);

	@Override
	public XPuttingList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XPuttingList<E> putAll(XGettingCollection<? extends E> elements);

}
