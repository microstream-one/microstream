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

import one.microstream.collections.sorting.XSortation;

/**
 * Intermediate sequence type providing getting, adding, removing concerns to act as a common super type for
 * {@link XSequence} and {@link XSortation}. This is necessary because {@link XSortation} cannot provide
 * the otherwise typical sequence concerns like inserting and ordering due to the limitations of the characteristic
 * of being always sorted.
 *
 * @param <E> the type of elements in this collection
 */
public interface XBasicSequence<E> extends XCollection<E>, XPutGetSequence<E>, XProcessingSequence<E>
{
	public interface Factory<E> extends XCollection.Factory<E>, XPutGetSequence.Factory<E>, XProcessingSequence.Factory<E>
	{
		@Override
		public XBasicSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XBasicSequence<E> putAll(E... elements);

	@Override
	public XBasicSequence<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicSequence<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBasicSequence<E> addAll(E... elements);

	@Override
	public XBasicSequence<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBasicSequence<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBasicSequence<E> copy();

	@Override
	public XBasicSequence<E> toReversed();
}
