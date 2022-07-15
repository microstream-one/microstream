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
 * @param <E> type of contained elements
 * 
 *
 */
public interface XAddGetSet<E> extends XAddingSet<E>, XGettingSet<E>, XAddGetCollection<E>
{
	public interface Factory<E> extends XAddingSet.Creator<E>, XGettingSet.Creator<E>, XAddGetCollection.Creator<E>
	{
		@Override
		public XAddGetSet<E> newInstance();
	}



	public E addGet(E element);
	
	public E deduplicate(E element);

	@SuppressWarnings("unchecked")
	@Override
	public XAddGetSet<E> addAll(E... elements);

	@Override
	public XAddGetSet<E> addAll(E[] elements, int srcStartIndex, int srcLength);

	@Override
	public XAddGetSet<E> addAll(XGettingCollection<? extends E> elements);

	@Override
	public XAddGetSet<E> copy();

}
