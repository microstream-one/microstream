package one.microstream.collections.types;

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
