package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ExtendedSequence;





/**
 * @author Thomas Muenz
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
