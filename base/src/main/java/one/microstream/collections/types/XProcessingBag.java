package one.microstream.collections.types;



public interface XProcessingBag<E> extends XRemovingBag<E>, XGettingBag<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingBag.Factory<E>, XGettingBag.Factory<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingBag<E> newInstance();
	}



	@Override
	public XProcessingBag<E> copy();

	@Override
	public XGettingBag<E> view();


	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	@Override
	public XImmutableBag<E> immure();

}
