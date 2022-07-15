package one.microstream.persistence.binary.jdk17.java.util;

/*-
 * #%L
 * MicroStream Persistence JDK17
 * %%
 * Copyright (C) 2019 - 2021 MicroStream Software
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

import java.util.Collection;

import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

/**
 * Generic abstract class for specialized handler for
 * java.util.ImmutableCollections.Set12<E> and java.util.ImmutableCollections.List12<E>
 * in JDK 15 and later
 *
 * The handler takes the internal constant java.util.ImmutableCollections.EMPTY
 * into account which must not be persisted.
 */
public abstract class AbstractBinaryHandlerGenericImmutableCollections12<T> extends AbstractBinaryHandlerCustom<T>
{
	///////////////////////////////////////////////////////////////////////////
	// abstract methods //
	/////////////////////

	protected abstract T createInstance(Object e0, Object e1);


	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_E0 = 0;
	private static final long BINARY_OFFSET_E1 = BINARY_OFFSET_E0 + Binary.referenceBinaryLength(1);
	private static final long BINARY_LENGTH    = BINARY_OFFSET_E1 + Binary.referenceBinaryLength(1);


	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	protected AbstractBinaryHandlerGenericImmutableCollections12(final Class<T> type)
	{
		super(type,
			CustomFields(
				CustomField(Object.class, "e0"),
				CustomField(Object.class, "e1")
				));
	}


	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public T create(final Binary data, final PersistenceLoadHandler handler)
	{
		Object e0 = null;
		Object e1 = null;

		long objectId = data.read_long(BINARY_OFFSET_E0);
		if(objectId > 0L)
		{
			e0 = handler.getPersister().getObject(objectId);
		}

		objectId = data.read_long(BINARY_OFFSET_E1);
		if(objectId > 0L)
		{
			e1 = handler.getPersister().getObject(objectId);
		}

		return this.createInstance(e0, e1);
	}

	@Override
	public void store(final Binary data, final T instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		final Collection<?> items = (Collection<?>)instance;
		final int size = items.size();
		final Object[] arr = items.toArray();

		data.storeEntityHeader(BINARY_LENGTH, this.typeId(), objectId);

		if(size == 0)
		{
			data.store_long(BINARY_OFFSET_E0, handler.apply(null));
			data.store_long(BINARY_OFFSET_E1, handler.apply(null));
		}
		if(size == 1)
		{
			data.store_long(BINARY_OFFSET_E0, handler.apply(arr[0]));
			data.store_long(BINARY_OFFSET_E1, handler.apply(null));
		}
		if(size == 2)
		{
			data.store_long(BINARY_OFFSET_E0, handler.apply(arr[0]));
			data.store_long(BINARY_OFFSET_E1, handler.apply(arr[1]));
		}
	}

	@Override
	public void updateState(final Binary data, final T instance, final PersistenceLoadHandler handler)
	{
		//no-op
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return true;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		//no-op
	}

}
