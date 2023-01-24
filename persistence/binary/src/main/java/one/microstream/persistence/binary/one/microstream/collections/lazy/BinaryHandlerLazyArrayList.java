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

import java.lang.reflect.Method;
import java.util.Iterator;

import one.microstream.collections.lazy.LazyArrayList;
import one.microstream.collections.lazy.LazySegment;
import one.microstream.collections.lazy.LazySegmentUnloader;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reference.ControlledLazyReference;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerLazyArrayList extends AbstractBinaryHandlerCustomCollection<LazyArrayList<?>>
{
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	static final long
		BINARY_OFFSET_MAXSEGMENTSIZE    =                                            0,
		BINARY_OFFSET_SIZE              = BINARY_OFFSET_MAXSEGMENTSIZE + Integer.BYTES,
		BINARY_OFFSET_UNLOADER          = BINARY_OFFSET_SIZE           + Integer.BYTES,
		BINARY_OFFSET_SEGMENTS          = BINARY_OFFSET_UNLOADER       + Binary.referenceBinaryLength(1)
	;
	
	private static final long
		OFFSET_MaxSegmentSize           = getFieldOffset(LazyArrayList.class, "maxSegmentSize"),
		OFFSET_ArrayList_size           = getFieldOffset(LazyArrayList.class, "size"),
		OFFSET_ArrayList_unloader       = getFieldOffset(LazyArrayList.class, "unloader"),
		OFFSET_Segment_offset           = getFieldOffset(LazyArrayList.Segment.class, "offset"),
		OFFSET_Segment_size             = getFieldOffset(LazyArrayList.Segment.class, "segmentSize")
	;
	
	private static final Method
		METHOD_Segment_getData          = getDeclaredMethod(LazyArrayList.Segment.class, "getLazyData"),
		METHOD_Segment_getLazy          = getDeclaredMethod(LazyArrayList.Segment.class, "getLazy"),
		METHOD_Segement_cleanDirtyFlag  = getDeclaredMethod(LazyArrayList.Segment.class, "cleanModified")                        ,
		METHOD_LazyArrayList_addSegment = getDeclaredMethod(LazyArrayList.class, "addSegment", int.class, int.class, Object.class)
	;
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LazyArrayList<?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LazyArrayList.class;
	}
	
	public static BinaryHandlerLazyArrayList New()
	{
		return new BinaryHandlerLazyArrayList();
	}
	

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryHandlerLazyArrayList()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(int.class,                 "maxSegmentSize"),
				CustomField(int.class,                 "size"          ),
				CustomField(LazySegmentUnloader.class, "unloader"      ),
				Complex("segments",
					CustomField(int.class,                   "offset"),
					CustomField(int.class,                   "size"  ),
					CustomField(ControlledLazyReference.class, "data"  )
				)
			)
		);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public void store(final Binary data, final LazyArrayList<?> instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		final long fieldsLength = Integer.BYTES + Integer.BYTES + Binary.referenceBinaryLength(1);
		final long segmentLength = Integer.BYTES + Integer.BYTES + Binary.referenceBinaryLength(1);
		final long segmentListContentLength = segmentLength * instance.getSegmentCount();
		final long segmentListTotalLength = Binary.toBinaryListTotalByteLength(segmentListContentLength);
		final long totalLength = fieldsLength + segmentListTotalLength;
		
		data.storeEntityHeader(totalLength, this.typeId(), objectId);
		data.store_int(BINARY_OFFSET_MAXSEGMENTSIZE, instance.getMaxSegmentSize());
		data.store_int(BINARY_OFFSET_SIZE, instance.size());
	    
		data.store_long(BINARY_OFFSET_UNLOADER, handler.apply(XMemory.getObject(instance, OFFSET_ArrayList_unloader)));
			
		data.storeListHeader(BINARY_OFFSET_SEGMENTS, segmentListContentLength, instance.getSegmentCount());
		
		long elementsDataOffset =  Binary.toBinaryListElementsOffset(BINARY_OFFSET_SEGMENTS);
		final Iterator<? extends LazyArrayList<?>.Segment> iterator = instance.segments().iterator();
		while(iterator.hasNext()) {
			final LazySegment<?> segment = iterator.next();
						
			data.store_int(elementsDataOffset, XMemory.get_int(segment, OFFSET_Segment_offset));
			elementsDataOffset += Integer.BYTES;

			data.store_int(elementsDataOffset, XMemory.get_int(segment, OFFSET_Segment_size));
			elementsDataOffset += Integer.BYTES;
						
			data.store_long(elementsDataOffset, handler.apply(XReflect.invoke(METHOD_Segment_getLazy, segment)));
			elementsDataOffset += Binary.referenceBinaryLength(1);
									
			if(segment.isLoaded() && segment.isModified()) {
				handler.applyEager(XReflect.invoke(METHOD_Segment_getData, segment));
				XReflect.invoke(METHOD_Segement_cleanDirtyFlag, segment);
			}
		}
	}
	
	@Override
	public LazyArrayList<?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new LazyArrayList<>();
	}
	
	@Override
	public void updateState(final Binary data, final LazyArrayList<?> instance, final PersistenceLoadHandler handler)
	{
		final int maxSegmentSize = data.read_int(BINARY_OFFSET_MAXSEGMENTSIZE);
		XMemory.set_int(instance, OFFSET_MaxSegmentSize, maxSegmentSize);
		
		final int size = data.read_int(BINARY_OFFSET_SIZE);
		XMemory.set_int(instance, OFFSET_ArrayList_size, size);
		
		final long unloderId = data.read_long(BINARY_OFFSET_UNLOADER);
		final Object unloader = handler.lookupObject(unloderId);
		XMemory.setObject(instance, OFFSET_ArrayList_unloader, unloader);
		
		//cast from long to int is OK because the list can't have more then max int elements.
		final int segmentCount = (int)data.getBinaryListElementCountValidating(BINARY_OFFSET_SEGMENTS, 16);
		
		long elementsDataOffset = Binary.toBinaryListElementsOffset(BINARY_OFFSET_SEGMENTS);
		
		for(int i = 0; i < segmentCount; i++) {
			final int segmentOffset = data.read_int(elementsDataOffset);
			elementsDataOffset += Integer.BYTES;
			final int segmentSize = data.read_int(elementsDataOffset);
			elementsDataOffset += Integer.BYTES;
			final long refId = data.read_long(elementsDataOffset);
			elementsDataOffset += Binary.referenceBinaryLength(1);
			
			final Object ref = handler.lookupObject(refId);
			XReflect.invoke(METHOD_LazyArrayList_addSegment, instance, segmentOffset, segmentSize, ref);
		}
	}
	
	@Override
	public void iterateInstanceReferences(final LazyArrayList<?> instance, final PersistenceFunction iterator)
	{
		super.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		Binary.iterateListStructureCompositeElements(data, BINARY_OFFSET_SEGMENTS, 8, 1, 0, iterator);
		iterator.acceptObjectId(data.read_long(BINARY_OFFSET_UNLOADER));
	}
		
	private static final long getFieldOffset(final Class<?> type, final String declaredFieldName)
	{
		return XMemory.objectFieldOffset(XReflect.getAnyField(type, declaredFieldName));
	}
	
	private static final Method getDeclaredMethod(final Class<?> c, final String name, final Class<?>... parameterTypes)
	{
		return XReflect.setAccessible(XReflect.getDeclaredMethod(c, name, parameterTypes));
	}
		
}
