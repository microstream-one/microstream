package one.microstream.collections.old;

import java.util.Collection;

import one.microstream.collections.BulkList;
import one.microstream.collections.XArrays;
import one.microstream.collections.types.XGettingCollection;

public interface OldCollection<E> extends Collection<E>
{
	public XGettingCollection<E> parent();

	@Override
	public default <T> T[] toArray(final T[] target)
	{
		XArrays.copyTo(this.parent(), target);
		return target;
	}


	public default void bla()
	{
		final BulkList<String> ps = null;

		final Number[] ns = new Number[5];

		XArrays.copyTo(ps, ns);
	}


}
