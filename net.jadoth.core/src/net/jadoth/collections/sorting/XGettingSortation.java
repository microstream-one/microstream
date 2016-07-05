package net.jadoth.collections.sorting;

import net.jadoth.collections.types.XGettingSequence;

public interface XGettingSortation<E> extends XGettingSequence<E>, Sorted<E>
{
	public interface Factory<E> extends XGettingSequence.Factory<E>
	{
		@Override
		public XGettingSortation<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingSortation<E> copy();

	@Override
	public XGettingSortation<E> toReversed();

}
