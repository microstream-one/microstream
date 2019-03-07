package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ExtendedList;



/**
 * @author Thomas Muenz
 *
 */
public interface XAddingList<E> extends XAddingSequence<E>, XAddingBag<E>, ExtendedList<E>
{
	public interface Creator<E> extends XAddingSequence.Creator<E>
	{
		@Override
		public XAddingList<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XAddingList<E> addAll(E... elements);

	@Override
	public XAddingList<E> addAll(E[] elements, int offset, int length);

	@Override
	public XAddingList<E> addAll(XGettingCollection<? extends E> elements);

}
