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

public interface XSettingEnum<E> extends XSortableEnum<E>, XSettingSequence<E>
{
	public interface Creator<E> extends XSortableEnum.Creator<E>, XSettingSequence.Creator<E>
	{
		@Override
		public XSettingEnum<E> newInstance();
	}



	@Override
	public E setGet(long index, E element);

	// intentionally not returning old element for performance reasons. set(int, E) does that already.
	@Override
	public void setFirst(E element);

	@Override
	public void setLast(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XSettingEnum<E> setAll(long index, E... elements);

	@Override
	public XSettingEnum<E> set(long index, E[] elements, int offset, int length);

	@Override
	public XSettingEnum<E> set(long index, XGettingSequence<? extends E> elements, long offset, long length);



	@Override
	public XSettingEnum<E> swap(long indexA, long indexB);

	@Override
	public XSettingEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XSettingEnum<E> reverse();

	@Override
	public XSettingEnum<E> sort(Comparator<? super E> comparator);

	@Override
	public XSettingEnum<E> copy();

	@Override
	public XSettingEnum<E> toReversed();

	@Override
	public XSettingEnum<E> range(long fromIndex, long toIndex);

}
