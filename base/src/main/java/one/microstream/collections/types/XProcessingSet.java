package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XProcessingSet<E> extends XRemovingSet<E>, XGettingSet<E>, XProcessingCollection<E>
{
	public interface Factory<E>
	extends XRemovingSet.Factory<E>, XGettingSet.Creator<E>, XProcessingCollection.Factory<E>
	{
		@Override
		public XProcessingSet<E> newInstance();
	}

	@Override
	public XImmutableSet<E> immure();

	@Override
	public XProcessingSet<E> copy();

}
