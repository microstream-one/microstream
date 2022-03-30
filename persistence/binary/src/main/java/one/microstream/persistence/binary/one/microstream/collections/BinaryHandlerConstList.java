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
import one.microstream.collections.ConstList;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerConstList
extends AbstractBinaryHandlerCustomCollection<ConstList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	private static final long BINARY_OFFSET_LIST = 0; // binary form is 100% just a simple list, so offset 0



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<ConstList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)ConstList.class;
	}
	
	public static BinaryHandlerConstList New()
	{
		return new BinaryHandlerConstList();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerConstList()
	{
		// binary layout definition
		super(
			handledType(),
			SimpleArrayFields()
		);
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final ConstList<?>                    instance,
		final long                            objectId,
		final PersistenceStoreHandler<Binary> handler
	)
	{
		data.storeReferences(
			this.typeId()                          ,
			objectId                               ,
			0                                      ,
			handler                                ,
			XCollectionsInternals.getData(instance)
		);
	}

	@Override
	public final ConstList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return ConstList.New(X.checkArrayRange(data.getListElementCountReferences(0)));
	}

	@Override
	public final void updateState(final Binary data, final ConstList<?> instance, final PersistenceLoadHandler handler)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);

		// Length must be checked for consistency reasons. No clear required.
		data.validateArrayLength(arrayInstance, BINARY_OFFSET_LIST);
		data.collectElementsIntoArray(BINARY_OFFSET_LIST, handler, arrayInstance);
	}

	@Override
	public final void iterateInstanceReferences(final ConstList<?> instance, final PersistenceFunction iterator)
	{
		final Object[] arrayInstance = XCollectionsInternals.getData(instance);
		Persistence.iterateReferences(iterator, arrayInstance, 0, arrayInstance.length);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		data.iterateListElementReferences(BINARY_OFFSET_LIST, iterator);
	}

}
