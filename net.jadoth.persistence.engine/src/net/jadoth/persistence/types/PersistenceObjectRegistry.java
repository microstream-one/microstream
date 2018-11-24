package net.jadoth.persistence.types;

import java.util.function.Consumer;
import java.util.function.Predicate;


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

	public void iterateEntries(Consumer<? super PersistenceObjectRegistry.Entry> iterator);

	// general querying //

	public long size();

	public boolean isEmpty();

	public int hashRange();

	public float hashDensity();

	public int capacity();
		
	// registering //
	
	public boolean registerObject(long objectId, Object object);

	public Object optionalRegisterObject(long objectId, Object object);
	
	public Object registerObjectId(long objectId);

	// housekeeping //

	/**
	 * Cleans up internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 */
	public void cleanUp();
	
	public void clearOrphanEntries();

	public void shrink();
	
	// removing //
	
	public boolean removeObjectById(long id);

	public boolean removeObject(Object object);

	public void clear();
	
	public void clear(Predicate<? super PersistenceObjectRegistry.Entry> filter);
	
	
	
	public interface Entry
	{
		public long id();
		
		public Object reference();
	}

}
