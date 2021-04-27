package one.microstream.collections.types;

import java.util.function.BiConsumer;

public interface XJoinable<E>
{
	/**
	 * Iterates over all elements of the collections and calls the joiner
	 * with each element and the aggregate.
	 * 
	 * @param joiner is the actual function to do the joining
	 * @param aggregate where to join into
	 * @param <E> type of data to join
	 * @param <A> type of aggregate
	 */
	public <A> A join(BiConsumer<? super E, ? super A> joiner, A aggregate);
}
