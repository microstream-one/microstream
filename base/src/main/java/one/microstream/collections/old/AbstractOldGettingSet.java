package one.microstream.collections.old;

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

import java.util.Collection;
import java.util.Iterator;

import one.microstream.collections.types.XGettingSet;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;

public abstract class AbstractOldGettingSet<E> implements OldSet<E>
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	final XGettingSet<E> subject;



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldGettingSet(final XGettingSet<E> set)
	{
		super();
		this.subject = set;
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XGettingSet<E> parent()
	{
		return this.subject;
	}

	@Override
	public boolean add(final E e) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends E> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(final Object o)
	{
		return this.subject.containsSearched(XFunc.isEqualTo((E)o));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		for(final Object o : c)
		{
			if(!this.subject.containsSearched(XFunc.isEqualTo((E)o)))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return this.subject.isEmpty();
	}

	@Override
	public Iterator<E> iterator()
	{
		return this.subject.iterator();
	}

	@Override
	public boolean remove(final Object o) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int size()
	{
		return XTypes.to_int(this.subject.size());
	}

	@Override
	public Object[] toArray()
	{
		return this.subject.toArray();
	}

	@Override
	public String toString()
	{
		return this.subject.toString();
	}


}
