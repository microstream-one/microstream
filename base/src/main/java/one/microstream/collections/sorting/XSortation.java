package one.microstream.collections.sorting;

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

import one.microstream.collections.types.XBasicSequence;
import one.microstream.collections.types.XGettingCollection;

/**
 * Actually being a "Collation" (a collection of elements to which a sortation is applied), this type has been named
 * "Sortation" nevertheless to avoid the mistakable similarity to the basic collection type "Collection" in reading,
 * writing, talking and IntelliSense filtering.
 *
 *
 * @param <E> the type of the input to the operation
 */
public interface XSortation<E> extends XBasicSequence<E>, XPutGetSortation<E>, XProcessingSortation<E>
{
	public interface Factory<E>
	extends XBasicSequence.Factory<E>, XPutGetSortation.Factory<E>, XProcessingSortation.Factory<E>
	{
		@Override
		public XSortation<E> newInstance();
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public XSortation<E> copy();

	@Override
	public XSortation<E> toReversed();

	@SuppressWarnings("unchecked")
	@Override
	public XSortation<E> putAll(E... elements);
	
	@Override
	public XSortation<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XSortation<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XSortation<E> addAll(E... elements);
	
	@Override
	public XSortation<E> addAll(E[] elements, int srcStartIndex, int srcLength);
	
	@Override
	public XSortation<E> addAll(XGettingCollection<? extends E> elements);

}
