package one.microstream.collections.sorting;

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XGettingSequence;

public interface XPutGetSortation<E> extends XGettingSequence<E>, XPuttingSortation<E>
{
	public interface Factory<E> extends XGettingSequence.Factory<E>, XPuttingSortation.Factory<E>
	{
		@Override
		public XPutGetSortation<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XPutGetSortation<E> copy();

	@Override
	public XPutGetSortation<E> toReversed();

	@Override
	@SuppressWarnings("unchecked")
	public XPutGetSortation<E> addAll(E... elements);
	
	@Override
	public XPutGetSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetSortation<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSortation<E> putAll(E... elements);
	
	@Override
	public XPutGetSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XPutGetSortation<E> putAll(XGettingCollection<? extends E> elements);

}
