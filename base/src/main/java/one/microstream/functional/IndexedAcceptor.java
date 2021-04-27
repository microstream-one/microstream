package one.microstream.functional;

/**
 * Additionally to the element to accept, this class' {@link IndexedAcceptor#accept(Object, long)} method,
 * uses the coherent index of the given element.
 * 
 * @param <T> type of element to accept
 *
 */
public interface IndexedAcceptor<T>
{
	/**
	 * Expects the element and its coherent index.
	 * 
	 * @param e element which is expected at the given index
	 * @param index on which the element is expected
	 */
	public void accept(T e, long index);
}
