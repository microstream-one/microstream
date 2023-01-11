package one.microstream.persistence.binary.java.util;

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

import java.util.IdentityHashMap;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceException;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerIdentityHashMap extends AbstractBinaryHandlerCustomCollection<IdentityHashMap<?, ?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_ELEMENTS = 0;
	// to prevent recurring confusion: IdentityHashMap really has no loadFactor. It uses an open addressing hash array.



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<IdentityHashMap<?, ?>> handledType()
	{
		return (Class)IdentityHashMap.class; // no idea how to get ".class" to work otherwise
	}

	static final int getElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountKeyValue(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerIdentityHashMap New()
	{
		return new BinaryHandlerIdentityHashMap();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerIdentityHashMap()
	{
		super(
			handledType(),
			keyValuesFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final IdentityHashMap<?, ?>           instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeMapEntrySet(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance.entrySet()   ,
			handler
		);
	}
	
	@Override
	public final IdentityHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new IdentityHashMap<>(
			getElementCount(data)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final IdentityHashMap<?, ?>  instance,
		final PersistenceLoadHandler handler
	)
	{
		instance.clear();
		
		@SuppressWarnings("unchecked")
		final IdentityHashMap<Object, Object> castedInstance = (IdentityHashMap<Object, Object>)instance;
		
		// IdentityHashMap does not need the elementsHelper detour as identity hashing does not depend on contained data
		data.collectKeyValueReferences(
			BINARY_OFFSET_ELEMENTS,
			getElementCount(data),
			handler,
			(k, v) ->
			{
				if(castedInstance.putIfAbsent(k, v) != null)
				{
					throw new BinaryPersistenceException(
						"Duplicate key reference in " + IdentityHashMap.class.getSimpleName()
						+ " " + XChars.systemString(instance)
					);
				}
			}
		);
	}
	
	@Override
	public final void iterateInstanceReferences(final IdentityHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferencesMap(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateKeyValueEntriesReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
