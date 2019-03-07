package one.microstream.functional;


/**
 * @author Thomas Muenz
 *
 */
public interface IndexedAggregator<E, R> extends IndexedAcceptor<E>
{
	@Override
	public void accept(E e, long index);

	public R yield();



	public interface Creator<E, R>
	{
		public IndexedAggregator<E, R> createAggregator();
	}

}
