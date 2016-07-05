package net.jadoth.collections.sorting;

import net.jadoth.collections.types.XProcessingSequence;

public interface XProcessingSortation<E> extends XGettingSortation<E>, XRemovingSortation<E>, XProcessingSequence<E>
{
	public interface Factory<E> extends XGettingSortation.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XProcessingSortation<E> newInstance();

	}



	@Override
	public XProcessingSortation<E> copy();

	@Override
	public XProcessingSortation<E> toReversed();

}
