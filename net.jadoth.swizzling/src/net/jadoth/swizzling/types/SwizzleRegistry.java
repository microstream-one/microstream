package net.jadoth.swizzling.types;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.jadoth.collections.interfaces.Sized;
import net.jadoth.swizzling.exceptions.SwizzleExceptionConsistency;
import net.jadoth.typing.Clearable;

/**
 * Type combining {@link SwizzleObjectRegistry} and {@link SwizzleTypeRegistry}.
 * <p>
 * What might at first look like an implementation detail is actually architectural mandatory because
 * despite architectural diferentiatd tasks, in the end all instances must always be contained
 * in one registry instance for best performance and consistency.
 *
 * @author Thomas Muenz
 */
public interface SwizzleRegistry
extends SwizzleObjectRegistry, SwizzleTypeRegistry, Sized, Clearable, SwizzleTypeIterable //, SwizzleIdCache
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
	public long lookupTypeId(Class<?> type);

	@Override
	public Object lookupObject(long oid);

	@Override
	public <T> Class<T> lookupType(long tid);

	@Override
	public void validateExistingTypeMappings(Iterable<? extends SwizzleTypeLink<?>> mappings)
		throws SwizzleExceptionConsistency;

	@Override
	public boolean registerType(long tid, Class<?> type);

	@Override
	public boolean registerObject(long oid, Object object);

	@Override
	public Object optionalRegisterObject(long oid, Object object);

	@Override
	public void iterateTypes(Consumer<? super SwizzleRegistry.Entry> iterator);

//	@Override
//	public long lookupTypeIdForObjectId(long oid);

//	@Override
//	public Object registerTypeIdForObjectId(long oid, long tid);
	
	public Object registerObjectId(long oid);

	public void clearOrphanEntries();
	
	public void clear(Predicate<? super SwizzleRegistry.Entry> filter);

	public void shrink();

	/**
	 * Cleans up internal data structures, e.g. by removing orphan entries and empty hash chains.
	 * Depending on the implementation and the size of the registry, this can take a considerable amount of time.
	 */
	public void cleanUp();

	public void iterateEntries(Consumer<? super SwizzleRegistry.Entry> iterator);

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
