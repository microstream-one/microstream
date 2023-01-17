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

import one.microstream.X;
import one.microstream.collections.lazy.LazyHashMap;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomIterable;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class BinaryHandlerLazyHashMapSegmentEntryList extends AbstractBinaryHandlerCustomIterable<LazyHashMap.LazyHashMapSegmentEntryList<?,?>>
{
	
	static final long ENTRY_LENGHT = Binary.referenceBinaryLength(2) + Integer.BYTES;
	
	static final long BINARY_OFFSET_ELEMENTS = 0;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LazyHashMap.LazyHashMapSegmentEntryList<?,?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LazyHashMap.LazyHashMapSegmentEntryList.class;
	}
	
	public static BinaryHandlerLazyHashMapSegmentEntryList New()
	{
		return new BinaryHandlerLazyHashMapSegmentEntryList();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryHandlerLazyHashMapSegmentEntryList()
	{
		super(
			handledType(),
			CustomFields(
				Complex("segments",
					CustomField(int.class   , "hash" ),
					CustomField(Object.class, "key"  ),
					CustomField(Object.class, "value")
				)
			)
		);
	}
		

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		Binary.iterateListStructureCompositeElements(data, BINARY_OFFSET_ELEMENTS, Integer.BYTES, 2, 0, iterator);
	}
	
	@Override
	public void complete(final Binary data, final LazyHashMap.LazyHashMapSegmentEntryList<?, ?> instance, final PersistenceLoadHandler handler)
	{
		final int elementCount = this.getElementCount(data);
		
		long offset = Binary.toBinaryListElementsOffset(BINARY_OFFSET_ELEMENTS);
		
		for(int i = 0; i < elementCount; i++) {
			final int hash = data.read_int(offset);
			offset += Integer.BYTES;
			final long keyId = data.read_long(offset);
			offset += Binary.referenceBinaryLength(1);
			final long valueId = data.read_long(offset);
			offset += Binary.referenceBinaryLength(1);
			
			final Object key = handler.lookupObject(keyId);
			final Object value = handler.lookupObject(valueId);
			instance.addEntry(hash, key, value);
		}
	}
	
	@Override
	public void updateState(final Binary data, final LazyHashMap.LazyHashMapSegmentEntryList<?, ?> instance, final PersistenceLoadHandler handler)
	{
		//noop
	}

	@Override
	public void store(final Binary data, final LazyHashMap.LazyHashMapSegmentEntryList<?, ?> instance, final long objectId,
			final PersistenceStoreHandler<Binary> handler) {
	
		final long elementsBinaryRange = instance.size() * ENTRY_LENGHT;
		final long elementsCount = instance.size();
		
		data.storeEntityHeader(
			BINARY_OFFSET_ELEMENTS + Binary.toBinaryListTotalByteLength(ENTRY_LENGHT * elementsCount),
			this.typeId(),
			objectId
		);
		
		data.storeListHeader(BINARY_OFFSET_ELEMENTS, elementsBinaryRange, elementsCount);
		
		final long referenceLength = Binary.referenceBinaryLength(1);
		long offset = Binary.toBinaryListElementsOffset(BINARY_OFFSET_ELEMENTS);
		
		for (final LazyHashMap.Entry<?, ?> entry : instance) {
			data.store_int(offset, entry.getHash());
			offset += Integer.BYTES;
			data.store_long(offset, handler.apply(entry.getKey()));
			offset += referenceLength;
			data.store_long(offset, handler.apply(entry.getValue()));
			offset += referenceLength;
		}
	}

	@Override
	public LazyHashMap.LazyHashMapSegmentEntryList<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		final int elementCount = this.getElementCount(data);
		return new LazyHashMap.LazyHashMapSegmentEntryList<>(elementCount);
	}

	private int getElementCount(final Binary data)
	{
		return X.checkArrayRange(data.getBinaryListElementCountValidating(BINARY_OFFSET_ELEMENTS, BinaryHandlerLazyHashMapSegmentEntryList.ENTRY_LENGHT));
	}
	
	
}
