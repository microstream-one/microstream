package one.microstream.functional;

/**
 * 
 *
 */
public interface IndexedAcceptor<T>
{
	public void accept(T e, long index);
}
