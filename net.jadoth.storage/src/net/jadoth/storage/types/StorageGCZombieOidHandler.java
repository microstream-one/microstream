package net.jadoth.storage.types;

/**
 * Note on zombie OID / null entry during GC:
 * This should of course never happen and must be seen as a bug.
 * However the GC is not necessarily the place to break in such a case.
 * Currently, this can even happen regularly, if the last reference to an entity is removed, then the GC
 * deleted the entity and then a store reestablishes the reference to the then deleted entity.
 * This might be a bug in the user code or in the storer or swizzle registry or whatever, but it should
 * be recognized and handled at that point, not break the GC.
 * For that reason, handling an encountered zombie OID is modularized with the default of ignoring it.
 *
 */
@FunctionalInterface
public interface StorageGCZombieOidHandler
{
	public boolean handleZombieOid(long oid);


	/**
	 * Default implementation always returns <code>true</code>.
	 *
	 * @author TM
	 */
	public final class Implementation implements StorageGCZombieOidHandler
	{
		@Override
		public final boolean handleZombieOid(final long oid)
		{
			return true;
		}
	}
}
