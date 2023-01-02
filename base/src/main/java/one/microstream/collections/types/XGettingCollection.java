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

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.X;
import one.microstream.collections.interfaces.CapacityCarrying;
import one.microstream.collections.interfaces.ExtendedCollection;
import one.microstream.collections.old.OldCollection;
import one.microstream.equality.Equalator;
import one.microstream.functional.ToArrayAggregator;
import one.microstream.typing.Copyable;


/**
 * @param <E> type of contained elements
 * 
 *
 */
public interface XGettingCollection<E>
extends
XIterable<E>,
XJoinable<E>,
ExtendedCollection<E>,
Iterable<E>,
CapacityCarrying,
Copyable
{
	/**
	 * Gets one element from the collection. If the collection is not ordered {@link XGettingSequence}, then it is
	 * undefined which element is returned. If the collection is ordered, the element at index 0 is returned.
	 *
	 * @return the first / any element.
	 */
	public E get();

	@Override
	public Iterator<E> iterator();

	/**
	 * Returns an array containing all the elements in this collection.
	 *
	 * <p>The returned array will be "safe" in that no references to it are
	 * maintained by this list.  (In other words, this method must allocate
	 * a new array).  The caller is thus free to modify the returned array.
	 *
	 * <p>This method acts as bridge between MicroStream-based collections
	 * and Java-native-based APIs.
	 *
	 * @return an array containing all the elements in this collection.
	 */
	public default Object[] toArray()
	{
		return this.iterate(new ToArrayAggregator<>(new Object[X.checkArrayRange(this.size())])).yield();
	}

	/**
	 * Returns a <b>typed</b> array containing all the elements in this collection.
	 *
	 * <p>The returned array will be "safe" in that no references to it are
	 * maintained by this list.  (In other words, this method must allocate
	 * a new array).  The caller is thus free to modify the returned array.
	 *
	 * <p>This method acts as bridge between MicroStream-based collections
	 * and Java-native-based APIs.
	 *
	 * @param type the {@link Class} representing type {@code E} at runtime.
	 * @return a typed array containing all the elements in this collection.
	 */
	public default E[] toArray(final Class<E> type)
	{
		return this.iterate(new ToArrayAggregator<>(X.Array(type, X.checkArrayRange(this.size())))).yield();
	}

	public OldCollection<E> old();

	@Override
	public boolean hasVolatileElements();

	@Override
	public long size();

	public default int intSize()
	{
		return X.checkArrayRange(this.size());
	}

	public Equalator<? super E> equality();

	/**
	 * @return {@code true} if the passed collection is of the same type as this collection and
	 * {@code this.equalsContent(list, equalator)} yields {@code true}
	 * @param equalator is used to check the equality of the collections
	 * @param samples is the collection which is checked for equality
	 */
	public boolean equals(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

	/**
	 * Returns {@code true} if all elements of this list and the passed list are sequentially equal as defined
	 * by the passed equalator.
	 * <p>
	 * Note that for colletion types that don't have a defined order of elements, this method is hardly usable
	 * (as is {@link #equals(Object)} for them as defined in {@link Collection}). The core problem of comparing
	 * collections that have no defined order is that they aren't really reliably comparable to any other collection.
	 *
	 * @param equalator the equalator to use to determine the equality of each element
	 * @param samples is the collection which is checked for equality
	 * @return {@code true} if this list is equal to the passed list, {@code false} otherwise
	 */
	public boolean equalsContent(XGettingCollection<? extends E> samples, Equalator<? super E> equalator);

	/**
	 * Provides an instance of an immutable collection type with equal behavior and data as this instance.
	 * <p>
	 * If this instance already is of an immutable collection type, it returns itself.
	 *
	 * @return an immutable copy of this collection instance.
	 */
	public XImmutableCollection<E> immure();

	/**
	 * Creates a view of this collection and returns it. It is a read-only collection,
	 * which wraps around this collection and only allows read methods.
	 * <p>
	 * A view is different from immutable collection ({@link XGettingCollection#immure()})
	 * in the way, that changes in this collection are still affecting the view.
	 * The immutable collection on the other hand has no reference to this collection
	 * and changes therefore do not affect the immutable collection.
	 * @return new read-only collection to view this collection
	 */
	public XGettingCollection<E> view();

	/**
	 * Creates a true copy of this collection which references the same elements as this collection does
	 * at the time the method is called. The elements themselves are NOT copied (no deep copying).<br>
	 * The type of the returned set is the same as of this list if possible.
	 *
	 * @return a copy of this list
	 */
	@Override
	public XGettingCollection<E> copy();

	public boolean nullContained();

	/**
	 * Special version of contains() that guarantees to use identity comparison (" == ") when searching for the
	 * given element regardless of the collection's internal logic.<br>
	 * This method has the same behavior as {@link #containsSearched(Predicate)} with a {@link Predicate} implementation
	 * that checks for object identity. The only difference is a performance and usability advantage
	 * @param element the element to be searched in the collection by identity.
	 * @return whether this collection contains exactly the given element.
	 */
	public boolean containsId(E element);

	/**
	 * Checks if the given element is contained in the collection. <br>
	 * In contrast to the {@link XGettingCollection#containsId(Object)} method, this method
	 * uses the internal {@link Equalator} defined by the collection itself.
	 * @param element to be searched in the collection
	 * @return Whether this collection contains the given element as specified by the {@link Equalator}.
	 */
	public boolean contains(E element);

	public boolean containsSearched(Predicate<? super E> predicate);

	/**
	 * @param elements to be searched in the collection.
	 * @return Whether this collection contains all given elements as specified by the {@link Equalator}.
	 */
	public default boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return elements.applies(this::contains);
	}

	/**
	 * Tests each element of the collection on the given predicate.<br>
	 * 
	 * @param predicate that's tested on each element.
	 * @return If all elements test successfully, true is returned.
	 * Otherwise (if at least one test has failed), false is returned.
	 */
	public boolean applies(Predicate<? super E> predicate);

	/**
	 * Count how many times this element matches another element in the collection
	 * using the {@link Equalator}.
	 * @param element to count
	 * @return Amount of matches
	 */
	public long count(E element);

	/**
	 * Count how many matches are found using the given predicate on each element of the collection.
	 * @param predicate defines which elements are counted and which are not
	 * @return Amount of matches
	 */
	public long countBy(Predicate<? super E> predicate);

//	public boolean hasDistinctValues();

//	public boolean hasDistinctValues(Equalator<? super E> equalator);

	/**
	 * Returns the first contained element matching the passed predicate.
	 * @param predicate defines which element is searched
	 * @return Matching element
	 */
	public E search(Predicate<? super E> predicate);

	/**
	 * Returns the first contained element matching the passed sample as defined by the collection's equality logic
	 * or null, if no fitting element is contained.
	 * (For collections using referential equality, this method is basically just a variation of
	 * {@link #contains(Object)} with a different return type. For collections with data-dependant equality,
	 * the returned element might be the same as the passed one or a data-wise equal one, depending on the content
	 * of the collection)
	 * 
	 * @param sample to seek in the collection
	 * @return the first contained element matching the passed sample
	 */
	public E seek(E sample);

	public E max(Comparator<? super E> comparator);

	public E min(Comparator<? super E> comparator);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for all the unique/distinct
	 * elements of this collection. This means the elements are not equal to each other.<br>
	 * Uniqueness is defined by the collections internal {@link Equalator}.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <code>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,2,3);<br>
	 * BulkList&lt;Integer&gt; distinctCollection = collection1.distinct(BulkList.New());
	 * </code><br>
	 * Results in <code>distinctCollection</code> containing 1, 2 and 3.
	 * 
	 * @param <T> type of the target
	 * @param target on which the {@link Consumer#accept(Object)} is called for every distinct element of this collection.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T distinct(T target);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for all the unique/distinct
	 * elements of this collection. This means the elements are not equal to each other.<br>
	 * Uniqueness is defined by the given {@link Equalator}.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,2,3);
	 * BulkList&lt;Integer&gt; distinctCollection = collection1.distinct(BulkList.New(), Equalator.identity());
	 * </pre>
	 * Results in <code>distinctCollection</code> containing 1, 2 and 3.
	 * 
	 * @param <T> type of the target
	 * @param target on which the {@link Consumer#accept(Object)} is called for every distinct element of this collection.
	 * @param equalator defines what distinct means (which elements are equal to one another)
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T distinct(T target, Equalator<? super E> equalator);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for all the elements of this collection.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,3);
	 * BulkList&lt;Integer&gt; copiedCollection = collection1.copyTo(BulkList.New());
	 * </pre>
	 * Results in <code>copiedCollection</code> containing 1, 2 and 3.
	 * 
	 * @param <T> type of the target
	 * @param target on which the {@link Consumer#accept(Object)} is called for all elements of this collection.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T copyTo(T target);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for all the elements of this collection
	 * which test {@code true} on the given predicate.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,3);
	 * BulkList&lt;Integer&gt; filteredCollection = collection1.filterTo(BulkList.New(), e-&gt; e % 2 == 0);
	 * </pre>
	 * Results in <code>filteredCollection</code> containing 2.
	 * 
	 * @param <T> type of the target
	 * @param target on which the {@link Consumer#accept(Object)} is called for elements that test {@code true}.
	 * @param predicate on which to test all elements.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T filterTo(T target, Predicate<? super E> predicate);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for all the elements of this collection.
	 * <b>And</b> calls it for all elements of the other collection, that are not already in this collection
	 * (defined by the given {@link Equalator})<br>
	 * Therefore it effectively creates a mathematical union between the two collections.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,3);
	 * BulkList&lt;Integer&gt; collection2 = BulkList.New(2,3,4);
	 * BulkList&lt;Integer&gt; union = collection1.union(collection2, Equalator.identity(), <b>BulkList.New()</b>);
	 * </pre>
	 * Results in <code>union</code> containing 1, 2, 3 and 4.
	 * 
	 * @param <T> type of the target
	 * @param other collection to build a union with.
	 * @param equalator which is used for the equal-tests.
	 * @param target on which the {@link Consumer#accept(Object)} is called for all unified elements.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T union    (XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	/**
	 * Tests equality between each element of the two lists and calls {@link Consumer#accept(Object)} on the target {@link Consumer} for the
	 * equal elements.<br>
	 * Therefore, it effectively creates a mathematical intersection between the two collections.
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,3);
	 * BulkList&lt;Integer&gt; collection2 = BulkList.New(2,3,4);
	 * BulkList&lt;Integer&gt; intersection = collection1.intersect(collection2, Equalator.identity(), <b>BulkList.New()</b>);
	 * </pre>
	 * Results in <code>intersection</code> containing 2 and 3.
	 * 
	 * @param <T> type of the target
	 * @param other collection to intersect with.
	 * @param equalator which is used for the equal-tests.
	 * @param target on which the {@link Consumer#accept(Object)} is called for equal elements.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T intersect(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	/**
	 * Calls {@link Consumer#accept(Object)} on the target {@link Consumer} for each
	 * element of this collection that is not contained in the other collection (through the given equalator).
	 * <p>
	 * Since all MicroStream Collections implement the {@link Consumer} interface,
	 * new collections can be used as target.
	 * <p>
	 * <b>Example:</b><br>
	 * <pre>
	 * BulkList&lt;Integer&gt; collection1 = BulkList.New(1,2,3);
	 * BulkList&lt;Integer&gt; collection2 = BulkList.New(2,3,4);
	 * BulkList&lt;Integer&gt; exceptCollection = collection1.except(collection2, Equalator.identity(), <b>BulkList.New()</b>);
	 * </pre>
	 * Results in <code>exceptCollection</code> containing 1.
	 * 
	 * @param <T> type of the target
	 * @param other collection whose elements are excluded from the target.
	 * @param equalator which is used for the equal-tests.
	 * @param target on which the {@link Consumer#accept(Object)} is called for elements not contained in the other collection.
	 * @return Given target
	 */
	public <T extends Consumer<? super E>> T except   (XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);


	@Override
	public default <A> A join(final BiConsumer<? super E, ? super A> joiner, final A aggregate)
	{
		this.iterate(e ->
			joiner.accept(e, aggregate)
		);
		return aggregate;
	}


	// (10.06.2014 TM)FIXME: remove old equality from interface / move to "Old" interfaces
	/**
	 * Performs an equality comparison according to the specification in {@link Collection}.
	 * <p>
	 * Note that it is this interface's author opinion that the whole concept of equals() in standard Java, especially
	 * in the collection implementations, is flawed.<br>
	 * The reason is because all different kinds of comparison types that actually depend on the situation
	 * have to be mixed up in a harcoded fashion in one method, from identity comparison over
	 * data indentity comparison to content comparison. <br>
	 * In order to get the right behavior in every situation, one has to distinct between different types of equality<br>
	 * <p>
	 * This means several things:<br>
	 * 1.) You can't just say for example an ArrayList is the "same" as a LinkedList just because they contain the
	 * same content.<br>
	 * There are different implementations for a good reason, so you have to distinct them when comparing.
	 * There are simple code examples which create massive misbehavior that will catastrophically ruin the runtime
	 * behavior of a programm due to this error in Java / JDK / Sun / whatever.<br>
	 * 2.) You can't always determine equality of two collections by determining equality of each element as
	 * {@link Collection} defines it.
	 * <p>
	 * As a conclusion: don't use this method!<br>
	 * Be clear what type of comparison you really need, then use one of the following methods
	 * and proper comparators:<br>
	 * {@link #equals(XGettingCollection, Equalator)}<br>
	 * {@link #equalsContent(XGettingCollection, Equalator)}<br>
	 * <p>
	 * {@inheritDoc}
	 *
	 * @param o  the reference object with which to compare.
	 */
	@Override
	@Deprecated
	public boolean equals(Object o);

	@Override
	@Deprecated
	public int hashCode();



	public interface Creator<E> extends XFactory<E>
	{
		@Override
		public XGettingCollection<E> newInstance();
	}

}
