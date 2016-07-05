package net.jadoth.collections.old;

import java.util.Set;

import net.jadoth.collections.types.XGettingSet;

public interface OldSet<E> extends Set<E>, OldCollection<E>
{
	@Override
	public XGettingSet<E> parent();
}
