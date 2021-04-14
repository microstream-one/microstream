package one.microstream.collections.types;


/**
 * Bag type collections make the single demand (thus being a level 1 collection type) that duplicate elements have
 * to be allowed, effectively being the opposite to set type collections.
 * <p>
 * The naming for the type is based on the conception that a bag can contain any elements (including duplicates),
 * but is definitely not ordered.
 * <p>
 * Apologies to the Apache guys (but honestly: why should the conception of a "bag" imply that its contained elements
 * are counted? Apart from that, counting elements is such a specific addon logic, that the proper approach would be
 * to extend an existing type, like CountingList, etc. but surely not "bag", which implies unordered content).
 * <p>
 * This will probably be a rather academic type and has been introduced more for reasons of completeness of the
 * typing architecture, as in practice, list type collections will be preferred to pure bag type collections.
 * <p>
 * Bag type collections are architectural on par with the other level 1 collection types set and sequence.
 * <p>
 * Currently, the only known to be useful subtype of a bag is the level 2 collection type list, combining bag
 * and sequence (order of elements).
 *
 * @see XSet
 * @see XSequence
 * @see XList
 *
 * 
 */
public interface XBag<E> extends XPutGetBag<E>, XProcessingBag<E>, XCollection<E>
{
	public interface Factory<E> extends XPutGetBag.Factory<E>, XProcessingBag.Factory<E>, XCollection.Factory<E>
	{
		@Override
		public XBag<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XBag<E> putAll(E... elements);

	@Override
	public XBag<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBag<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBag<E> addAll(E... elements);

	@Override
	public XBag<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBag<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBag<E> copy();

}
