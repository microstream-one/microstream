package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XInsertingEnum<E> extends XInsertingSequence<E>, XExtendingEnum<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>
	{
		@Override
		public XInsertingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInsertingEnum<E> addAll(E... elements);

	@Override
	public XInsertingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInsertingEnum<E> prependAll(E... elements);

	@Override
	public XInsertingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingEnum<E> prependAll(XGettingCollection<? extends E> elements);

}
