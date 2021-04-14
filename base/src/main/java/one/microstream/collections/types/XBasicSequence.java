package one.microstream.collections.types;

import one.microstream.collections.sorting.XSortation;

/**
 * Intermediate sequence type providing getting, adding, removing concerns to act as a common super type for
 * {@link XSequence} and {@link XSortation}. This is necessary because {@link XSortation} cannot provide
 * the otherwise typical sequence concerns like inserting and ordering due to the limitations of the characteristic
 * of being always sorted.
 *
 * 
 *
 * @param <E>
 */
public interface XBasicSequence<E> extends XCollection<E>, XPutGetSequence<E>, XProcessingSequence<E>
{
	public interface Factory<E> extends XCollection.Factory<E>, XPutGetSequence.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XBasicSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XBasicSequence<E> putAll(E... elements);

	@Override
	public XBasicSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBasicSequence<E> addAll(E... elements);

	@Override
	public XBasicSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicSequence<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBasicSequence<E> copy();

	@Override
	public XBasicSequence<E> toReversed();
}
