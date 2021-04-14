package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XInsertingSequence<E> extends XExtendingSequence<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>
	{
		@Override
		public XInsertingSequence<E> newInstance();
	}



	public boolean insert(long index, E element);

	public boolean nullInsert(long index);

	@SuppressWarnings("unchecked")
	public long insertAll(long index, E... elements);

	public long insertAll(long index, E[] elements, int offset, int length);

	public long insertAll(long index, XGettingCollection<? extends E> elements);



	@SuppressWarnings("unchecked")
	@Override
	public XInsertingSequence<E> addAll(E... elements);

	@Override
	public XInsertingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInsertingSequence<E> prependAll(E... elements);

	@Override
	public XInsertingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingSequence<E> prependAll(XGettingCollection<? extends E> elements);

}
