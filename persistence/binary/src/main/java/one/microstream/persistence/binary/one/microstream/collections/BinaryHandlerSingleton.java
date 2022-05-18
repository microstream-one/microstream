package one.microstream.persistence.binary.one.microstream.collections;

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

import one.microstream.collections.Singleton;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


// (28.12.2019 TM)NOTE: the sole purpose of this implementation is to provide the explicit method #getReferenceObjectId
public final class BinaryHandlerSingleton extends AbstractBinaryHandlerCustomCollection<Singleton<Object>>
{
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	private static long binaryOffsetReference()
	{
		return 0L;
	}

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<Singleton<Object>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)Singleton.class;
	}
	
	public static long getReferenceObjectId(final Binary data)
	{
		return data.read_long(binaryOffsetReference());
	}
	
	public static BinaryHandlerSingleton New()
	{
		return new BinaryHandlerSingleton();
	}

	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerSingleton()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(Object.class, "element")
			)
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public void store(
		final Binary                          data    ,
		final Singleton<Object>               instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		final long referenceObjectId = handler.apply(instance.get());
		data.store_long(referenceObjectId);
	}

	@Override
	public Singleton<Object> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return Singleton.New(null);
	}

	@Override
	public void updateState(final Binary data, final Singleton<Object> instance, final PersistenceLoadHandler handler)
	{
//		@SuppressWarnings("unchecked")
//		final Singleton<Object> casted = instance;
		
		final long refObjectId = getReferenceObjectId(data);
		final Object reference = handler.lookupObject(refObjectId);
		XCollectionsInternals.setElement(instance, reference);
	}
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		final long refObjectId = getReferenceObjectId(data);
		iterator.acceptObjectId(refObjectId);
	}
	
	@Override
	public void iterateInstanceReferences(final Singleton<Object> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.get());
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

}
