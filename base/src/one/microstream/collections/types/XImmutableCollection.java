package one.microstream.collections.types;

import one.microstream.collections.BulkList;
import one.microstream.functional.Aggregator;
import one.microstream.typing.Immutable;

public interface XImmutableCollection<E> extends XGettingCollection<E>, Immutable
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XImmutableCollection<E> newInstance();
	}


	public static <E> Aggregator<E, XImmutableCollection<E>> Builder()
	{
		return Builder(1);
	}

	public static <E> Aggregator<E, XImmutableCollection<E>> Builder(final long initialCapacity)
	{
		return new Aggregator<E, XImmutableCollection<E>>()
		{
			private final BulkList<E> newInstance = BulkList.New(initialCapacity);

			@Override
			public final void accept(final E element)
			{
				this.newInstance.add(element);
			}

			@Override
			public final XImmutableCollection<E> yield()
			{
				return this.newInstance.immure();
			}
		};
	}



	@Override
	public XImmutableCollection<E> copy();

	/**
	 * Always returns the already immutable collection instance itself
	 * <p>
	 * For spawning a copy of the collection instance, see {@link #copy()}
	 *
	 * @return a reference to the instance itself.
	 * @see #copy()
	 */
	@Override
	public XImmutableCollection<E> immure();

}
