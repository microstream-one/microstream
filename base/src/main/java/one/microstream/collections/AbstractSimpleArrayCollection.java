package one.microstream.collections;

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
public abstract class AbstractSimpleArrayCollection<E> extends AbstractSectionedArrayCollection<E>
{
	/**
	 * This is an internal shortcut method to provide fast access to the various array-backed list implementations'
	 * storage arrays.<br>
	 * The purpose of this method is to allow access to the array only for read-only procedures, never for modifying
	 * accesses.
	 * <p>
	 * The returned array is expected to contain the elements of the list in simple order from index 0 on to index
	 * (size - 1), so for example an array-backed ring list (queue) can NOT (reasonably) extend this class.
	 *
	 * @return the storage array used by the list, containing all elements in straight order.
	 */
	@Override
	protected abstract E[] internalGetStorageArray();

	protected abstract int internalSize();


	/**
	 * Workaround method to handle the generics warning at a central place instead of maintaining them at hundreds
	 * of code locations. Note that the calling logic must guarantee the type safety (see calls of this method
	 * for examples)
	 *
	 * @param subject the collection to get the storage array from
	 * @param <E> the element type
	 * @return the storage array used by the list, containing all elements in straight order.
	 */
	@SuppressWarnings("unchecked")
	protected static <E> E[] internalGetStorageArray(final AbstractSimpleArrayCollection<?> subject)
	{
		return (E[])subject.internalGetStorageArray();
	}

}
