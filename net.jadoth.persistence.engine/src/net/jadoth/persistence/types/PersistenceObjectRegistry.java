package net.jadoth.persistence.types;

import net.jadoth.collections.types.XGettingTable;
import net.jadoth.hashing.HashStatistics;
import net.jadoth.persistence.internal.ObjectRegistryCrazyArrays;

/**
 * A registry type for biunique associations of arbitrary objects with ids.
 *
 * @author Thomas Muenz
 */
public interface PersistenceObjectRegistry extends PersistenceObjectLookup
{
	/* funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * welcome to this user code class
	 */
	
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
	
	public PersistenceObjectRegistry setHashDensity(float hashDensity);

	public long capacity();
		
	// registering //
	
	public boolean registerObject(long objectId, Object object);

	public Object optionalRegisterObject(long objectId, Object object);
	
	public default boolean registerConstant(final long objectId, final Object constant)
	{
		return this.registerObject(objectId, constant);
	}

	/**
	 * Cleans up internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 */
	public void cleanUp();
	
	// removing logic is not viable except for testing purposes, which can be done implementation-specific.
//	public boolean removeObjectById(long objectId);
//
//	public boolean removeObject(Object object);
//
//	public <P extends PersistencePredicate> P removeObjectsBy(P filter);

	/**
	 * Clears only non-constants.
	 */
	public default void clear()
	{
		this.truncate();
	}
	
	/**
	 * Clears everything, including constants.
	 */
	public void truncate();
	
	public XGettingTable<String, ? extends HashStatistics> createHashStatistics();
	
	
	
	public static ObjectRegistryCrazyArrays New()
	{
		return ObjectRegistryCrazyArrays.New();
	}
	// (29.11.2018 TM)FIXME: switch default implementation
//	public static DefaultObjectRegistry New()
//	{
//		return DefaultObjectRegistry.New();
//	}
	
}
