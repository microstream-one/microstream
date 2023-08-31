package one.microstream.persistence.binary.one.microstream.collections.lazy;

/*-
 * #%L
 * MicroStream Persistence Binary
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

import java.lang.reflect.Field;

import one.microstream.collections.lazy.LazyHashMap;
import one.microstream.collections.lazy.LazyHashSet;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerLazyHashSet extends AbstractBinaryHandlerCustom<LazyHashSet<?>>{

	private static final Field FIELD_MAP = getMapField();
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LazyHashSet<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LazyHashSet.class;
	}
	
	public static BinaryHandlerLazyHashSet New()
	{
		return new BinaryHandlerLazyHashSet();
	}
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryHandlerLazyHashSet()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(LazyHashMap.class, "map")
			)
		);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public boolean hasPersistedReferences() {
		return true;
	}
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.read_long(0));
	}

	@Override
	public void updateState(final Binary data, final LazyHashSet<?> instance, final PersistenceLoadHandler handler)
	{
		final LazyHashMap<?,?> map = (LazyHashMap<?, ?>)handler.lookupObject(data.read_long(0));
		XReflect.setFieldValue(FIELD_MAP, instance, map);
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}

	@Override
	public void store(final Binary data, final LazyHashSet<?> instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		data.storeEntityHeader(Binary.referenceBinaryLength(1), this.typeId(), objectId);
		data.store_long(handler.applyEager(XReflect.getFieldValue(FIELD_MAP, instance)));
	}

	@Override
	public LazyHashSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new LazyHashSet<>();
	}
	
	private static Field getMapField()
	{
		final Field field = XReflect.getAnyField(LazyHashSet.class, "map");
		return XReflect.setAccessible(field);
	}
}
