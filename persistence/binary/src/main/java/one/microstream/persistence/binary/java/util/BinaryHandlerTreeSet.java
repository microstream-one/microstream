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

import java.util.Comparator;
import java.util.TreeSet;

import one.microstream.X;
import one.microstream.collections.old.OldCollections;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.Persistence;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;


public final class BinaryHandlerTreeSet extends AbstractBinaryHandlerCustomCollection<TreeSet<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////

	static final long BINARY_OFFSET_COMPARATOR =                                                      0;
	static final long BINARY_OFFSET_ELEMENTS   = BINARY_OFFSET_COMPARATOR + Binary.objectIdByteLength();



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Class<TreeSet<?>> handledType()
	{
		return (Class)TreeSet.class; // no idea how to get ".class" to work otherwise
	}
	
	@SuppressWarnings("unchecked")
	private static <E> Comparator<? super E> getComparator(
		final Binary                 data   ,
		final PersistenceLoadHandler handler
	)
	{
		return (Comparator<? super E>)handler.lookupObject(data.read_long(BINARY_OFFSET_COMPARATOR));
	}

	static final int getElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getListElementCountReferences(BINARY_OFFSET_ELEMENTS));
	}
	
	public static BinaryHandlerTreeSet New()
	{
		return new BinaryHandlerTreeSet();
	}



	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	BinaryHandlerTreeSet()
	{
		super(
			handledType(),
			SimpleArrayFields(
				CustomField(Comparator.class, "comparator")
			)
		);
		
	}



	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public final void store(
		final Binary                          data    ,
		final TreeSet<?>                      instance,
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
		
		data.store_long(
			BINARY_OFFSET_COMPARATOR,
			handler.apply(instance.comparator())
		);
	}
	
	@Override
	public final TreeSet<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new TreeSet<>(
			getComparator(data, handler)
		);
	}

	@Override
	public final void updateState(final Binary data, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		instance.clear();
		
		/*
		 * Tree collections don't use hashing, but their comparing logic still uses the elements' state,
		 * which might not yet be available when this method is called. Hence the detour to #complete.
		 */
		final Object[] elementsHelper = new Object[getElementCount(data)];
		data.collectElementsIntoArray(BINARY_OFFSET_ELEMENTS, handler, elementsHelper);
		data.registerHelper(instance, elementsHelper);
	}
	
	@Override
	public final void complete(final Binary data, final TreeSet<?> instance, final PersistenceLoadHandler handler)
	{
		OldCollections.populateCollectionFromHelperArray(instance, data.getHelper(instance));
	}

	@Override
	public final void iterateInstanceReferences(final TreeSet<?> instance, final PersistenceFunction iterator)
	{
		iterator.apply(instance.comparator());
		Persistence.iterateReferencesIterable(iterator, instance);
	}

	@Override
	public final void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_COMPARATOR));
		data.iterateListElementReferences(BINARY_OFFSET_ELEMENTS, iterator);
	}
	
}
