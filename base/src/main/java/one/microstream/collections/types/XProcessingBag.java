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

	@Override
	public XImmutableBag<E> immure();

}
