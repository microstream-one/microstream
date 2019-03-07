package one.microstream.collections.old;

import java.util.List;

import one.microstream.collections.types.XGettingList;

public interface OldList<E> extends List<E>, OldCollection<E>
{
	@Override
	public XGettingList<E> parent();
}
