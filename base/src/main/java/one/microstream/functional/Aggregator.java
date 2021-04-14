package one.microstream.functional;

import java.util.function.Consumer;


/**
 * 
 *
 */
@FunctionalInterface
public interface Aggregator<E, R> extends Consumer<E>
{
	public default Aggregator<E, R> reset()
	{
		// no-op in default implementation (no state to reset)
		return this;
	}

	@Override
	public void accept(E element);

	public default R yield()
	{
		return null;
	}



	public interface Creator<E, R>
	{
		public Aggregator<E, R> createAggregator();
	}

}
