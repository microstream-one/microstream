package net.jadoth.collections.old;

import java.util.Collection;

import net.jadoth.collections.types.XGettingCollection;

public interface OldCollection<E> extends Collection<E>
{
	public XGettingCollection<E> parent();
}
