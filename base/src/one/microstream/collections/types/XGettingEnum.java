package one.microstream.collections.types;

import java.util.function.Consumer;

public interface XGettingEnum<E> extends XGettingSet<E>, XGettingSequence<E>
{
	public interface Creator<E> extends XGettingSet.Creator<E>, XGettingSequence.Factory<E>
	{
		@Override
		public XGettingEnum<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableEnum<E> immure();

	@Override
	public XGettingEnum<E> copy();

	@Override
	public XGettingEnum<E> toReversed();

	@Override
	public XGettingEnum<E> view();

	@Override
	public XGettingEnum<E> view(long lowIndex, long highIndex);

	@Override
	public XGettingEnum<E> range(long lowIndex, long highIndex);

	@Override
	public <P extends Consumer<? super E>> P iterate(P procedure);

}
