package one.microstream.reference;

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

import one.microstream.collections.Singleton;


public interface LinkReference<T> extends LinkingReference<T>
{
	@Override
	public LinkReference<T> next();

	/**
	 * Sets {@code linkedReference} as this {@code LinkedReference} object's
	 * linked {@code LinkedReference} object.
	 * <p>
	 * Note that the so far linked {@code LinkedReference} object is returned, not this object itself!
	 * @param linkedReference the new linked reference
	 * @return the so far linked {@code LinkedReference} object (NOT this!)
	 */
	public LinkReference<T> setNext(LinkReference<T> linkedReference);


	/**
	 * Sets {@code linkedReference} as this {@code LinkedReference} object's
	 * linked {@code LinkedReference} object.
	 * <p>
	 * Note that the reference is returned, not this object itself!
	 * @param linkedReference the new linked reference
	 * @return the linked {@code LinkedReference} object (NOT this!)
	 */
	public LinkReference<T> link(LinkReference<T> linkedReference);

	/**
	 * Alias for {@code link(new LinkedReference(nextRef))}.
	 * @param nextRef the object for the new linked reference
	 * @return the linked {@code LinkedReference} object (NOT this!)
	 */
	public LinkReference<T> link(T nextRef);


	public LinkReference<T> insert(LinkReference<T> linkedReference);
	public LinkReference<T> removeNext();
	// (22.07.2010 TM)TODO: removeNext(i)

	
	@SafeVarargs
	public static <T> LinkReference<T> New(final T... objects)
	{
		if(objects == null)
		{
			return null;
		}

		final LinkReference<T> chain = new LinkReference.Default<>(objects[0]);

		if(objects.length > 1)
		{
			LinkReference<T> loopRef = chain;
			for(int i = 1; i < objects.length; i++)
			{
				loopRef = loopRef.link(objects[i]);
			}
		}
		
		return chain;
	}
	

	public class Default<T> extends Singleton<T> implements LinkReference<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		private LinkReference<T> next;
		
		
		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		public Default(final T ref)
		{
			super(ref);
		}

		@Override
		public LinkReference<T> next()
		{
			return this.next;
		}

		@Override
		public boolean hasNext()
		{
			return this.next != null;
		}

		@Override
		public boolean isNext(final LinkingReferencing<T> linkedReference)
		{
			return this.next == linkedReference;
		}

		@Override
		public LinkReference<T> link(final LinkReference<T> linkedReference)
		{
			this.next = linkedReference;
			
			return linkedReference;
		}

		@Override
		public LinkReference<T> setNext(final LinkReference<T> linkedReference)
		{
			final LinkReference<T> old = this.next;
			this.next = linkedReference;
			
			return old;
		}

		@Override
		public LinkReference<T> link(final T nextRef)
		{
			return this.link(new LinkReference.Default<>(nextRef));
		}


		/**
		 * @param linkedReference the reference to insert
		 * 
		 * @throws NullPointerException if {@code linkedReference} is {@code null}.
		 */
		@Override
		public LinkReference<T> insert(final LinkReference<T> linkedReference)
		{
			final LinkReference<T> next = this.next;
			this.next = linkedReference;

			if(next != null)
			{
				linkedReference.setNext(next); //provoke NullPointer for argument
			}
			
			return this;
		}

		@Override
		public LinkReference<T> removeNext()
		{
			final LinkReference<T> next = this.next;
			this.next = next == null ? null : next.next();
			
			return next;
		}

		@Override
		public Object[] toArray()
		{
			LinkReference<T> loopNext = this;
			int i = 1;
			while((loopNext = loopNext.next()) != null)
			{
				i++; //this is presumable faster than using an ArrayList or LinkedList for collection
			}

			final Object[] array = new Object[i];
			loopNext = this;
			i = 0;
			do
			{
				array[i++] = loopNext.get();
			}
			while((loopNext = loopNext.next()) != null);

			return array;
		}

		@Override
		public Iterator<T> iterator()
		{
			return new ChainIterator<>(this);
		}

		@Override
		public String toString()
		{
			final String e = String.valueOf(this.get());
			final StringBuilder sb = new StringBuilder(e.length() + 3);
			
			return sb.append('(').append(e).append(')').append(this.hasNext() ? '-' : 'x').toString();
		}

		@Override
		public String toChainString()
		{
			final StringBuilder sb = new StringBuilder(1024);
			sb.append('(').append(this.get()).append(')');
			for(LinkingReferencing<T> r = this.next; r != null; r = r.next())
			{
				sb.append('-').append('(').append(r.get()).append(')');
			}
			
			return sb.toString();
		}

	}


	final class ChainIterator<T> implements Iterator<T>
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////
		
		private LinkReference<T> current;


		
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		ChainIterator(final LinkReference<T> current)
		{
			super();
			this.current = current;
		}

		@Override
		public boolean hasNext()
		{
			return this.current.next() != null;
		}

		@Override
		public T next()
		{
			final LinkReference<T> currentCurrent = this.current;
			this.current = currentCurrent.next();
			
			return currentCurrent.get();
		}

		@Override
		public void remove() throws UnsupportedOperationException
		{
			throw new UnsupportedOperationException(
				"Can't remove current element in a one directional chain"
			);
		}
		
	}
}
