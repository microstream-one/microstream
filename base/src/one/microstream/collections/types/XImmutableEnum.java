package one.microstream.collections.types;


public interface XImmutableEnum<E> extends XImmutableSequence<E>, XImmutableSet<E>, XGettingEnum<E>
{
	public interface Factory<E> extends XImmutableSequence.Factory<E>, XImmutableSet.Factory<E>, XGettingEnum<E>
	{
		@Override
		public XImmutableEnum<E> newInstance();
	}


	
	@Override
	public XImmutableEnum<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableEnum<E> immure();

	@Override
	public XImmutableEnum<E> toReversed();

}
