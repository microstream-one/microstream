package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XPutGetSequence<E> extends XPuttingSequence<E>, XGettingSequence<E>, XPutGetCollection<E>
{
	public interface Factory<E> extends XPuttingSequence.Creator<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XPutGetSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSequence<E> putAll(E... elements);

	@Override
	public XPutGetSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XPutGetSequence<E> addAll(E... elements);

	@Override
	public XPutGetSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XPutGetSequence<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XPutGetSequence<E> copy();

	@Override
	public XPutGetSequence<E> toReversed();

}
