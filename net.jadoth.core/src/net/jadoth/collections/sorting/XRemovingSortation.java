package net.jadoth.collections.sorting;

import net.jadoth.collections.types.XRemovingSequence;

public interface XRemovingSortation<E> extends XRemovingSequence<E>, Sorted<E>
{
	public interface Factory<E> extends XRemovingSequence.Factory<E>
	{
		@Override
		public XRemovingSortation<E> newInstance();
	}

}
