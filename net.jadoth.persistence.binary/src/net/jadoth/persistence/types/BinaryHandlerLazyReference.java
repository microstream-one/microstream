package net.jadoth.persistence.types;

import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNativeCustom;
import net.jadoth.persistence.binary.types.Binary;
import net.jadoth.persistence.binary.types.BinaryPersistence;
import net.jadoth.persistence.types.Lazy;
import net.jadoth.persistence.types.PersistenceBuildLinker;
import net.jadoth.persistence.types.PersistenceHandler;


public final class BinaryHandlerLazyReference extends AbstractBinaryHandlerNativeCustom<Lazy<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constructors     //
	/////////////////////

	public BinaryHandlerLazyReference()
	{
		super(
			Lazy.genericType(),
			pseudoFields(
				pseudoField(Object.class, "subject")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// override methods //
	/////////////////////

	@Override
	public void store(final Binary bytes, final Lazy<?> instance, final long oid, final PersistenceHandler handler)
	{
		/* (29.09.2015 TM)NOTE: There are several cases that have to be handled here correctly:
		 *
		 * 1) objectId == 0, referent == null
		 * "Empty" lazy reference that must be stored as such
		 *
		 * 2.) objectId == 0, referent != null
		 * Newly created lazy reference with a referent. The referent has to be handled and the lazy reference has
		 * to be stored with the referent's OID
		 *
		 * 3.) objectId != null, referent == null
		 * The lazy reference represents a non-null referent that is currently simply not loaded. The lazy reference
		 * must be stored nonetheless, pointing to its known referent objectId
		 *
		 * 4.) objectId != null, referent != null
		 * The lazy reference represents a non-null referent that is currently loaded. The refernt must be handled,
		 * the lazy reference must be stored, pointing to its known referent objectId.
		 */

		final Object referent = instance.peek();
		final long referenceOid;

		if(referent == null)
		{
			// (29.09.2015 TM)NOTE: The special casing below is wrong: the lazy instance must be stored in any case.
			referenceOid = instance.objectId;

//			// if the lazy instance already knows the OID and its reference has simply been cleared, don't store null!
//			if(instance.objectId != Persistence.nullId())
//			{
//				return;
//			}
//			referenceOid = Persistence.nullId();
		}
		else
		{
			// OID validation or updating is done by linking logic
			referenceOid = handler.apply(referent);
		}

		// link to object supplier (internal logic can either update, discard or throw exception on mismatch)
		instance.link(referenceOid, handler.getSwizzleObjectSupplier());

		// lazy reference instance must be stored in any case
		XVM.set_long(
			bytes.storeEntityHeader(BinaryPersistence.referenceBinaryLength(1), this.typeId(), oid),
			referenceOid
		);
	}

	@Override
	public Lazy<?> create(final Binary bytes)
	{
		/* (27.04.2016 TM)NOTE: registering a Lazy instance with a reference manager
		 * without having the object supplier set yet might cause an inconsistency if the
		 * LRM iterates lazy references before the update added the supplier reference.
		 * ON the other hand: the lazy reference instance is not yet completed and whatever
		 * logic iterates over the LRM's entries shouldn't rely on anything.
		 */
		return Lazy.New(XVM.get_long(bytes.buildItemAddress()), null);
	}

	@Override
	public final void update(final Binary bytes, final Lazy<?> instance, final PersistenceBuildLinker builder)
	{
		/* intentionally no subject lookup here as premature strong referencing
		 * might defeat the purpose of memory freeing lazy referencing if no
		 * other strong reference to the subject is present at the moment.
		 */
		instance.setLoader(builder.getSwizzleObjectSupplier());
	}

	@Override
	public final void complete(final Binary medium, final Lazy<?> instance, final PersistenceBuildLinker builder)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}

	@Override
	public final boolean hasInstanceReferences()
	{
		return true;
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	

}
