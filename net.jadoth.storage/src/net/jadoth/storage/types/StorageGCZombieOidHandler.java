package net.jadoth.storage.types;

import net.jadoth.persistence.types.Persistence;

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
 * Note that ConstantIds for JLS constants and TypeIds are intentionally unresolvable in the persistent state.
 * @see Persistence.IdType#TID
 * @see Persistence.IdType#CID
 *
 */
@FunctionalInterface
public interface StorageGCZombieOidHandler
{
	public boolean handleZombieOid(long oid);



	public final class Implementation implements StorageGCZombieOidHandler
	{
		@Override
		public final boolean handleZombieOid(final long oid)
		{
			/*
			 * Note that types and constants are intentionally not represented in the persistent form
			 * but are resolved at runtime by the loading mechanism.
			 * It is NOT an error that these OIDs cannot be resolved on the persistent form level.
			 */
			if(Persistence.IdType.TID.isInRange(oid))
			{
				// debug hook for TypeIDs
				return true;
			}
			if(Persistence.IdType.CID.isInRange(oid))
			{
				// debug hook for ConstantIDs
				return true;
			}

			// default implementation just naively warns about the problem
			DEBUGStorage.println("GC marking encountered zombie OID " + oid);

			return true;
		}
	}
}
