package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedSequence;





/**
 * 
 *
 */
public interface XAddingSequence<E> extends XAddingCollection<E>, ExtendedSequence<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XAddingSequence<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingSequence<E> addAll(E... elements);

	@Override
	public XAddingSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingSequence<E> addAll(XGettingCollection<? extends E> elements);

}
