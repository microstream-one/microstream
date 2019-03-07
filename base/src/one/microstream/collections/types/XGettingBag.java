package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedBag;


public interface XGettingBag<E> extends XGettingCollection<E>, ExtendedBag<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingBag<E> newInstance();
	}

	
	
	@Override
	public XGettingBag<E> copy();

	@Override
	public XGettingBag<E> view();

	@Override
	public XImmutableBag<E> immure();

}
