/**
 * 
 */
package net.jadoth.util.iterables;

import java.util.Iterator;

/**
 * @author Thomas Muenz
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
	 * @return
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator()
	{
		return new ArrayIterator<>(this.array);
	}
	
}
