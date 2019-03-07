package net.jadoth.collections.old;

import java.util.List;

import net.jadoth.collections.types.XGettingList;

public interface OldList<E> extends List<E>, OldCollection<E>
{
	@Override
	public XGettingList<E> parent();
}
