package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedBag;


public interface XAddingBag<E> extends XAddingCollection<E>, ExtendedBag<E>
{
	public interface Factory<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XAddingBag<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XAddingCollection<E> addAll(E... elements);

	@Override
	public XAddingCollection<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddingCollection<E> addAll(XGettingCollection<? extends E> elements);

}
