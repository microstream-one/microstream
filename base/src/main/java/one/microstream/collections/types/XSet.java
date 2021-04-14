package one.microstream.collections.types;



/**
 * 
 *
 */
public interface XSet<E> extends XCollection<E>, XPutGetSet<E>, XProcessingSet<E>
{
	public interface Factory<E> extends XCollection.Factory<E>, XPutGetSet.Factory<E>, XProcessingSet.Factory<E>
	{
		@Override
		public XSet<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XSet<E> putAll(E... elements);

	@Override
	public XSet<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSet<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XSet<E> addAll(E... elements);

	@Override
	public XSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XSet<E> copy();

}
