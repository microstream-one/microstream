package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XInputtingSequence<E> extends XInsertingSequence<E>, XExpandingSequence<E>
{
	public interface Creator<E> extends XInsertingSequence.Creator<E>, XExpandingSequence.Creator<E>
	{
		@Override
		public XInputtingSequence<E> newInstance();
	}



	public boolean input(long index, E element);

	public boolean nullInput(long index);

	@SuppressWarnings("unchecked")
	public long inputAll(long index, E... elements);

	public long inputAll(long index, E[] elements, int offset, int length);

	public long inputAll(long index, XGettingCollection<? extends E> elements);



	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> addAll(E... elements);

	@Override
	public XInputtingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")

	@Override
	public XInputtingSequence<E> putAll(E... elements);

	@Override
	public XInputtingSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> prependAll(E... elements);

	@Override
	public XInputtingSequence<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> prependAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInputtingSequence<E> preputAll(E... elements);

	@Override
	public XInputtingSequence<E> preputAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInputtingSequence<E> preputAll(XGettingCollection<? extends E> elements);

}
