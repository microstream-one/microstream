/**
 * 
 */
package one.microstream.util.iterables;

import java.util.Iterator;

/**
 * 
 *
 */
public class ArrayIterable<T> implements Iterable<T>
{
	private final T[] array;

	public ArrayIterable(final T[] array)
	{
		super();
		if(array == null)
		{
			throw new NullPointerException("array may not be null");
		}
		this.array = array;
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<>(this.array);
	}
	
}
