package one.microstream.collections.interfaces;


/**
 * The capacity of a capacity carrying type (e.g. a collection) defines the amount of elements it can carry
 * in the current state before an internal rebuild becomes necessary. The capacity can be, but does not have to be,
 * the size of the internal storage (e.g. an array) itself. It can also be a meta value derived from the actual
 * storage size, like "threshold" in hash collections.
 *
 * 
 *
 */
public interface CapacityExtendable extends CapacityCarrying
{
	public CapacityExtendable ensureCapacity(long minimalCapacity);

	/**
	 * Ensures that the next {@literal minimalFreeCapacity} elements can be actually added in a fast way,
	 * meaning for example no internal storage rebuild will be necessary.
	 * 
	 * @param minimalFreeCapacity
	 */
	public CapacityExtendable ensureFreeCapacity(long minimalFreeCapacity);

	/**
	 * Returns the current amount of elements this instance can hold before a storage rebuild becomes necessary.
	 * <p>
	 * For carrier implementations that don't have a concept of storage rebuilding (like linked list for example)
	 * this method returns the same value as {@link #maximumCapacity()}.
	 *
	 * @return the current capacity of this instance before a rebuild is required.
	 */
	public long currentCapacity();



	public default long currentFreeCapacity()
	{
		return this.currentCapacity() - this.size();
	}

}
