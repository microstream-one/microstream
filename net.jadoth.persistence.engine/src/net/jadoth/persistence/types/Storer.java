package net.jadoth.persistence.types;

/**
 * A type extending the simple {@link PersistenceStoring} to have stateful store handling.
 * This can be used to do what is generally called "transactions": preprocess data to be stored and then store
 * either all or nothing.<br>
 * It can also be used to skip certain references, etc.<br>
 * The naming (missing "Persistence" prefix) is intentional to support convenience on the application code level.
 *
 * @author TM
 */
public interface Storer extends PersistenceStoring
{
	public Object commit();

	public void clear();

	public void registerSkip(Object instance, long oid);

	public void clearRegistered();

	public void registerSkip(Object instance);

	public long size();

	public default boolean isEmpty()
	{
		return this.size() == 0L;
	}

	/**
	 * Returns the internal state's value significant for its capacity of unique instances.
	 * Note that the exact meaning of this value is implementation dependant, e.g. it might just be a hash table's
	 * length, while the actual amount of unique instances that can be handled by that hash table might be
	 * much higher (infinite).
	 *
	 * @return the current implementation-specific "capacity" value.
	 */
	public long currentCapacity();

	/**
	 * The maximum value that {@link #currentCapacity()} can reach. For more explanation on the exact meaning of the
	 * capacity, see there.
	 *
	 * @return the maximum of the implementation-specific "capacity" value.
	 */
	public long maximumCapacity();

	public boolean isInitialized();

	/**
	 * Ensures the storer instance is initialized, i.e. ready to perform storing.
	 * This method is idempotent.
	 * For a forced (re)initialization, see {@link #reinitialize()}.
	 *
	 * @return this.
	 */
	public Storer initialize();

	/**
	 * Ensures the storer instance is initialized, i.e. ready to perform storing.
	 * If the storer instance needs to be initialized as a consequence of this call, the passed {@code initialCapacity}
	 * is considered as an estimate for the number of unique instances to be handled until the next commit.
	 * This method is idempotent, meaning if this instance is already initialized, the passed value might not have
	 * any effect.
	 * For a forced (re)initialization, see {@link #reinitialize(long)}.
	 *
	 * @return this.
	 */
	public Storer initialize(long initialCapacity);

	/**
	 * Enforces the instance to be initialized, discarding any previous state (clearing it) if necessary prior to
	 * calling {@link #initialize()}.
	 *
	 * @return this.
	 */
	public Storer reinitialize();

	/**
	 * Enforces the instance to be initialized, discarding any previous state (clearing it) if necessary prior to
	 * calling {@link #initialize()}.
	 *
	 * @return this.
	 */
	public Storer reinitialize(long initialCapacity);

	/**
	 * Ensures that the instance's internal state is prepared for handling an amount of unique instance equal to
	 * the passed value. Note that is explicitly does not have to mean that the instance's internal state actually
	 * reserves as much space, only makes a best effort to prepare for that amount. Example: an internal hash table's
	 * hash length might still remain at 2^30, despite the passed value being much higher.
	 *
	 * @param desiredCapacity the amount of unique instances that this instance shall prepare to handle.
	 * @return this
	 */
	public Storer ensureCapacity(long desiredCapacity);

}
