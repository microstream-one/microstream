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

import one.microstream.collections.sorting.XLadder;


/**
 * Intermediate list type providing getting, adding, removing concerns to act as a common super type for
 * {@link XList} and {@link XLadder}. This is necessary because {@link XLadder} cannot provide
 * the otherwise typical list concerns like inserting, ordering, setting due to the limitations of the characteristic
 * of being always sorted.
 *
 * @param <E> type of contained elements
 */
public interface XBasicList<E> extends XBag<E>, XBasicSequence<E>, XPutGetList<E>, XProcessingList<E>
{
	public interface Creator<E>
	extends
	XBag.Factory<E>,
	XBasicSequence.Factory<E>,
	XPutGetList.Factory<E>,
	XProcessingList.Factory<E>
	{
		@Override
		public XBasicList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XBasicList<E> putAll(E... elements);

	@Override
	public XBasicList<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicList<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBasicList<E> addAll(E... elements);

	@Override
	public XBasicList<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicList<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBasicList<E> copy();

	/**
	 * Creates a new {@link XBasicList} with the reversed order of elements.
	 * <p>
	 * This method creates a new collection and does <b>not</b> change the
	 * existing collection.<br>
	 * Furthermore, changes to the reversed collection do <b>not</b> reflect to the original.
	 * @return new reversed collection
	 */
	@Override
	public XBasicList<E> toReversed();

}
