package net.jadoth.collections.old;

import java.util.Collection;

import net.jadoth.collections.BulkList;
import net.jadoth.collections.JadothArrays;
import net.jadoth.collections.types.XGettingCollection;

public interface OldCollection<E> extends Collection<E>
{
	public XGettingCollection<E> parent();

	@Override
	public default <T> T[] toArray(final T[] target)
	{
		JadothArrays.copyTo(this.parent(), target);
		return target;
	}


	public default void bla()
	{
		final BulkList<String> ps = null;

		final Number[] ns = new Number[5];

		JadothArrays.copyTo(ps, ns);
	}


}
