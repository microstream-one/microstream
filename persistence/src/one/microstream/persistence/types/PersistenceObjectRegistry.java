package one.microstream.persistence.types;

import one.microstream.collections.types.XGettingTable;
import one.microstream.hashing.HashStatistics;
import one.microstream.persistence.internal.DefaultObjectRegistry;
import one.microstream.util.Cloneable;

/**
 * A registry type for biunique associations of arbitrary objects with ids.
 *
 * @author Thomas Muenz
 */
public interface PersistenceObjectRegistry extends PersistenceSwizzlingLookup, Cloneable<PersistenceObjectRegistry>
{
	/* funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * welcome to this user code class
	 */
	
	/**
	 * Useful for {@link PersistenceContextDispatcher}.
	 * @return A Clone of this instance as described in {@link Cloneable}.
	 */
	@Override
	public PersistenceObjectRegistry Clone();
	
	// entry querying //

	@Override
	public long lookupObjectId(Object object);

	@Override
	public Object lookupObject(long objectId);

	public boolean containsObjectId(long objectId);

	public <A extends PersistenceAcceptor> A iterateEntries(A acceptor);

	// general querying //

	public long size();

	public boolean isEmpty();

	public int hashRange();

	public float hashDensity();
	
	public long minimumCapacity();

	/**
	 * Returns the current size potential before a (maybe costly) rebuild becomes necessary.
	 */
	public long capacity();
	
	public boolean setHashDensity(float hashDensity);
	
	public boolean setMinimumCapacity(long minimumCapacity);
	
	public boolean setConfiguration(float hashDensity, long  minimumCapacity);
	
	/**
	 * Makes sure the internal storage structure is prepared to provide a {@link #capacity()} of at least
	 * the passed capacity value.
	 * 
	 * @param capacity
	 * @return whether a rebuild of internal storage structures was necessary.
	 */
	public boolean ensureCapacity(long capacity);
		
	// registering //
	
	public boolean registerObject(long objectId, Object object);

	public Object optionalRegisterObject(long objectId, Object object);
	
	public boolean registerConstant(final long objectId, final Object constant);

	/**
	 * Consolidate internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 * 
	 * @return whether a rebuild was required.
	 */
	public boolean consolidate();

	/**
	 * Clears all entries except those that are essential for a correctly executed program (e.g. constants).
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 */
	public void clear();

	/**
	 * Clears all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.
	 * Clearing means to leave the current capacity as it is and just to actually clear its entries.
	 */
	public void clearAll();
	
	/**
	 * Truncates all entries except those that are essential for a correctly executed program (e.g. constants).
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 */
	public void truncate();

	/**
	 * Truncates all entries, including those that are essential for a correctly executed program (e.g. constants),
	 * effectively leaving a completely empty registry.
	 * Truncating means to quickly empty the registry by reinitializing the internal storage structures with a
	 * new and minimal capacity.
	 */
	public void truncateAll();
	
	// removing logic is not viable except for testing purposes, which can be done implementation-specific.
	
	public XGettingTable<String, ? extends HashStatistics> createHashStatistics();
	
	
	
	public static DefaultObjectRegistry New()
	{
		return DefaultObjectRegistry.New();
	}
	
}
