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

public interface XSortableEnum<E> extends XSortableSequence<E>, XGettingEnum<E>, XOrderingEnum<E>
{
	public interface Creator<E> extends XSortableSequence.Creator<E>, XGettingEnum.Creator<E>
	{
		@Override
		public XSortableEnum<E> newInstance();
	}


	@Override
	public XSortableEnum<E> shiftTo(long sourceIndex, long targetIndex);
	@Override
	public XSortableEnum<E> shiftTo(long sourceIndex, long targetIndex, long length);
	@Override
	public XSortableEnum<E> shiftBy(long sourceIndex, long distance);
	@Override
	public XSortableEnum<E> shiftBy(long sourceIndex, long distance, long length);

	@Override
	public XSortableEnum<E> swap(long indexA, long indexB);
	@Override
	public XSortableEnum<E> swap(long indexA, long indexB, long length);

	@Override
	public XSortableEnum<E> reverse();



	@Override
	public XSortableEnum<E> copy();

	@Override
	public XSortableEnum<E> toReversed();

	@Override
	public XSortableEnum<E> sort(Comparator<? super E> comparator);

}
