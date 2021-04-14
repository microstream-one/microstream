package one.microstream.collections.types;


/**
 * 
 *
 */
public interface XPutGetCollection<E> extends XAddGetCollection<E>, XPuttingCollection<E>
{
	public interface Creator<E> extends XPuttingCollection.Creator<E>, XAddGetCollection.Creator<E>
	{
		@Override
		public XPutGetCollection<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XPutGetCollection<E> copy();

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetCollection<E> addAll(E... elements);

	@Override
	public XPutGetCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetCollection<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	@SuppressWarnings("unchecked")
	public XPutGetCollection<E> putAll(E... elements);
	
	@Override
	public XPutGetCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetCollection<E> putAll(XGettingCollection<? extends E> elements);
	
}
