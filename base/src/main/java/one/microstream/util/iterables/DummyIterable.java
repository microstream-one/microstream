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
