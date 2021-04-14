package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XPutGetSet<E> extends XPuttingSet<E>, XAddGetSet<E>, XPutGetCollection<E>
{
	public interface Factory<E> extends XPuttingSet.Creator<E>, XGettingSet.Creator<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XPutGetSet<E> newInstance();
	}



	public E putGet(E element);
	
	public E replace(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSet<E> putAll(E... elements);

	@Override
	public XPutGetSet<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSet<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSet<E> addAll(E... elements);

	@Override
	public XPutGetSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XPutGetSet<E> copy();

}
