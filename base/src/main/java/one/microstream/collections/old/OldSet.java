package one.microstream.collections.old;

import java.util.Set;

import one.microstream.collections.types.XGettingSet;

public interface OldSet<E> extends Set<E>, OldCollection<E>
{
	@Override
	public XGettingSet<E> parent();
}
