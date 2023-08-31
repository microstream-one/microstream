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
 *
 */
public interface XDecreasingSequence<E> extends XProcessingSequence<E>, XSettingSequence<E>
{
	public interface Creator<E> extends XProcessingSequence.Factory<E>, XSettingSequence.Creator<E>
	{
		@Override
		public XDecreasingSequence<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XDecreasingSequence<E> setAll(long index, E... elements);

	@Override
	public XDecreasingSequence<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XDecreasingSequence<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);

	@Override
	public XDecreasingSequence<E> swap(long indexA, long indexB);

	@Override
	public XDecreasingSequence<E> swap(long indexA, long indexB, long length);

	@Override
	public XDecreasingSequence<E> copy();

	@Override
	public XDecreasingSequence<E> toReversed();

	@Override
	public XDecreasingSequence<E> reverse();

	@Override
	public XDecreasingSequence<E> range(long fromIndex, long toIndex);

	@Override
	public XDecreasingSequence<E> sort(Comparator<? super E> comparator);

}
