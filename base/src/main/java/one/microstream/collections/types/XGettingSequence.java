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
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.branching.ThrowBreak;
import one.microstream.collections.interfaces.ExtendedSequence;
import one.microstream.exceptions.IndexBoundsException;

public interface XGettingSequence<E> extends XGettingCollection<E>, ExtendedSequence<E>, XIndexIterable<E>
{
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Creates a true copy of this list which references the same elements in the same order as this list does
	 * at the time the method is called. The elements themselves are NOT copied (no deep copying).<br>
	 * The type of the returned list is the same as of this list if possible (i.e.: a SubList can not meaningful
	 * return a true copy that references its elements but still is a SubList)
	 *
	 * @return a copy of this list
	 */
	@Override
	public XGettingSequence<E> copy();

	@Override
	public XImmutableSequence<E> immure();

	/**
	 * Gets the first element in the collection. This is a parameterless alias for {@code at(0)}.
	 * <p>
	 * {@link #first()} is an alias for this method.
	 *
	 * @throws NoSuchElementException if collection is empty
	 * @see #at(long)
	 * @see #first()
	 * @see #last()
	 * @return the first element.
	 */
	@Override
	public E get() throws NoSuchElementException;


	///////////////////////////////////////////////////////////////////////////
	// declared methods //
	/////////////////////

	public E at(long index) throws IndexBoundsException; // get element at index or throw IndexOutOfBoundsException

	/**
	 * Equivalent to "contains index". Compare: {@link XGettingTable} table with table.keys().contains(index)
	 *
	 * @param index the index to check
	 * @return <code>true</code> if the passed index is greater or equal to 0,
	 * {@link #size()} is greater than the passed index
	 * and {@link #at(long)} returns a non-null value.
	 */
	public default boolean hasIndex(final long index)
	{
		return index >= 0 && this.size() > index && this.at(index) != null;
	}

	/**
	 * Gets first element or throws {@link IndexOutOfBoundsException} if the collection is empty.
	 * <p>
	 * Is an alias for {@link #get()}.
	 * @return First element
	 */
	public E first() throws IndexBoundsException;

	/**
	 * Gets last element or throws {@link IndexOutOfBoundsException} if the collection is empty.
	 * @return Last element
	 */
	public E last() throws IndexBoundsException;

	/**
	 * Gets first element or null if the collection is empty.
	 * @return First element or null
	 */
	public E poll();  // get first element or null if empty (like polling an empty collection to get filled)

	/**
	 * Gets last element or null if the collection is empty. <br>
	 * This behaves like peeking on a stack without pop.
	 * @return Last element or null
	 */
	public E peek();

	public long maxIndex(Comparator<? super E> comparator);

	public long minIndex(Comparator<? super E> comparator);

	public long indexOf(E element);

	/**
	 * Iterates forwards through the collection and returns the index of the <b>first element</b> that the passed {link Predicate}
	 * applies to immediately.<br>
	 * Stops iterating on the first element that the predicate applies to.
	 * <p>
	 * Basically the opposite of {@link XGettingSequence#lastIndexBy(Predicate)}
	 * 
	 * @param predicate to define a valid element
	 * @return The index of the first positively tested element.
	 */
	public long indexBy(Predicate<? super E> predicate);

	public long lastIndexOf(E element);

	/**
	 * Iterates backwards through the collection and returns the index of the <b>last element</b> that the passed {@link Predicate}
	 * applies to immediately.<br>
	 * Stops iterating on the first element that the predicate applies to.
	 * <p>
	 * Basically the opposite of {@link XGettingSequence#indexBy(Predicate)}.<br>
	 * Similar but not the same as {@link XGettingSequence#scan(Predicate)}, since <code>scan</code> iterates through <b>all elements</b>.
	 *
	 * @param predicate to define a valid element
	 * @return the index of the last positively tested element.
	 */
	public long lastIndexBy(Predicate<? super E> predicate);

	/**
	 * Iterates through the collection and returns the index of the last element that the passed {@link Predicate}
	 * applied to ("scanning").
	 * <p>
	 * In order to find the last element, this method must iterate over <b>all elements</b> of the collection
	 * (opposed to {@link XGettingSequence#indexBy(Predicate)} and {@link XGettingSequence#lastIndexBy(Predicate)}).
	 * <p>
	 * Iteration can be safely canceled with a {@link ThrowBreak} ({@link one.microstream.X#BREAK})
	 * @param predicate to define a valid element
	 * @return the index of the last positively tested element.
	 */
	public long scan(Predicate<? super E> predicate);

	/**
	 * Tests if the collection is sorted according to the given comparator.
	 * @param comparator defines if elements are sorted
	 * @return true if it sorted, false if not
	 */
	public boolean isSorted(Comparator<? super E> comparator);

	/**
	 * Creates a new {@link XGettingSequence} with the reversed order of elements.
	 * <p>
	 * This method creates a new collection and does <b>not</b> change the
	 * existing collection.
	 * 
	 * @return New copy of the collection
	 */
	public XGettingSequence<E> toReversed();

	/**
	 * Iterates through all the elements of the given indices and calls the
	 * {@link Consumer#accept(Object)} on the target {@link Consumer}.
	 * 
	 * @param <T> type of the target
	 * @param target on which the {@link Consumer#accept(Object)} is called
	 * @param indices of the elements which are copied
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T copySelection(T target, long... indices);

	@Override
	public XGettingSequence<E> view();

	/**
	 * Creates a sub-view of this collection and returns it. It is a read-only collection,
	 * which wraps around this collection and only allows read methods.<br>
	 * The view is limited to a range from the lowIndex to the highIndex.
	 * <p>
	 * A view is different from immutable collection ({@link XGettingCollection#immure()})
	 * in the way, that changes in this collection are still affecting the view.
	 * The immutable collection on the other hand has no reference to this collection
	 * and changes therefore do not affect the immutable collection.
	 * 
	 * @param lowIndex defines lower boundary for the view of the collection.
	 * @param highIndex defines higher boundary for the view of the collection.
	 * @return new read-only collection to view a range of elements in this collection
	 */
	public XGettingSequence<E> view(long lowIndex, long highIndex);

	public XGettingSequence<E> range(long lowIndex, long highIndex);



	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingSequence<E> newInstance();
	}

}
