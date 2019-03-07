package one.microstream.functional;

/**
 * @author Thomas Muenz
 *
 */
public interface IndexedAcceptor<T>
{
	public void accept(T e, long index);
}
