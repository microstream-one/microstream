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
 * Putting aspect:<br>
 * Ensure that all putted elements are contained in the collection
 * <p>
 * Examples:
 * Set: Add all elements, overwriting equal already contained elements.
 * Bag: Always add all elements (identical to add)
 * <p>
 * Note: Corresponds to the Java collections {@code add()} for single dimensional collections and {@code put()} for Map.
 *
 * @param <E> type of contained elements
 * 
 */
public interface XPuttingCollection<E> extends XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XPuttingCollection<E> newInstance();
	}

	/**
	 * Adds the specified element to this collection if it is not already present (optional operation).
	 * @param element to add
	 * @return true if this collection did not already contain the specified element
	 */
	public boolean put(E element);
	
	public boolean nullPut();
	
	/**
	 * Adds the specified elements to this collection if it is not already present (optional operation).
	 * @param elements to add
	 * @return this
	 */
	@SuppressWarnings("unchecked")
	public XPuttingCollection<E> putAll(E... elements);
	
	/**
	 * Adds the specified elements to this collection if it is not already present (optional operation).<br>
	 * Only the elements with indizes from the srcStartIndex to the srcStartIndex+srcLength
	 * are put in the collection.
	 * @param elements to add
	 * @param srcStartIndex start index of elements-array to add to collection
	 * @param srcLength length of elements-array to add to collection
	 * @return this
	 */
	public XPuttingCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	/**
	 * Adds the specified elements to this collection if it is not already present (optional operation).
	 * @param elements to add
	 * @return this
	 */
	public XPuttingCollection<E> putAll(XGettingCollection<? extends E> elements);

}
