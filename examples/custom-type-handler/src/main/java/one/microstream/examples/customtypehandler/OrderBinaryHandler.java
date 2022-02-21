package one.microstream.examples.customtypehandler;

/*-
 * #%L
 * microstream-examples-custom-type-handler
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import one.microstream.X;
import one.microstream.examples.customtypehandler.Order.Item;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustom;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;

public class OrderBinaryHandler extends AbstractBinaryHandlerCustom<Order>
{
	static final int
		BINARY_OFFSET_timestamp          = 0                                                             ,
		BINARY_OFFSET_customerNumber     = BINARY_OFFSET_timestamp          + XMemory.byteSize_long()    ,
		BINARY_OFFSET_ITEMS              = BINARY_OFFSET_customerNumber     + Binary.objectIdByteLength()
	;
	static final int
		BINARY_ITEM_OFFSET_productNumber = 0                                                             ,
		BINARY_ITEM_OFFSET_amount        = BINARY_ITEM_OFFSET_productNumber + XMemory.byteSize_int()     ,
		BINARY_ITEM_LENGTH               = BINARY_ITEM_OFFSET_amount        + XMemory.byteSize_int()
	;
	
	static int getBuildItemElementCount(final Binary bytes)
	{
		return X.checkArrayRange(bytes.getListElementCount(BINARY_OFFSET_ITEMS, BINARY_ITEM_LENGTH));
	}
	
	
	public OrderBinaryHandler()
	{
		super(
			Order.class,
			CustomFields(
				CustomField(long  .class, "timestamp"     ),
				CustomField(String.class, "customerNumber"),
				Complex("items",
					CustomField(int.class, "productNumber"),
					CustomField(int.class, "amount"       )
				)
			)
		);
	}
	
	@Override
	public final void store(
		final Binary                          bytes   ,
		final Order                           instance,
		final long                            oid     ,
		final PersistenceStoreHandler<Binary> linker
	)
	{
		final Item[] items = instance.getItems();
		
		// calculate length and write header
		final long listElementCount          = items.length;
		final long listContentBinaryLength   = listElementCount * BINARY_ITEM_LENGTH;
		final long entityContentBinaryLength = BINARY_OFFSET_ITEMS
			+ Binary.toBinaryListTotalByteLength(listContentBinaryLength)
		;
		bytes.storeEntityHeader(entityContentBinaryLength, this.typeId(), oid);
		
		// write order values
		bytes.store_long(BINARY_OFFSET_timestamp     , instance.getTimestamp().getTime()         );
		bytes.store_long(BINARY_OFFSET_customerNumber, linker.apply(instance.getCustomerNumber()));
		
		// set element list header
		bytes.storeListHeader(
			BINARY_OFFSET_ITEMS    ,
			listContentBinaryLength,
			listElementCount
		);
		
		// write items
		long offset = Binary.toBinaryListTotalByteLength(BINARY_OFFSET_ITEMS);
		for(final Item item : items)
		{
			bytes.store_int(offset + BINARY_ITEM_OFFSET_productNumber, item.getProductNumber());
			bytes.store_int(offset + BINARY_ITEM_OFFSET_amount       , item.getAmount()       );
			
			// add item length to offset
			offset += BINARY_ITEM_LENGTH;
		}
	}
	
	@Override
	public Order create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new Order();
	}
	
	@Override
	public final void updateState(
		final Binary                      bytes   ,
		final Order                       instance,
		final PersistenceLoadHandler      builder
	)
	{
		final long elementCount    = getBuildItemElementCount(bytes);
		final long addressBound    = elementCount * BINARY_ITEM_LENGTH;
		
		instance.setTimestamp(
			new Date(bytes.read_long(BINARY_OFFSET_timestamp))
		);
		instance.setCustomerNumber(
			(String)builder.lookupObject(bytes.read_long(BINARY_OFFSET_customerNumber))
		);
		
		final List<Item> items = new ArrayList<>();
		
		for(long adr = Binary.toBinaryListTotalByteLength(BINARY_OFFSET_ITEMS); adr < addressBound; adr += BINARY_ITEM_LENGTH)
		{
			final Item item = new Item(
				bytes.read_int(adr + BINARY_ITEM_OFFSET_productNumber),
				bytes.read_int(adr + BINARY_ITEM_OFFSET_amount       )
			);
			items.add(item);
		}
		
		instance.setItems(items.toArray(new Item[items.size()]));
	}
	
	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		// no-op
	}

	@Override
	public boolean hasPersistedReferences()
	{
		return false;
	}

	@Override
	public boolean hasVaryingPersistedLengthInstances()
	{
		return false;
	}
	
}
