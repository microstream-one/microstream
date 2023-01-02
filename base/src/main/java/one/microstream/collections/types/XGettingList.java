package one.microstream.collections.types;

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

import java.util.ListIterator;

import one.microstream.collections.interfaces.ExtendedList;
import one.microstream.collections.old.OldList;

public interface XGettingList<E> extends XGettingSequence<E>, XGettingBag<E>, ExtendedList<E>
{
	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingList<E> newInstance();
	}



	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableList<E> immure();

	// java.util.List List Iterators
	public ListIterator<E> listIterator();
	
	public ListIterator<E> listIterator(long index);

	@Override
	public OldList<E> old();

	@Override
	public XGettingList<E> copy();

	@Override
	public XGettingList<E> toReversed();

	@Override
	public XGettingList<E> view();
	
	@Override
	public XGettingList<E> view(long lowIndex, long highIndex);

	@Override
	public XGettingList<E> range(long fromIndex, long toIndex);

}
