package one.microstream.collections.types;

import java.util.Comparator;

/**
 * Level 1 collection type defining the single demand for the collection's elements to be ordered.
 * <p>
 * Being ordered is defined as: An procedure affecting one element does not affect the order of all other elements.
 * Note that being ordered is not the same as being sorted. Being ordered only defines that there has to be a stable
 * order, while being sorted defines that the order is not only stable but also complies to a certain sorting logic.
 * <p>
 * The concept of being ordered introduces the concept of indexed element accessing as a consequence.
 * <p>
 * Sequence type collections are architectural on par with the other level 1 collection types set and bag.
 * <p>
 * Currently existing subtypes of sequence (level 2 collection types) are list (combining sequence and bag),
 * enum (combining sequence and set) and sortation (enhancing the contract from being ordered to being sorted).
 * <p>
 * Note that all collection types not being a subtype of sequence (like pure set and pure bag subtypes) are rather
 * academic and most probably only reasonably usable for high end performance optimisations. This effectively
 * makes the sequence the dominant level 1 collection type, almost superseding the level 0 collection type collection
 * in practice.
 *
 * 
 */
public interface XSequence<E> extends XBasicSequence<E>, XSortableSequence<E>, XInputtingSequence<E>
{
	public interface Creator<E>
	extends XBasicSequence.Factory<E>, XSortableSequence.Creator<E>, XInputtingSequence.Creator<E>
	{
		@Override
		public XSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XSequence<E> putAll(E... elements);

	@Override
	public XSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XSequence<E> addAll(E... elements);

	@Override
	public XSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSequence<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XSequence<E> copy();

	@Override
	public XSequence<E> toReversed();

	@Override
	public XSequence<E> sort(Comparator<? super E> comparator);

}
