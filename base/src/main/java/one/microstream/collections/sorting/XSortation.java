package one.microstream.collections.sorting;

import one.microstream.collections.types.XBasicSequence;
import one.microstream.collections.types.XGettingCollection;

/**
 * Actually being a "Collation" (a collection of elements to which a sortation is applied), this type has been named
 * "Sortation" nevertheless to avoid the mistakable similarity to the basic collection type "Collection" in reading,
 * writing, talking and IntelliSense filtering.
 * <p>
 * On a funny side note:
 *
 * 
 *
 * @param <E>
 */
public interface XSortation<E> extends XBasicSequence<E>, XPutGetSortation<E>, XProcessingSortation<E>
{
	public interface Factory<E>
	extends XBasicSequence.Factory<E>, XPutGetSortation.Factory<E>, XProcessingSortation.Factory<E>
	{
		@Override
		public XSortation<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSortation<E> copy();

	@Override
	public XSortation<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XSortation<E> putAll(E... elements);
	
	@Override
	public XSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XSortation<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XSortation<E> addAll(E... elements);
	
	@Override
	public XSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XSortation<E> addAll(XGettingCollection<? extends E> elements);

}
