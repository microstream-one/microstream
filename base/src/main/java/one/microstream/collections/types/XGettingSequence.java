package one.microstream.collections.types;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

import one.microstream.collections.interfaces.ExtendedSequence;
import one.microstream.exceptions.IndexBoundsException;

/**
 *
 * 
 */
public interface XGettingSequence<E> extends XGettingCollection<E>, ExtendedSequence<E>, XIndexIterable<E>
{
	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	/**
	 * Creates a true copy of this list which references th same elements in the same order as this list does
	 * at the time the method is called. The elements themselves are NOT copied (no deep copying).<br>
	 * The type of the returned list is the same as of this list if possible (i.e.: a SubList can not meaningful
	 * return a true copy that references its elements but still is a SubList)
	 *
	 * @return a copy of this list
	 */
	@Override
	public XGettingSequence<E> copy();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public XImmutableSequence<E> immure();

	/**
	 * Gets the first element in the collection. This is a parameterless alias vor {@code at(0)}.
	 * <p>
	 * {@code first() is an alias for this method for consistency reasons with last()}.
	 * <p>
	 *
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

	/*
	 * This method was named "get(int)" for a long time. However, this common name has numerous problems:
	 * - collides with get(Integer key) of a Map<Integer, ?> type
	 * - collides with get()
	 * - "get" for accessing an index is actually quite blurry. Get what and how?
	 *    The precise name would be "getElementAtIndex(int)". However this is quite verbose, so a shortening compromise
	 *    has to be made. So the question is, what is the preferable compromise:
	 *    an ubiquitous "get" because in doubt anything and everything is called "get",
	 *    or a linguistic reference to the index accessing nature, the "... at ..." relation?
	 *    Considering the other two problems and considering that "at" is even a little shorter than "get",
	 *    the best decision for clarity and expressiveness is already made.
	 */
	public E at(long index) throws IndexBoundsException; // get element at index or throw IndexOutOfBoundsException

	/**
	 * Equivalent to "contains index". Compare: {@link XGettingTable} table with table.keys().contains(index)
	 *
	 * @param index
	 * @return <tt>true</tt> if the passed index is greater or equal to 0,
	 * {@link #size()} is greater than the passed index
	 * and {@link #at(long)} returns a non-null value.
	 */
	public default boolean hasIndex(final long index)
	{
		return index >= 0 && this.size() > index && this.at(index) != null;
	}

	public E first() throws IndexBoundsException; // get first element or throw IndexOutOfBoundsException if empty

	public E last() throws IndexBoundsException;  // get last  element or throw IndexOutOfBoundsException if empty

	public E poll();  // get first element or null if empty (like polling an empty collection to get filled)

	public E peek();  // get last  element or null if empty (like peeking on a stack without popping)

	public long maxIndex(Comparator<? super E> comparator);

	public long minIndex(Comparator<? super E> comparator);

	public long indexOf(E element);

	public long indexBy(Predicate<? super E> predicate);

	public long lastIndexOf(E element);

	public long lastIndexBy(Predicate<? super E> predicate);

	/**
	 * Iterates through the collection and returns the index of the last element that the passed {@link Predicate}
	 * applied to ("scanning").
	 *
	 * @param predicate
	 * @return the index of the last positively tested element.
	 */
	public long scan(Predicate<? super E> predicate);

	public boolean isSorted(Comparator<? super E> comparator);

	public XGettingSequence<E> toReversed();

	public <T extends Consumer<? super E>> T copySelection(T target, long... indices);

	@Override
	public XGettingSequence<E> view();

	public XGettingSequence<E> view(long lowIndex, long highIndex);

	public XGettingSequence<E> range(long lowIndex, long highIndex);



	public interface Factory<E> extends XGettingCollection.Creator<E>
	{
		@Override
		public XGettingSequence<E> newInstance();
	}

}
