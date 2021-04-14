package one.microstream.collections.types;

import one.microstream.functional.Aggregator;



/**
 * A collection is the root type for all collections (level 0 collection type).
 *
 *
 * 
 *
 */
public interface XCollection<E> extends XPutGetCollection<E>, XProcessingCollection<E>
{
	public interface Factory<E> extends XProcessingCollection.Factory<E>, XPutGetCollection.Creator<E>
	{
		@Override
		public XCollection<E> newInstance();
	}



	@Override
	public default Aggregator<E, ? extends XCollection<E>> collector()
	{
		return new Aggregator<E, XCollection<E>>()
		{
			@Override
			public void accept(final E element)
			{
				XCollection.this.add(element);
			}

			@Override
			public XCollection<E> yield()
			{
				return XCollection.this;
			}
		};
	}


	@SuppressWarnings("unchecked")
	@Override
	public XCollection<E> putAll(E... elements);

	@Override
	public XCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XCollection<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XCollection<E> addAll(E... elements);
	@Override

	public XCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XCollection<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XCollection<E> copy();

}
