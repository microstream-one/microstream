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
import one.microstream.reference.ControlledLazyReference;
import one.microstream.reference.Lazy;
import one.microstream.reference.ObjectSwizzling;
import one.microstream.reflect.XReflect;

/**
 * Nearly identical to {@link BinaryHandlerLazyDefault} except
 * the handled type. That is {@link one.microstream.reference.ControlledLazyReference.Default}.
 * 
 */
public final class BinaryHandlerControlledLazy extends AbstractBinaryHandlerCustom<ControlledLazyReference.Default<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	public static BinaryHandlerControlledLazy New()
	{
		return new BinaryHandlerControlledLazy();
	}
	
	@SuppressWarnings("rawtypes")
	static final Constructor<ControlledLazyReference.Default> CONSTRUCTOR = XReflect.setAccessible(
		XReflect.getDeclaredConstructor(ControlledLazyReference.Default.class, Object.class, long.class, ObjectSwizzling.class)
	);
		
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ControlledLazyReference.Default<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ControlledLazyReference.Default.class;
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerControlledLazy()
	{
		super(
			handledType(),
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
		final Binary                             data    ,
		final ControlledLazyReference.Default<?> instance,
		final long                               objectId,
		final PersistenceStoreHandler<Binary>    handler
	)
	{
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
	public final ControlledLazyReference.Default<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final long objectId = data.read_long(0);
		
		return Lazy.register(
			XReflect.invoke(CONSTRUCTOR, null, objectId, null)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final ControlledLazyReference.Default<?>        instance,
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
		final ControlledLazyReference.Default<?>        instance,
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
