/**
 *
 */
package one.microstream.util.iterables;

import java.util.Iterator;

/**
 * 
 *
 */
public class DummyIterable<T> implements Iterable<T>
{
	T element;

	@Override
	public Iterator<T> iterator()
	{
		return new DummyIterator();
	}


	public void set(final T element)
	{
		this.element = element;
	}

	public T get()
	{
		return this.element;
	}



	private class DummyIterator implements Iterator<T>
	{
		private boolean hasNext = true;

		DummyIterator()
		{
			super();
		}

		@Override
		public boolean hasNext()
		{
			return this.hasNext;
		}

		@Override
		public T next()
		{
			this.hasNext = false;
			return DummyIterable.this.element;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}
}
