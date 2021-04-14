package one.microstream.collections.types;





/**
 * 
 *
 */
public interface XPuttingSequence<E> extends XPuttingCollection<E>, XAddingSequence<E>
{
	public interface Creator<E> extends XPuttingCollection.Creator<E>, XAddingSequence.Creator<E>
	{
		@Override
		public XPuttingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPuttingSequence<E> putAll(E... elements);

	@Override
	public XPuttingSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPuttingSequence<E> putAll(XGettingCollection<? extends E> elements);

}
