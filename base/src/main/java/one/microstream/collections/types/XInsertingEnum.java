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



public interface XInsertingEnum<E> extends XInsertingSequence<E>, XExtendingEnum<E>
{
	public interface Creator<E> extends XExtendingSequence.Creator<E>
	{
		@Override
		public XInsertingEnum<E> newInstance();
	}



	@SuppressWarnings("unchecked")
	@Override
	public XInsertingEnum<E> addAll(E... elements);

	@Override
	public XInsertingEnum<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingEnum<E> addAll(XGettingCollection<? extends E> elements);

	@SuppressWarnings("unchecked")
	@Override
	public XInsertingEnum<E> prependAll(E... elements);

	@Override
	public XInsertingEnum<E> prependAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XInsertingEnum<E> prependAll(XGettingCollection<? extends E> elements);

}
