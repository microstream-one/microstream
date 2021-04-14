package one.microstream.collections.types;


public interface XImmutableBag<E> extends XImmutableCollection<E>, XGettingBag<E>
{
	public interface Factory<E> extends XImmutableCollection.Factory<E>, XGettingBag.Factory<E>
	{
		@Override
		public XImmutableBag<E> newInstance();
	}

	
	
	@Override
	public XImmutableBag<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableBag<E> immure();


}
