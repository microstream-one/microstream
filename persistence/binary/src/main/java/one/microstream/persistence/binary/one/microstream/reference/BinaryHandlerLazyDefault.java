package one.microstream.persistence.binary.one.microstream.reference;

/*-
 * #%L
 * microstream-persistence-binary
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import java.lang.reflect.Constructor;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reference.Lazy;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reflect.XReflect;


public final class BinaryHandlerLazyDefault extends AbstractBinaryHandlerCustom<Lazy.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerLazyDefault New()
	{
		return new BinaryHandlerLazyDefault();
	}
	
	@SuppressWarnings("rawtypes")
	static final Constructor<Lazy.Default> CONSTRUCTOR = XReflect.setAccessible(
		XReflect.getDeclaredConstructor(Lazy.Default.class, Object.class, long.class, ObjectSwizzling.class)
	);
		
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerLazyDefault()
	{
		super(
			Lazy.Default.genericType(),
			CustomFields(
				CustomField(Object.class, "subject")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final Lazy.Default<?>                 instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
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
			referenceOid = instance.objectId();
		}
		else
		{
			// OID validation or updating is done by linking logic
			referenceOid = handler.apply(referent);
		}

		// link to object supplier (internal logic can either update, discard or throw exception on mismatch)
		instance.$link(referenceOid, handler.getObjectRetriever());

		// lazy reference instance must be stored in any case
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		data.store_long(referenceOid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final Lazy.Default<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		/* (27.04.2016 TM)NOTE: registering a Lazy instance with a reference manager
		 * without having the object supplier set yet might cause an inconsistency if the
		 * LRM iterates lazy references before the update added the supplier reference.
		 * ON the other hand: the lazy reference instance is not yet completed and whatever
		 * logic iterates over the LRM's entries shouldn't rely on anything.
		 */
		final long objectId = data.read_long(0);
		
		return Lazy.register(
			XReflect.invoke(CONSTRUCTOR, null, objectId, null)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final Lazy.Default<?>        instance,
		final PersistenceLoadHandler handler
	)
	{
		/*
		 * Intentionally no subject lookup here as premature strong referencing
		 * might defeat the purpose of memory freeing lazy referencing if no
		 * other strong reference to the subject is present at the moment.
		 */
		instance.$setLoader(handler.getObjectRetriever());
	}

	@Override
	public final void complete(
		final Binary                 data    ,
		final Lazy.Default<?>        instance,
		final PersistenceLoadHandler handler
	)
	{
		// no-op for normal implementation (see non-reference-hashing collections for other examples)
	}
	
	@Override
	public final boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public final boolean hasPersistedVariableLength()
	{
		return false;
	}
	
	@Override
	public final boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public final void iterateLoadableReferences(
		final Binary                     offset  ,
		final PersistenceReferenceLoader iterator
	)
	{
		// the lazy reference is not naturally loadable, but special-handled by this handler
	}

}
