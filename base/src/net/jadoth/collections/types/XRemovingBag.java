package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ExtendedBag;

public interface XRemovingBag<E> extends XRemovingCollection<E>, ExtendedBag<E>
{
	public interface Factory<E> extends XRemovingCollection.Factory<E>
	{
		@Override
		public XRemovingBag<E> newInstance();
	}
}
