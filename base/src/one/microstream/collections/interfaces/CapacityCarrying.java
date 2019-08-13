package one.microstream.collections.interfaces;

public interface CapacityCarrying extends Sized
{
	/**
	 * Returns the maximum amount of elements this carrier instance can contain.<br>
	 * The actual value may be depend on the configuration of the concrete instance or may depend only on the
	 * implementation of the carrier (meaning it is constant for all instances of the implementation,
	 * e.g. {@link Integer#MAX_VALUE})
	 *
	 * @return the maximum amount of elements this carrier instance can contain.
	 */
	public long maximumCapacity();

	/**
	 * Returns the amount of elements this carrier instance can collect before reaching its maximimum capacity.
	 *
	 */
	public default long remainingCapacity()
	{
		return this.maximumCapacity() - this.size();
	}

	/**
	 * Returns true if the current capacity cannot be increased any more.
	 */
	public default boolean isFull()
	{
		return this.remainingCapacity() == 0L;
	}

}
