package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XPutGetList<E> extends XPuttingList<E>, XGettingList<E>, XPutGetSequence<E>, XPutGetBag<E>
{
	public interface Factory<E>
	extends XPuttingList.Creator<E>, XGettingList.Factory<E>, XPutGetSequence.Factory<E>, XPutGetBag.Factory<E>
	{
		@Override
		public XPutGetList<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XPutGetList<E> copy();

	@Override
	public XPutGetList<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetList<E> putAll(E... elements);

	@Override
	public XPutGetList<E> putAll(E[] elements, int offset, int length);

	@Override
	public XPutGetList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetList<E> addAll(E... elements);

	@Override
	public XPutGetList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetList<E> addAll(XGettingCollection<? extends E> elements);

}
