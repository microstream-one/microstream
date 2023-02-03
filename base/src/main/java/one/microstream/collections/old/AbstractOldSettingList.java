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

import one.microstream.collections.types.XSettingList;

public abstract class AbstractOldSettingList<E> extends AbstractOldGettingList<E>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractOldSettingList(final XSettingList<E> list)
	{
		super(list);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSettingList<E> parent()
	{
		return (XSettingList<E>)this.subject;
	}

	@Override
	public E set(final int index, final E element)
	{
		return ((XSettingList<E>)this.subject).setGet(index, element);
	}

	@Override
	public AbstractOldSettingList<E> subList(final int fromIndex, final int toIndex)
	{
		/* XSettingList implementations always create a SubList instance whose implementation creates an
		 * OldSettingList bridge instance, so this cast is safe (and inevitable).
		 */
		return (AbstractOldSettingList<E>)(((XSettingList<E>)this.subject).range(fromIndex, toIndex).old());
	}

}
