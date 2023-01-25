package one.microstream.persistence.binary.one.microstream.collections.lazy;

import java.lang.reflect.Method;
import java.util.Iterator;

import one.microstream.collections.lazy.LazyHashMap;
import one.microstream.collections.lazy.LazySegmentUnloader;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.internal.AbstractBinaryHandlerCustomCollection;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceLoadHandler;
import one.microstream.persistence.types.PersistenceReferenceLoader;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.reflect.XReflect;

public final class BinaryHandlerLazyHashMap extends AbstractBinaryHandlerCustomCollection<LazyHashMap<?,?>>
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
	OFFSET_MaxSegmentSize           = getFieldOffset(LazyHashMap.class, "maxSegmentSize"),
	OFFSET_ArrayList_size           = getFieldOffset(LazyHashMap.class, "size")          ,
	OFFSET_ArrayList_unloader       = getFieldOffset(LazyHashMap.class, "unloader"),
	OFFSET_Segment_min              = getFieldOffset(LazyHashMap.Segment.class, "min")   ,
	OFFSET_Segment_max              = getFieldOffset(LazyHashMap.Segment.class, "max")   ,
	OFFSET_Segment_size             = getFieldOffset(LazyHashMap.Segment.class, "segmentSize")
	;
	
	private static final Method
	METHOD_Segment_getData          = getDeclaredMethod(LazyHashMap.Segment.class, "getLazyData"),
	METHOD_Segment_getLazy          = getDeclaredMethod(LazyHashMap.Segment.class, "getLazy"),
	METHOD_Segement_cleanDirtyFlag  = getDeclaredMethod(LazyHashMap.Segment.class, "cleanModified"),
	METHOD_LazyHashMap_addSegment   = getDeclaredMethod(LazyHashMap.class, "addSegment", int.class, int.class, int.class, Object.class)
	;
	
	private static final long BINARY_MAP_FIELDS_LENGTH = Integer.BYTES + Integer.BYTES + Binary.referenceBinaryLength(1);
	private static final long BINARY_SEGMENT_LENGTH = Integer.BYTES + Integer.BYTES + Integer.BYTES + Binary.referenceBinaryLength(1);
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////
	
	@SuppressWarnings({"unchecked",  "rawtypes"})
	private static Class<LazyHashMap<?,?>> handledType()
	{
		// no idea how to get ".class" to work otherwise
		return (Class)LazyHashMap.class;
	}
	
	public static BinaryHandlerLazyHashMap New()
	{
		return new BinaryHandlerLazyHashMap();
	}
		
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	public BinaryHandlerLazyHashMap()
	{
		super(
			handledType(),
			CustomFields(
				CustomField(int.class,                 "maxSegmentSize"),
				CustomField(int.class,                 "size"          ),
				CustomField(LazySegmentUnloader.class, "unloader"      ),
				Complex("segments",
					CustomField(int.class , "min" ),
					CustomField(int.class , "max" ),
					CustomField(int.class , "size"),
					CustomField(LazyHashMap.LazyHashMapSegmentEntryList.class, "data")
				)
			)
		);
	}

	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
	
	@Override
	public LazyHashMap<?, ?> create(final Binary data, final PersistenceLoadHandler handler)
	{
		return new LazyHashMap<>();
	}

	@Override
	public void updateState(final Binary data, final LazyHashMap<?, ?> instance, final PersistenceLoadHandler handler)
	{
		instance.verifyLoader(handler.getObjectRetriever());
		instance.link(handler.getObjectRetriever());
		instance.clear();
		
		final int maxSegmentSize = data.read_int(BINARY_OFFSET_MAXSEGMENTSIZE);
		XMemory.set_int(instance, OFFSET_MaxSegmentSize, maxSegmentSize);
		
		final int size = data.read_int(BINARY_OFFSET_SIZE);
		XMemory.set_int(instance, OFFSET_ArrayList_size, size);
		
		final long unloderId = data.read_long(BINARY_OFFSET_UNLOADER);
		final Object unloader = handler.lookupObject(unloderId);
		XMemory.setObject(instance, OFFSET_ArrayList_unloader, unloader);
		
		//cast from long to int is OK because the list can't have more then max int elements.
		final int segmentCount = (int)data.getBinaryListElementCountValidating(BINARY_OFFSET_SEGMENTS, BINARY_SEGMENT_LENGTH);
		
		long elementsDataOffset = Binary.toBinaryListElementsOffset(BINARY_OFFSET_SEGMENTS);
		
		for(int i = 0; i < segmentCount; i++)
		{
			final int min = data.read_int(elementsDataOffset);
			elementsDataOffset += Integer.BYTES;

			final int max = data.read_int(elementsDataOffset);
			elementsDataOffset += Integer.BYTES;
			
			final int segmentSize = data.read_int(elementsDataOffset);
			elementsDataOffset += Integer.BYTES;
			
			final long refId = data.read_long(elementsDataOffset);
			elementsDataOffset += Binary.referenceBinaryLength(1);
			
			final Object ref = handler.lookupObject(refId);
			XReflect.invoke(METHOD_LazyHashMap_addSegment, instance, min, max, segmentSize, ref);
		}
	}

	@Override
	public void store(final Binary data, final LazyHashMap<?, ?> instance, final long objectId, final PersistenceStoreHandler<Binary> handler)
	{
		instance.verifyLoader(handler.getObjectRetriever());
		instance.link(handler.getObjectRetriever());
		
		final long segmentListContentLength = BINARY_SEGMENT_LENGTH * instance.getSegmentCount();
		final long segmentListTotalLength = Binary.toBinaryListTotalByteLength(segmentListContentLength);
		final long totalLength = BINARY_MAP_FIELDS_LENGTH + segmentListTotalLength;
		
		data.storeEntityHeader(totalLength, this.typeId(), objectId);
		data.store_int(BINARY_OFFSET_MAXSEGMENTSIZE, instance.getMaxSegmentSize());
		data.store_int(BINARY_OFFSET_SIZE, instance.size());
		
		data.store_long(BINARY_OFFSET_UNLOADER, handler.apply(XMemory.getObject(instance, OFFSET_ArrayList_unloader)));
		
		data.storeListHeader(BINARY_OFFSET_SEGMENTS, segmentListContentLength, instance.getSegmentCount());
		
		long elementsDataOffset =  Binary.toBinaryListElementsOffset(BINARY_OFFSET_SEGMENTS);
		final Iterator<? extends LazyHashMap<?, ?>.Segment<?>> iterator = instance.segments().iterator();
		while(iterator.hasNext())
		{
			final LazyHashMap<?, ?>.Segment<?> segment = iterator.next();
						
			data.store_int(elementsDataOffset, XMemory.get_int(segment, OFFSET_Segment_min));
			elementsDataOffset += Integer.BYTES;
			
			data.store_int(elementsDataOffset, XMemory.get_int(segment, OFFSET_Segment_max));
			elementsDataOffset += Integer.BYTES;

			data.store_int(elementsDataOffset, XMemory.get_int(segment, OFFSET_Segment_size));
			elementsDataOffset += Integer.BYTES;
						
			data.store_long(elementsDataOffset, handler.apply(XReflect.invoke(METHOD_Segment_getLazy, segment)));
			elementsDataOffset += Binary.referenceBinaryLength(1);
									
			if(segment.isLoaded() && segment.isModified())
			{
				handler.applyEager(XReflect.invoke(METHOD_Segment_getData, segment));
				XReflect.invoke(METHOD_Segement_cleanDirtyFlag, segment);
			}
		}
	}
	
	@Override
	public void iterateInstanceReferences(final LazyHashMap<?, ?> instance, final PersistenceFunction iterator)
	{
		super.iterateInstanceReferences(instance, iterator);
	}

	@Override
	public void iterateLoadableReferences(final Binary data, final PersistenceReferenceLoader iterator)
	{
		Binary.iterateListStructureCompositeElements(data, BINARY_OFFSET_SEGMENTS, 12, 1, 0, iterator);
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
