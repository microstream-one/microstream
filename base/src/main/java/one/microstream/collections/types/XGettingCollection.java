package one.microstream.collections.types;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
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

	public default Object[] toArray()
	{
		return this.iterate(new ToArrayAggregator<>(new Object[X.checkArrayRange(this.size())])).yield();
	}

	public default E[] toArray(final Class<E> type)
	{
		return this.iterate(new ToArrayAggregator<>(X.Array(type, X.checkArrayRange(this.size())))).yield();
	}
	
	// stolen from Streams API. However. A) ambiguous for compiler and B) inferior to just keeping and passing a type.
//	public default E[] toArray(final IntFunction<E[]> arrayConstructor)
//	{
//		final E[] array = arrayConstructor.apply(X.checkArrayRange(this.size()));
//		return this.iterate(new ToArrayAggregator<>(array)).yield();
//	}

	public OldCollection<E> old();

	/**
	 * Tells if this collection contains volatile elements.<br>
	 * An element is volatile, if it can become no longer reachable by the collection without being removed from the
	 * collection. Examples are {@link WeakReference} of {@link SoftReference} or implementations of collection entries
	 * that remove the element contained in an entry by some means outside the collection.<br>
	 * Note that {@link WeakReference} instances that are added to a a simple (non-volatile) implementation of a
	 * collection do NOT make the collection volatile, as the elements themselves (the reference instances) are still
	 * strongly referenced.
	 *
	 * @return {@code true} if the collection contains volatile elements.
	 */
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
	 * Returns {@code true} if the passed collection is of the same type as this collection and
	 * {@code this.equalsContent(list, equalator)} yields {@code true}.
	 *
	 * @param equalator
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
	 * @return {@code true} if this list is equal to the passed list, <tt>false</tt> otherwise
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
	 * @return whether this collection contains exactely the given element.
	 */
	public boolean containsId(E element);

	public boolean contains(E element);

	public boolean containsSearched(Predicate<? super E> predicate);

	public default boolean containsAll(final XGettingCollection<? extends E> elements)
	{
		return elements.applies(this::contains);
	}

	public boolean applies(Predicate<? super E> predicate);

	public long count(E element);

	public long countBy(Predicate<? super E> predicate);

//	public boolean hasDistinctValues();

//	public boolean hasDistinctValues(Equalator<? super E> equalator);

	public E search(Predicate<? super E> predicate);

	/**
	 * Returns the first contained element matching the passed sample as defined by the collection's equality logic
	 * or null, if no fitting element is contained.
	 * (For collections using referential equality, this method is basically just a variation of
	 * {@link #contains(Object)} with a different return type. For collections with data-dependant equality,
	 * the returned element might be the same as the passed one or a data-wise equal one, depending on the content
	 * of the collection)
	 * 
	 * @param sample
	 */
	public E seek(E sample);

	public E max(Comparator<? super E> comparator);

	public E min(Comparator<? super E> comparator);

	public <T extends Consumer<? super E>> T distinct(T target);

	public <T extends Consumer<? super E>> T distinct(T target, Equalator<? super E> equalator);

	public <T extends Consumer<? super E>> T copyTo(T target);

	public <T extends Consumer<? super E>> T filterTo(T target, Predicate<? super E> predicate);

	public <T extends Consumer<? super E>> T union    (XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

	public <T extends Consumer<? super E>> T intersect(XGettingCollection<? extends E> other, Equalator<? super E> equalator, T target);

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
	 * @param o
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
