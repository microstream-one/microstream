package one.microstream.collections.types;


public interface XImmutableSequence<E> extends XImmutableCollection<E>, XGettingSequence<E>
{
	public interface Factory<E> extends XImmutableCollection.Factory<E>, XGettingSequence.Factory<E>
	{
		@Override
		public XImmutableSequence<E> newInstance();
	}



	@Override
	public XImmutableSequence<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableSequence<E> immure();

	@Override
	public XImmutableSequence<E> toReversed();

}
