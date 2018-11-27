package net.jadoth.persistence.types;

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

	public long capacity();
		
	// registering //
	
	public boolean registerObject(long objectId, Object object);

	public Object optionalRegisterObject(long objectId, Object object);
	

	// (26.11.2018 TM)NOTE: since the removal of the TID registration, the registerObjectId method is nonsense.
//	public Object registerObjectId(long objectId);

	// housekeeping //

	/**
	 * Cleans up internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 */
	public void cleanUp();
	
	public void clearOrphanEntries();

	public void shrink();
	
	// removing //
	
	public boolean removeObjectById(long objectId);

	public boolean removeObject(Object object);
	
	public <P extends PersistencePredicate> P removeObjectsBy(P filter);

	public void clear();
	

	// (27.11.2018 TM)FIXME: JET-48: methods for constants handling
	
}
