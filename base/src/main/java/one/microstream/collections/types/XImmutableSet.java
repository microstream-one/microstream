package one.microstream.collections.types;


public interface XImmutableSet<E> extends XImmutableCollection<E>, XGettingSet<E>
{
	public interface Factory<E> extends XImmutableCollection.Factory<E>, XGettingSet.Creator<E>
	{
		@Override
		public XImmutableSet<E> newInstance();
	}



	@Override
	public XImmutableSet<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableSet<E> immure();

}
