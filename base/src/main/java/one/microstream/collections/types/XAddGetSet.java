package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XAddGetSet<E> extends XAddingSet<E>, XGettingSet<E>, XAddGetCollection<E>
{
	public interface Factory<E> extends XAddingSet.Creator<E>, XGettingSet.Creator<E>, XAddGetCollection.Creator<E>
	{
		@Override
		public XAddGetSet<E> newInstance();
	}



	public E addGet(E element);
	
	public E deduplicate(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XAddGetSet<E> addAll(E... elements);

	@Override
	public XAddGetSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddGetSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XAddGetSet<E> copy();

}
