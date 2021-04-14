/**
 *
 */
package one.microstream.util.iterables;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 */
public class ChainedArraysIterable<T> implements Iterable<T>
{
	final T[][] iterables;

	@SafeVarargs
	public ChainedArraysIterable(final T[]... iterables)
	{
		super();
		this.iterables = iterables;
	}

	@Override
	public Iterator<T> iterator()
	{
		return new ChainedIterator();
	}


	protected class ChainedIterator implements Iterator<T>
	{
		private int currentArrayMasterIndex = -1;
		private int currentArrayAccessIndex;
		private T[] currentArray           ;
		{
			this.nextArray();
		}

		@Override
		public boolean hasNext()
		{
			if(this.currentArrayAccessIndex < this.currentArray.length)
			{
				return true;
			}

			while(this.nextArray())
			{
				if(this.currentArrayAccessIndex < this.currentArray.length)
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public T next()
		{
			try
			{
				return this.currentArray[this.currentArrayAccessIndex++];
			}
			catch(final ArrayIndexOutOfBoundsException e)
			{
				throw new NoSuchElementException();
			}
		}

		protected boolean nextArray()
		{
			this.currentArrayAccessIndex = 0;
			final T[][] iterables = ChainedArraysIterable.this.iterables;
			int loopIndex = this.currentArrayMasterIndex;
			T[] loopIterable = null;
			while(loopIterable == null)
			{
				loopIndex++;
				if(loopIndex == iterables.length)
				{
					return false;
				}
				loopIterable = iterables[loopIndex];
			}
			this.currentArray = loopIterable;
			this.currentArrayMasterIndex = loopIndex;

			return true;
		}

		@Override
		public void remove()
		{
			this.currentArray[this.currentArrayAccessIndex] = null;
		}

	}

}
