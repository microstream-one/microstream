package one.microstream.collections.types;

import one.microstream.collections.interfaces.ExtendedCollection;

public interface XFactory<E>
{
	public ExtendedCollection<E> newInstance();
}
