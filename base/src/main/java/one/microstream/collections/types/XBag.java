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


/**
 * Bag type collections make the single demand (thus being a level 1 collection type) that duplicate elements have
 * to be allowed, effectively being the opposite to set type collections.
 * <p>
 * The naming for the type is based on the conception that a bag can contain any elements (including duplicates),
 * but is definitely not ordered.
 * <p>
 * This will probably be a rather academic type and has been introduced more for reasons of completeness of the
 * typing architecture, as in practice, list type collections will be preferred to pure bag type collections.
 * <p>
 * Bag type collections are architectural on par with the other level 1 collection types set and sequence.
 * <p>
 * Currently, the only known to be useful subtype of a bag is the level 2 collection type list, combining bag
 * and sequence (order of elements).
 *
 * @param <E> type of contained elements
 *
 * @see XSet
 * @see XSequence
 * @see XList
 *
 * 
 */
public interface XBag<E> extends XPutGetBag<E>, XProcessingBag<E>, XCollection<E>
{
	public interface Factory<E> extends XPutGetBag.Factory<E>, XProcessingBag.Factory<E>, XCollection.Factory<E>
	{
		@Override
		public XBag<E> newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public XBag<E> putAll(E... elements);

	@Override
	public XBag<E> putAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBag<E> putAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XBag<E> addAll(E... elements);

	@Override
	public XBag<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XBag<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XBag<E> copy();

}
