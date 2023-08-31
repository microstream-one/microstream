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

import java.util.Comparator;

/**
 * Intermediate list type that combines all list aspects except increasing (adding and inserting), effectively causing
 * instances of this list type to maintain its size or shrink, but never grow.
 * <p>
 * This type is primarily used for the values list of a map, which can offer all functionality except adding
 * values (without mapping it to a key).
 */
public interface XDecreasingList<E> extends XProcessingList<E>, XSettingList<E>, XDecreasingSequence<E>
{
	public interface Creator<E> extends XProcessingList.Factory<E>, XSettingList.Creator<E>, XDecreasingSequence.Creator<E>
	{
		@Override
		public XDecreasingList<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XDecreasingList<E> setAll(long index, E... elements);

	@Override
	public XDecreasingList<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XDecreasingList<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XDecreasingList<E> swap(long indexA, long indexB);

	@Override
	public XDecreasingList<E> swap(long indexA, long indexB, long length);

	@Override
	public XDecreasingList<E> copy();

	@Override
	public XDecreasingList<E> toReversed();

	@Override
	public XDecreasingList<E> reverse();

	@Override
	public XDecreasingList<E> range(long fromIndex, long toIndex);

	@Override
	public XDecreasingList<E> fill(long offset, long length, E element);

	@Override
	public XDecreasingList<E> sort(Comparator<? super E> comparator);
	
}
