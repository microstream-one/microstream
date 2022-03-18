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

import java.lang.reflect.Field;

import one.microstream.X;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XGettingSequence;
import one.microstream.hashing.HashEqualator;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGeneric;


public final class BinaryHandlerEqHashEnum
extends AbstractBinaryHandlerCustomCollection<EqHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long
		BINARY_OFFSET_EQUALATOR    =                                                        0, // oid for eqltr ref
		BINARY_OFFSET_HASH_DENSITY = BINARY_OFFSET_EQUALATOR    + Binary.objectIdByteLength(), // offset for 1 oid
		BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES                  // offset for 1 float
	;
	
	// field type detour because there are sadly no field literals in Java (yet?).
	static final Field FIELD_EQULATOR = getInstanceFieldOfType(EqHashEnum.class, HashEqualator.class);



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<EqHashEnum<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)EqHashEnum.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary data)
	{
		return data.read_float(BINARY_OFFSET_HASH_DENSITY);
	}

	public static final void staticStore(
		final Binary              data     ,
		final EqHashEnum<?>       instance ,
		final long                typeId   ,
		final long                objectId ,
		final PersistenceFunction persister
	)
	{
		// store elements simply as array binary form
		data.storeIterableAsList(
			typeId                ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			persister
		);

		// persist hashEqualator and set the resulting oid at its binary place (first header value)
		data.store_long(
			BINARY_OFFSET_EQUALATOR,
			persister.apply(instance.hashEquality())
		);

		// store hash density as second header value
		data.store_float(
			BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity()
		);
	}

	public static final EqHashEnum<?> staticCreate(final Binary data)
	{
		return EqHashEnum.NewCustom(
			getBuildItemElementCount(data),
			getBuildItemHashDensity(data)
		);
	}

	public static final void staticUpdate(
		final Binary                 data    ,
		final EqHashEnum<?>          instance,
		final PersistenceLoadHandler handler
	)
	{
		// must clear to ensure consistency
		instance.clear();

		// length must be checked for consistency reasons
		instance.ensureCapacity(getBuildItemElementCount(data));

		// set equalator instance (must be done on memory-level due to final modifier. Little hacky, but okay)
		XMemory.setObject(
			instance,
			XMemory.objectFieldOffset(FIELD_EQULATOR),
			handler.lookupObject(data.read_long(BINARY_OFFSET_EQUALATOR))
		);

		// collect elements AFTER hashEqualator has been set because it is used in it
		XCollectionsInternals.setSize(instance, data.collectListObjectReferences(
			BINARY_OFFSET_ELEMENTS                                               ,
			handler                                                              ,
			item -> XCollectionsInternals.internalCollectUnhashed(instance, item)
		));
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	public static final void staticComplete(final Binary data, final EqHashEnum<?> instance)
	{
		// rehash all previously unhashed collected elements
		instance.rehash();
	}

	public static final void staticIterateInstanceReferences(
		final EqHashEnum<?>       instance,
		final PersistenceFunction iterator
	)
	{
		iterator.apply(instance.equality());
		Persistence.iterateReferences(iterator, instance);
	}

	public static final void staticIteratePersistedReferences(
		final Binary                     data    ,
		final PersistenceReferenceLoader iterator
	)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_EQUALATOR));
		data.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberFieldGeneric> Fields()
	{
		return SimpleArrayFields(
			CustomField(HashEqualator.class, "hashEqualator"),
			CustomField(float.class, "hashDensity")
		);
	}
	
	public static BinaryHandlerEqHashEnum New()
	{
		return new BinaryHandlerEqHashEnum();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerEqHashEnum()
	{
		// binary layout definition
		super(
			handledType(),
			Fields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final EqHashEnum<?>                   instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		staticStore(data, instance, this.typeId(), objectId, handler);
	}

	@Override
	public final EqHashEnum<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return staticCreate(data);
	}

	@Override
	public final void updateState(final Binary data, final EqHashEnum<?> instance, final PersistenceLoadHandler handler)
	{
		staticUpdate(data, instance, handler);
	}

	@Override
	public final void complete(final Binary data, final EqHashEnum<?> instance, final PersistenceLoadHandler handler)
	{
		staticComplete(data, instance);
	}

	@Override
	public final void iterateInstanceReferences(final EqHashEnum<?> instance, final PersistenceFunction iterator)
	{
		staticIterateInstanceReferences(instance, iterator);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		staticIteratePersistedReferences(data, iterator);
	}

}
