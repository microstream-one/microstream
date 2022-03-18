package one.microstream.util.iterables;

/*-
 * #%L
 * microstream-base
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.util.Iterator;
import java.util.NoSuchElementException;

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
