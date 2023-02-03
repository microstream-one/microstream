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

import one.microstream.collections.types.XGettingCollection;
import one.microstream.collections.types.XSet;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;

public abstract class AbstractBridgeXSet<E> extends AbstractOldGettingSet<E>
{
	protected AbstractBridgeXSet(final XSet<E> set)
	{
		super(set);
	}


	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSet<E> parent()
	{
		return (XSet<E>)super.parent();
	}

	@Override
	public boolean add(final E e)
	{
		return ((XSet<E>)this.subject).add(e);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		if(c instanceof XGettingCollection<?>)
		{
			((XSet<E>)this.subject).addAll((XGettingCollection<? extends E>)c);
			return true;
		}

		final XSet<E> list = (XSet<E>)this.subject;
		for(final E e : c)
		{
			list.add(e);
		}
		return true;
	}

	@Override
	public void clear()
	{
		((XSet<E>)this.subject).clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		int removeCount = 0;
		final XSet<E> list = (XSet<E>)this.subject;

		// even xcollections have to be handled that way because of the missing type info
		for(final Object o : c)
		{
			removeCount += list.removeBy(XFunc.isEqualTo((E)o));
		}
		return removeCount > 0;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		final int oldSize = XTypes.to_int(this.subject.size());
		((XSet<E>)this.subject).removeBy(e -> !c.contains(e));
		return oldSize - XTypes.to_int(this.subject.size()) > 0;
	}

}
