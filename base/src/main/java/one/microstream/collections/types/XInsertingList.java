package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XInsertingList<E> extends XInsertingSequence<E>, XExtendingList<E>
{
	public interface Creator<E> extends XInsertingSequence.Creator<E>, XExtendingList.Creator<E>
	{
		@Override
		public XInsertingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInsertingList<E> addAll(E... elements);

	@Override
	public XInsertingList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingList<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInsertingList<E> prependAll(E... elements);

	@Override
	public XInsertingList<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingList<E> prependAll(XGettingCollection<? extends E> elements);

}
