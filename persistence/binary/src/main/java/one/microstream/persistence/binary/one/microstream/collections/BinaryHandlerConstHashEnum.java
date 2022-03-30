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

import one.microstream.X;
import one.microstream.collections.ConstHashEnum;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConstHashEnum
extends AbstractBinaryHandlerCustomCollection<ConstHashEnum<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_HASH_DENSITY =                                        0;
	static final long BINARY_OFFSET_ELEMENTS     = BINARY_OFFSET_HASH_DENSITY + Float.BYTES; // one float offset to sized array



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstHashEnum<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstHashEnum.class;
	}

	private static int getBuildItemElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}

	private static float getBuildItemHashDensity(final Binary data)
	{
		return data.read_float(BINARY_OFFSET_HASH_DENSITY);
	}
	
	public static BinaryHandlerConstHashEnum New()
	{
		return new BinaryHandlerConstHashEnum();
	}
	
	



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConstHashEnum()
	{
		// binary layout definition
		super(
			handledType(),
			SimpleArrayFields(CustomField(float.class, "hashDensity"))
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final ConstHashEnum<?>                instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		// store elements simply as array binary form
		data.storeIterableAsList(
			this.typeId()         ,
			objectId              ,
			BINARY_OFFSET_ELEMENTS,
			instance              ,
			instance.size()       ,
			handler
		);

		// store hash density as (sole) header value
		data.store_float(
			BINARY_OFFSET_HASH_DENSITY,
			instance.hashDensity()
		);
	}

	@Override
	public final ConstHashEnum<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ConstHashEnum.NewCustom(
			getBuildItemElementCount(data),
			getBuildItemHashDensity(data)
		);
	}

	@Override
	public final void updateState(
		final Binary                 data    ,
		final ConstHashEnum<?>       instance,
		final PersistenceLoadHandler handler
	)
	{
		// validate to the best of possibilities (or should an immutable instance be updatedable from outside?)
		if(instance.size() != 0)
		{
			throw new IllegalStateException("Instance already contains data: " + instance);
		}

		XCollectionsInternals.setSize(instance, data.collectListObjectReferences(
			BINARY_OFFSET_ELEMENTS                                   ,
			handler                                                  ,
			item -> XCollectionsInternals.internalAdd(instance, item)
		));
		// note: hashDensity has already been set at creation time (shallow primitive value)
	}

	@Override
	public final void iterateInstanceReferences(final ConstHashEnum<?> instance, final PersistenceFunction iterator)
	{
		Persistence.iterateReferences(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}

}
