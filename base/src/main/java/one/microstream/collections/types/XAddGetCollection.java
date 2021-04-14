package one.microstream.collections.types;


/**
 * 
 *
 */
public interface XAddGetCollection<E> extends XGettingCollection<E>, XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>, XGettingCollection.Creator<E>
	{
		@Override
		public XAddGetCollection<E> newInstance();
	}

		

	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XAddGetCollection<E> copy();

	@SuppressWarnings("unchecked")
	@Override
	public XAddGetCollection<E> addAll(E... elements);

	@Override
	public XAddGetCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddGetCollection<E> addAll(XGettingCollection<? extends E> elements);
	
}
