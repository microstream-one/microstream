package net.jadoth.collections.types;

import net.jadoth.collections.interfaces.ExtendedCollection;

public interface XFactory<E>
{
	public ExtendedCollection<E> newInstance();
}
