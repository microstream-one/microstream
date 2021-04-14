package one.microstream.collections.types;





/**
 * 
 *
 */
public interface XAddingSet<E> extends XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XAddingSet<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingSet<E> addAll(E... elements);

	@Override
	public XAddingSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingSet<E> addAll(XGettingCollection<? extends E> elements);
	
}
