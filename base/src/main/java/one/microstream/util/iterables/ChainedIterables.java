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

import java.util.ArrayList;
import java.util.Iterator;

public class ChainedIterables<T> implements Iterable<T>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	final ArrayList<Iterable<T>> iterables;

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	@SafeVarargs
	public ChainedIterables(final Iterable<T>... iterables)
	{
		super();
		final ArrayList<Iterable<T>> set = new ArrayList<>(iterables.length);
		this.iterables = set;
		for(final Iterable<T> iterable : iterables)
		{
			if(iterable == null)
			{
				continue;
			}
			set.add(iterable);
		}

	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Iterator<T> iterator()
	{
		return new ChainedIterator();
	}

	public Iterable<T> add(final Iterable<T> iterable)
	{
		this.iterables.add(iterable);
		return this;
	}

	@SafeVarargs
	public final ChainedIterables<T> add(final Iterable<T>... iterables)
	{
		for(final Iterable<T> iterable : iterables)
		{
			this.iterables.add(iterable);
		}
		return this;
	}

	public Iterable<T> remove(final Iterable<T> iterable)
	{
		this.iterables.remove(iterable);
		return iterable;
	}

	@SafeVarargs
	public final int remove(final Iterable<T>... iterables)
	{
		int removedCount = 0;
		for(final Iterable<T> i : iterables)
		{
			if(this.iterables.remove(i))
			{
				removedCount++;
			}
		}
		return removedCount;
	}

	public boolean contains(final Iterable<T> iterable)
	{
		return this.iterables.contains(iterable);
	}

	protected class ChainedIterator implements Iterator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private Iterator<T> currentIterator;
		private int         currentIndex   ;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ChainedIterator()
		{
			super();
			this.nextIterator();
		}

		@Override
		public boolean hasNext()
		{
			if(this.currentIterator.hasNext())
			{
				return true;
			}
			while(this.nextIterator())
			{
				if(this.currentIterator.hasNext())
				{
					return true;
				}
			}
			return false;
		}

		@Override
		public T next()
		{
			return this.currentIterator.next();
		}


		protected boolean nextIterator()
		{
			final ArrayList<Iterable<T>> iterables = ChainedIterables.this.iterables;

			Iterable<T> loopIterable = null;
			//the loops skips null elements until the first existing iterable is encountered
			while(loopIterable == null && this.currentIndex < iterables.size())
			{
				loopIterable = iterables.get(this.currentIndex++);
			}
			//if either currentIndex was already at the end or loop scrolled to the end, there are no more iterables
			if(loopIterable == null)
			{
				return false;
			}

			//otherwise, the next iterable has been found.
			this.currentIterator = loopIterable.iterator();
			return true;
		}

		@Override
		public void remove()
		{
			this.currentIterator.remove();
		}

	}

}
