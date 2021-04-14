package one.microstream.collections.types;

/**
 * Putting aspect:<br>
 * Ensure that all putted elements are contained in the collection
 * <p>
 * Examples:
 * Set: Add all elements, overwriting equal already contained elements.
 * Bag: Always add all elements (identical to add)
 * <p>
 * Note: Correspond's to old JDK collection's "add()" for single dimensional collections (which is conceptionally
 * misleading) and "put()" for Map.
 *
 * 
 */
public interface XPuttingCollection<E> extends XAddingCollection<E>
{
	public interface Creator<E> extends XAddingCollection.Creator<E>
	{
		@Override
		public XPuttingCollection<E> newInstance();
	}



	public boolean put(E element);
	
	public boolean nullPut();
	
	@SuppressWarnings("unchecked")
	public XPuttingCollection<E> putAll(E... elements);
	
	public XPuttingCollection<E> putAll(E[] elements, int srcStartIndex, int srcLength);
	
	public XPuttingCollection<E> putAll(XGettingCollection<? extends E> elements);

}
