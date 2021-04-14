package one.microstream.collections.types;


public interface XImmutableList<E> extends XImmutableSequence<E>, XImmutableBag<E>, XGettingList<E>
{
	public interface Factory<E> extends XImmutableSequence.Factory<E>, XGettingList.Factory<E>, XImmutableBag.Factory<E>
	{
		@Override
		public XImmutableList<E> newInstance();
	}


	
	@Override
	public XImmutableList<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableList<E> immure();

	@Override
	public XImmutableList<E> toReversed();

	@Override
	public XImmutableList<E> range(long fromIndex, long toIndex);

	@Override
	public XImmutableList<E> view();

	@Override
	public XImmutableList<E> view(long lowIndex, long highIndex);
}
