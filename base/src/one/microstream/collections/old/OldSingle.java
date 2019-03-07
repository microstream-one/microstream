package one.microstream.collections.old;

import java.util.Spliterator;

import one.microstream.collections.Single;

public interface OldSingle<E> extends OldList<E>, OldSet<E>
{
	@Override
	public Single<E> parent();

	@Override
	public default Spliterator<E> spliterator()
	{
		return OldList.super.spliterator();
	}
}
