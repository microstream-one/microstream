package net.jadoth.swizzling.types;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.interfaces.Sized;
import net.jadoth.typing.Clearable;

/**
 *
 * @author Thomas Muenz
 */
public interface SwizzleObjectRegistry
extends SwizzleObjectLookup, Sized, Clearable
{
	/* funny find:
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4990451
	 * welcome to this user code class
	 */

	@Override
	public long size();

	@Override
	public boolean isEmpty();

	public int getHashRange();

	public float getHashDensity();

	public int capacity();

	@Override
	public void clear();

	public boolean containsObjectId(long oid);

	@Override
	public long lookupObjectId(Object object);

	@Override
	public Object lookupObject(long oid);

	public boolean registerObject(long oid, Object object);

	public Object optionalRegisterObject(long oid, Object object);
	
	public Object registerObjectId(long oid);

	public void clearOrphanEntries();
	
	public void clear(Predicate<? super SwizzleObjectRegistry.Entry> filter);

	public void shrink();

	/**
	 * Cleans up internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 */
	public void cleanUp();

	public void iterateEntries(Consumer<? super SwizzleObjectRegistry.Entry> iterator);

	public Object retrieveByOid(long oid);

	public long retrieveByObject(Object object);

	public Class<?> retrieveByTid(long tid);

	public boolean removeById(long id);

	public boolean remove(Object object);
	
	
	public interface Entry
	{
		public long id();
		
		public Object reference();
	}

}
