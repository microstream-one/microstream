package one.microstream.collections.types;

import one.microstream.collections.sorting.XLadder;


/**
 * Intermediate list type providing getting, adding, removing concerns to act as a common super type for
 * {@link XList} and {@link XLadder}. This is necessary because {@link XLadder} cannot provide
 * the otherwise typical list concerns like inserting, ordering, setting due to the limitations of the characteristic
 * of being always sorted.
 *
 * 
 *
 * @param <E>
 */
public interface XBasicEnum<E> extends XSet<E>, XBasicSequence<E>, XPutGetEnum<E>, XProcessingEnum<E>
{
	public interface Creator<E> extends XSet.Factory<E>, XBasicSequence.Factory<E>, XPutGetEnum.Creator<E>, XProcessingEnum.Creator<E>
	{
		@Override
		public XBasicEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XBasicEnum<E> putAll(E... elements);

	@Override
	public XBasicEnum<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicEnum<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBasicEnum<E> addAll(E... elements);

	@Override
	public XBasicEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicEnum<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBasicEnum<E> copy();

	@Override
	public XBasicEnum<E> toReversed();

	@Override
	public XImmutableEnum<E> immure();

}
