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
import one.microstream.collections.types.XList;
import one.microstream.collections.types.XSet;
import one.microstream.functional.XFunc;
import one.microstream.typing.XTypes;

public abstract class AbstractBridgeXList<E> extends AbstractOldSettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBridgeXList(final XList<E> list)
	{
		super(list);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XList<E> parent()
	{
		return (XList<E>)this.subject;
	}

	@Override
	public boolean add(final E e)
	{
		return ((XList<E>)this.subject).add(e);
	}

	@Override
	public void add(final int index, final E element)
	{
		((XList<E>)this.subject).input(index, element);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(final Collection<? extends E> c)
	{
		if(c instanceof XGettingCollection<?>)
		{
			((XList<E>)this.subject).addAll((XGettingCollection<? extends E>)c);
			return true;
		}

		final XList<E> list = (XList<E>)this.subject;
		for(final E e : c)
		{
			list.add(e);
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c)
	{
		if(c instanceof XGettingCollection<?>)
		{
			((XList<E>)this.subject).inputAll(index, (XGettingCollection<E>)c);
			return true;
		}

		final XList<E> list = (XList<E>)this.subject;
		int i = index;
		for(final E e : c)
		{
			list.input(i++, e);
		}
		return true;
	}

	@Override
	public void clear()
	{
		((XList<E>)this.subject).clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(final Object o)
	{
		return ((XList<E>)this.subject).removeBy(XFunc.isEqualTo((E)o)) > 0;
	}

	@Override
	public E remove(final int index)
	{
		return ((XList<E>)this.subject).removeAt(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		int removeCount = 0;
		final XSet<E> list = (XSet<E>)this.subject;

		// even xcollections have to be handled that way because of the missing type info.
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
		((XList<E>)this.subject).removeBy(e -> !c.contains(e));
		return oldSize - XTypes.to_int(this.subject.size()) > 0;
	}

	@Override
	public AbstractBridgeXList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XList implementations always create a SubList instance whose implementation creates an
		 * OldXList bridge instance, so this cast is safe (and inevitable).
		 */
		return (AbstractBridgeXList<E>)((XList<E>)this.subject).range(fromIndex, toIndex).old();
	}

}
