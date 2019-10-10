package one.microstream.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import one.microstream.X;
import one.microstream.chars.XChars;
import one.microstream.collections.EqHashEnum;
import one.microstream.collections.types.XAddingMap;
import one.microstream.collections.types.XGettingTable;
import one.microstream.math.XMath;
import one.microstream.memory.PlatformInternals;
import one.microstream.memory.XMemory;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceExceptionInvalidList;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceExceptionInvalidListElements;
import one.microstream.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import one.microstream.persistence.exceptions.PersistenceException;
import one.microstream.persistence.types.PersistenceFunction;
import one.microstream.persistence.types.PersistenceObjectIdAcceptor;
import one.microstream.persistence.types.PersistenceObjectIdResolver;
import one.microstream.persistence.types.PersistenceStoreHandler;
import one.microstream.typing.KeyValue;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunk
{
	/* (29.01.2019 TM)TODO: "Binary" ugliness consolidation
	 * Currently, the "Binary" class is an unclean "one size fits fall" implementation, providing API
	 * for both storing and loading, forcing two distinct subclasses for each concern to dummy-implement
	 * some methods and throw UnsupportedOperation exceptions.
	 * The initial reason for this was that "Binary" was just a typing helper type to make the compiler check if
	 * interface implementations (custom handlers etc) are compatible to each other.
	 * However, it could very well be possible to specify the generics much more precisely, e.g. as
	 * <B extends Binary>, maybe even with two arguments, like <L extends BinaryLoadItem> and <S extends BinaryStoreMedium>
	 * For now, the "Binary" API-ugliness is kept, but it could very well pay off to restructure and clean it up.
	 */
	
	///////////////////////////////////////////////////////////////////////////
	// constants //
	//////////////
	
	private static final int
		LENGTH_LEN  = Long.BYTES,
		LENGTH_OID  = Long.BYTES,
		LENGTH_TID  = Long.BYTES
	;
	private static final long
		OFFSET_LEN = 0L                     ,
		OFFSET_TID = OFFSET_LEN + LENGTH_LEN,
		OFFSET_OID = OFFSET_TID + LENGTH_TID,
		OFFSET_DAT = OFFSET_OID + LENGTH_OID
	;
	
	// header (currently) constists of only LEN, TID, OID. The extra constant has sementical reasons.
	private static final int LENGTH_ENTITY_HEADER = (int)OFFSET_DAT;

	/* (29.01.2019 TM)TODO: test and comment bit shifting multiplication performance
	 * test and comment if this really makes a difference in performance.
	 * The redundant length is ugly.
	 */
	/**
	 * "<< 3" is a performance optimization for "* 8".
	 */
	private static final int LONG_BYTE_LENGTH_BITSHIFT_COUNT = 3;
	
	private static final int
		LIST_OFFSET_BYTE_LENGTH   = 0                                     ,
		LIST_OFFSET_ELEMENT_COUNT = LIST_OFFSET_BYTE_LENGTH   + Long.BYTES,
		LIST_OFFSET_ELEMENTS      = LIST_OFFSET_ELEMENT_COUNT + Long.BYTES,
		LIST_HEADER_LENGTH        = LIST_OFFSET_ELEMENTS
	;
	
	/* sized array binary layout:
	 * [entity header][8 byte array length][list of elements without the arrays' trailing nulls]
	 */
	private static final long
		SIZED_ARRAY_OFFSET_LENGTH   = 0L                       , // length is the first (and only) header value
		SIZED_ARRAY_LENGTH_HEADER   = Long.BYTES               , // the header only consists of the length
		SIZED_ARRAY_OFFSET_ELEMENTS = SIZED_ARRAY_LENGTH_HEADER  // the element list begins after the header
	;
	
	/**
	 * Obviously 2 references: the key and the value.
	 */
	private static final long KEY_VALUE_REFERENCE_COUNT = 2;
	
	private static final long KEY_VALUE_BINARY_LENGTH = KEY_VALUE_REFERENCE_COUNT * LENGTH_OID;
	
	// special crazy sh*t negative offsets
	private static final long
		CONTENT_ADDRESS_NEGATIVE_OFFSET_TID = OFFSET_TID - LENGTH_ENTITY_HEADER,
		CONTENT_ADDRESS_NEGATIVE_OFFSET_OID = OFFSET_OID - LENGTH_ENTITY_HEADER
	;
	
	static
	{
		/* (02.07.2019 TM)NOTE: the binary persistence layer
		 * requires the full usability of the JDK-internal accessing functionality
		 * like calling the cleaner or accessing the direct byte buffer address.
		 * Until they fix their sh*t to provide proper solutions for that, makeshift
		 * solutions like this are required.
		 */
		PlatformInternals.guaranteeUsability();
	}
	
	
	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static long keyValueReferenceCount(final long elementCount)
	{
		return Binary.KEY_VALUE_REFERENCE_COUNT * elementCount;
	}
	
	public static long keyValueBinaryLength()
	{
		return Binary.KEY_VALUE_BINARY_LENGTH;
	}
	
	public static long binaryListMinimumLength()
	{
		return Binary.LIST_HEADER_LENGTH;
	}

	public static long binaryListMaximumLength()
	{
		return Long.MAX_VALUE;
	}

	public static long toBinaryListTotalByteLength(final long binaryListElementsByteLength)
	{
		return binaryListElementsByteLength + Binary.LIST_HEADER_LENGTH;
	}
	
	public static long toBinaryListContentByteLength(final long binaryListTotalByteLength)
	{
		return binaryListTotalByteLength - Binary.LIST_HEADER_LENGTH;
	}
	
	/**
	 * @return the length in bytes of a peristent item's length field (8 bytes).
	 */
	public static int lengthLength()
	{
		return Binary.LENGTH_LEN;
	}

	public static boolean isValidGapLength(final long gapLength)
	{
		if(true)
		{
			System.out.println();
		}
		// gap total length cannot indicate less then its own length (length of the length field, 1 long)
		return gapLength >= Binary.LENGTH_LEN;
	}

	public static boolean isValidEntityLength(final long entityLength)
	{
		return entityLength >= Binary.LENGTH_ENTITY_HEADER;
	}

	public static int entityHeaderLength()
	{
		return Binary.LENGTH_ENTITY_HEADER;
	}

	public static long entityTotalLength(final long entityContentLength)
	{
		// the total length is the content length plus the length of the header (containing length, Tid, Oid)
		return entityContentLength + Binary.LENGTH_ENTITY_HEADER;
	}
	
	public static long entityContentLength(final long entityTotalLength)
	{
		// the content length is the total length minus the length of the header (containing length, Tid, Oid)
		return entityTotalLength - Binary.LENGTH_ENTITY_HEADER;
	}

	public static long toEntityContentOffset(final long entityOffset)
	{
		// note that this method can be used for absolute addresses, too.
		return entityOffset + Binary.LENGTH_ENTITY_HEADER;
	}

	public static final long toBinaryListByteLengthOffset(final long binaryListOffset)
	{
		return binaryListOffset + LIST_OFFSET_BYTE_LENGTH;
	}
	
	public static final long toBinaryListElementCountOffset(final long binaryListOffset)
	{
		return binaryListOffset + LIST_OFFSET_ELEMENT_COUNT;
	}
	
	public static long toBinaryListElementsOffset(final long binaryListOffset)
	{
		// note that this method can be used for absolute addresses, too.
		return binaryListOffset + Binary.LIST_OFFSET_ELEMENTS;
	}
	
	public static int objectIdByteLength()
	{
		return Binary.LENGTH_OID;
	}
	
	public static long referenceBinaryLength(final long referenceCount)
	{
		return referenceCount << Binary.LONG_BYTE_LENGTH_BITSHIFT_COUNT; // reference (ID) binary length is 8
	}
	
	public static long calculateReferenceListTotalBinaryLength(final long count)
	{
		return toBinaryListTotalByteLength(referenceBinaryLength(count)); // 8 bytes per reference
	}
	
	public static long calculateStringListContentBinaryLength(final String[] strings)
	{
		// precise size for each string (char list header plus 2 byte per char)
		long listContentBinaryLength = 0;
		for(final String string : strings)
		{
			listContentBinaryLength += calculateBinaryLengthChars(string.length());
		}

		return listContentBinaryLength;
	}

	public static long calculateBinaryLengthChars(final long count)
	{
		return toBinaryListTotalByteLength(count << 1);  // header plus 2 bytes per char
	}
		
	
	
	
	// (04.10.2019 TM)FIXME: priv#111: clean up temporarily moved internal methods
	
	//////////////////////////////////////////////////////////////////////////////////////
	// static methods using absolut an memory address that should actually not be here //
	////////////////////////////////////////////////////////////////////////////////////
		
	public static final long getEntityLengthRawValue(final long entityAddress)
	{
		return XMemory.get_long(entityAddress + OFFSET_LEN);
	}
		
	public static final long getEntityTypeIdRawValue(final long entityAddress)
	{
		return XMemory.get_long(entityAddress + OFFSET_TID);
	}

	public static final long getEntityObjectIdRawValue(final long entityAddress)
	{
		return XMemory.get_long(entityAddress + OFFSET_OID);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// implementation internal static methods //
	///////////////////////////////////////////
	
	static final long entityAddressFromContentAddress(final long entityContentAddress)
	{
		return entityContentAddress - LENGTH_ENTITY_HEADER;
	}
	
	/**
	 * "Raw" means without byte order transformation. This must be done in the calling context.
	 * 
	 * @param entityAddress
	 * @param entityTotalLength
	 * @param entityTypeId
	 * @param entityObjectId
	 * 
	 */
	static final void setEntityHeaderRawValuesToAddress(
		final long entityAddress    ,
		final long entityTotalLength,
		final long entityTypeId     ,
		final long entityObjectId
	)
	{
		XMemory.set_long(entityAddress + OFFSET_LEN, entityTotalLength);
		XMemory.set_long(entityAddress + OFFSET_TID, entityTypeId     );
		XMemory.set_long(entityAddress + OFFSET_OID, entityObjectId   );
	}

	
	

	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/**
	 * Depending on the deriving class, this is either a single entity's address for reading data
	 * or the beginning of a store chunk for storing multiple entities in a row (for efficiency reasons).
	 */
	long address;

	private HelperEntry helperEntry;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	Binary()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// public methods //
	///////////////////
		
	@Override
	public abstract ByteBuffer[] buffers();
	
	public boolean isSwitchedByteOrder()
	{
		return false;
	}

	public final long getEntityLength()
	{
		// (06.09.2014)TODO: test and comment if " + 0" gets eliminated by JIT
		return this.get_longFromAddress(this.loadItemEntityAddress() + OFFSET_LEN);
	}

	public final long getEntityTypeId()
	{
		return this.get_longFromAddress(this.loadItemEntityAddress() + OFFSET_TID);
	}

	public final long getEntityObjectId()
	{
		return this.get_longFromAddress(this.loadItemEntityAddress() + OFFSET_OID);
	}

	public final long getBuildItemContentLength()
	{
		return this.get_longFromAddress(this.loadItemEntityContentAddress() - LENGTH_ENTITY_HEADER)	- LENGTH_ENTITY_HEADER;
	}

	public final long getBuildItemTypeId()
	{
		return this.get_longFromAddress(this.loadItemEntityContentAddress() + CONTENT_ADDRESS_NEGATIVE_OFFSET_TID);
	}

	public final long getBuildItemObjectId()
	{
		return this.get_longFromAddress(this.loadItemEntityContentAddress() + CONTENT_ADDRESS_NEGATIVE_OFFSET_OID);
	}
			
	public abstract void storeEntityHeader(
		long entityContentLength,
		long entityTypeId       ,
		long entityObjectId
	);
	
	public final long getListElementCountKeyValue(final long listStartOffset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.getBinaryListElementCountValidating(
			listStartOffset,
			keyValueBinaryLength()
		);
	}
	
	public final byte read_byte(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_byteFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final boolean read_boolean(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_booleanFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final short read_short(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_shortfromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final char read_char(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_charFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final int read_int(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_intFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final float read_float(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_floatFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final long read_long(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_longFromAddress(this.loadItemEntityContentAddress() + offset);
	}

	public final double read_double(final long offset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		return this.get_doubleFromAddress(this.loadItemEntityContentAddress() + offset);
	}
					
	public abstract Binary channelChunk(int channelIndex);
	
	public abstract int channelCount();
	
	public abstract void iterateChannelChunks(Consumer<? super Binary> logic);
	
	public abstract void iterateEntityData(BinaryEntityDataReader reader);
		
	public final void storeKeyValuesAsEntries(
		final long                               typeId      ,
		final long                               objectId    ,
		final long                               headerOffset,
		final Iterable<? extends KeyValue<?, ?>> keyValues   ,
		final long                               size        ,
		final PersistenceFunction                persister
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		// store entity header including the complete content size (headerOffset + entries)
		this.storeEntityHeader(
			headerOffset + calculateReferenceListTotalBinaryLength(keyValueReferenceCount(size)),
			typeId,
			objectId
		);

		// store entries
		this.storeKeyValuesAsEntries(headerOffset, persister, keyValues, size);
	}
	
	// this "<K, V>" is not superfluous! It prevents the dreaded "capture#3453 of ?" compiler errors.
	public final <K, V> void storeMapEntrySet(
		final long                 typeId      ,
		final long                 objectId    ,
		final long                 headerOffset,
		final Set<Map.Entry<K, V>> entrySet    ,
		final PersistenceFunction  persister
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		final int size = entrySet.size();
		
		// store entity header including the complete content size (headerOffset + entries)
		this.storeEntityHeader(
			headerOffset + calculateReferenceListTotalBinaryLength(keyValueReferenceCount(size)),
			typeId,
			objectId
		);

		final long referenceLength     = referenceBinaryLength(1);
		final long entryLength         = referenceBinaryLength(2); // two references per entry
		final long elementsBinaryRange = size * entryLength;
		final long elementsDataAddress = this.address + headerOffset + LIST_OFFSET_ELEMENTS;
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;
		
		this.storeListHeader(headerOffset, elementsBinaryRange, size);

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		final Iterator<? extends Map.Entry<?, ?>> iterator = entrySet.iterator();
		long a = elementsDataAddress;
		while(a < elementsBinaryBound && iterator.hasNext())
		{
			final Map.Entry<?, ?> element = iterator.next();
			this.set_longToAddress(a                  , persister.apply(element.getKey())  );
			this.set_longToAddress(a + referenceLength, persister.apply(element.getValue()));
			a += entryLength; // advance index for both in one step
		}

		validatePostIterationState(a, elementsBinaryBound, iterator, size, entryLength);
	}
	
	public final void storeSizedArray(
		final long                    tid         ,
		final long                    oid         ,
		final long                    headerOffset,
		final Object[]                array       ,
		final int                     size        ,
		final PersistenceStoreHandler persister
	)
	{
		this.storeSizedArray(tid, oid, headerOffset, array, 0, size, persister);
	}
	
	public final void storeSizedArray(
		final long                    tid         ,
		final long                    oid         ,
		final long                    headerOffset,
		final Object[]                array       ,
		final int                     offset      ,
		final int                     size        ,
		final PersistenceStoreHandler persister
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		// store entity header including the complete content size (8 + elements)
		this.storeEntityHeader(
			headerOffset + SIZED_ARRAY_LENGTH_HEADER + calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store specific header (only consisting of array capacity value)
		this.store_long(headerOffset + SIZED_ARRAY_OFFSET_LENGTH, array.length);

		// store content: array content up to size, trailing nulls are cut off.
		this.storeReferencesAsList(
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			persister,
			array,
			offset,
			size
		);
	}

	private static final long OFFSET_ROOTS_OID_LIST = 0;
	
	public final void storeRoots(
		final long                          typeId    ,
		final long                          objectId  ,
		final XGettingTable<String, Object> entries   ,
		final PersistenceStoreHandler       idResolver
	)
	{
		// performance is not important here as roots only get stored once per system start and are very few in numbers
		final String[] identifiers = entries.keys().toArray(String.class);
		final Object[] instances   = entries.values().toArray();
		
		final int instanceCount = instances.length;

		// calculate all the lengths
		final long instancesTotalBinLength     = calculateReferenceListTotalBinaryLength(instanceCount);
		final long identifiersContentBinLength = calculateStringListContentBinaryLength(identifiers);
		final long totalContentLength          = instancesTotalBinLength
			+ Binary.toBinaryListTotalByteLength(identifiersContentBinLength)
		;

		// store header for writing and reserving total length before writing content
		this.storeEntityHeader(totalContentLength, typeId, objectId);

		// store instances first to allow efficient references-only caching
		this.storeReferencesAsList(OFFSET_ROOTS_OID_LIST, idResolver, instances, 0, instanceCount);

		// store identifiers as list of inlined [char]s
		this.storeStringsAsList(
			this.address + instancesTotalBinLength,
			identifiersContentBinLength,
			identifiers
		);
	}
	
	public final <T extends XAddingMap<String, Long>> T buildRootMapping(final T mapping)
	{
		final long[] objectIds = this.buildArray_long(OFFSET_ROOTS_OID_LIST);
		
		final long oidListBinaryLength     = Binary.toBinaryListTotalByteLength(objectIds.length * Long.BYTES);
		final long identifiersBinaryOffset = OFFSET_ROOTS_OID_LIST + oidListBinaryLength;
		
		final String[] identifiers = this.buildStrings(identifiersBinaryOffset);

		if(objectIds.length != identifiers.length)
		{
			// just to be safe
			throw new RuntimeException(); // (21.10.2013 TM)EXCP: proper exception
		}

		// To really validate consistency completely
		final EqHashEnum<Long> objectIdUniquenessChecker = EqHashEnum.New();
		
		for(int i = 0; i < objectIds.length; i++)
		{
			if(!objectIdUniquenessChecker.add(objectIds[i]))
			{
				// (02.09.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Persisted root entries have a duplicate root objectId for entry ("
					+ identifiers[i] + " -> " + objectIds[i] + ")"
				);
			}
			
			if(!mapping.add(identifiers[i], objectIds[i]))
			{
				// (02.09.2019 TM)EXCP: proper exception
				throw new RuntimeException(
					"Persisted root entries have a duplicate root identifiers for entry ("
					+ identifiers[i] + " -> " + objectIds[i] + ")"
				);
			}
		}
		
		return mapping;
	}

	public final void storeIterableAsList(
		final long                tid         ,
		final long                oid         ,
		final long                headerOffset,
		final Iterable<?>         elements    ,
		final long                size        ,
		final PersistenceFunction persister
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		// store entity header including the complete content size (headerOffset + elements)
		this.storeEntityHeader(
			headerOffset + calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store elements
		this.storeIterableContentAsList(headerOffset, persister, elements, size);
	}

	public int getSizedArrayElementCount(final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		return X.checkArrayRange(this.getBinaryListElementCountValidating(
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			LENGTH_OID
		));
	}

	/**
	 * Updates the passed array up to the size defined by the binary data, returns the size.
	 */
	public final int updateSizedArrayObjectReferences(
		final long                        headerOffset,
		final Object[]                    array       ,
		final PersistenceObjectIdResolver idResolver
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		final int size = this.getSizedArrayElementCount(headerOffset);
		if(array.length < size)
		{
			throw new IllegalArgumentException(); // (23.10.2013 TM)EXCP: proper exception
		}
		
		this.updateArrayObjectReferences(
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			idResolver,
			array,
			0,
			size
		);
		
		return size;
	}

	public final int getSizedArrayLength(final long sizedArrayOffset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		/* Note on length validation for "array bombs" prevention
		 * (see BinaryPersistence#getBinaryListElementCountValidating)
		 * That kind of validation cannot be done here.
		 * Consider the following scenario, which would be perfectly correct:
		 * - An ArrayList is created with max int capacity and only one element (size is 1).
		 * - That ArrayList instance is serialized.
		 * - The array length value in binary form would be max int, the following binary list would contain only 1 element.
		 * - This means the resulting total length would indeed be tiny (currently 56 bytes).
		 * - The ArrayList instance created from that information would, however, be around 8/16 GB in size.
		 * All that would be perfectly correct. It is just an incredibly efficient binary form compression of
		 * a mostly empty array that cannot be validated against the binary form of the sent instance.
		 * 
		 * Instead, a customizable controller (PersistenceSizedArrayLengthController) is used in the calling context.
		 */
		return X.checkArrayRange(
			this.get_longFromAddress(this.loadItemEntityContentAddress() + sizedArrayOffset + SIZED_ARRAY_OFFSET_LENGTH)
		);
	}

	public final long getSizedArrayElementsAddress(final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		return this.binaryListElementsAddress(headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public final void validateArrayLength(final Object[] array, final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		if(array.length == this.getListElementCountReferences(headerOffset))
		{
			return;
		}
		
		throw new BinaryPersistenceExceptionStateArrayLength(
			array,
			X.checkArrayRange(this.getListElementCountReferences(headerOffset))
		);
	}
				
	public final long getBinaryListTotalByteLength(final long listOffset)
	{
		final long listTotalByteLength = this.get_longFromAddress(this.loadItemEntityContentAddress() + listOffset);
		
		// validation for safety AND security(!) reasons. E.g. to prevent reading beyond the entity data in memory.
		if(this.loadItemEntityContentAddress() + listOffset + listTotalByteLength > this.getEntityBoundAddress())
		{
			throw new BinaryPersistenceExceptionInvalidList(
				this.getEntityLength(),
				this.getEntityObjectId(),
				this.getEntityTypeId(),
				listOffset,
				listTotalByteLength
			);
		}
				
		return listTotalByteLength;
	}
			
	public final long getLoadItemAvailableContentLength()
	{
		// (06.09.2014)TODO: test and comment if " + 0" (OFFSET_LEN) gets eliminated by JIT
		return entityContentLength(this.get_longFromAddress(this.loadItemEntityAddress() + OFFSET_LEN));
	}
	
	public final long getBinaryListElementCountValidating(final long listOffset, final long elementLength)
	{
		// note: does not reuse getBinaryListByteLength() intentionally since the exception here has more information

		final long listTotalByteLength = this.getBinaryListTotalByteLength(listOffset);
		final long listElementCount    = this.getBinaryListElementCountUnvalidating(listOffset);
		
		// validation for safety AND security(!) reasons. E.g. to prevent "Array Bombs", lists with fake element count.
		if(!this.isValidLoadItemContentLength(listOffset + listTotalByteLength)
			|| toBinaryListTotalByteLength(listElementCount * elementLength) != listTotalByteLength
		)
		{
			throw new BinaryPersistenceExceptionInvalidListElements(
				this.getEntityLength(),
				this.getEntityObjectId(),
				this.getEntityTypeId(),
				listOffset,
				listTotalByteLength,
				listElementCount,
				elementLength
			);
		}
		
		return listElementCount;
	}
	
	public final long getBinaryListElementCountUnvalidating(final long listOffset)
	{
		return this.get_longFromAddress(this.loadItemEntityContentAddress() + toBinaryListElementCountOffset(listOffset));
	}
	
	public final long getListElementCount(final long listStartOffset, final int elementLength)
	{
		return this.getBinaryListElementCountValidating(listStartOffset, elementLength);
	}
	
	public final long getListElementCountReferences(final long listStartOffset)
	{
		return this.getBinaryListElementCountValidating(
			listStartOffset,
			LENGTH_OID
		);
	}
	
	public final void iterateListElementReferences(
		final long                        listOffset,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		this.iterateListStructureReferenceRange(listOffset, 1, iterator);
	}

	public final void iterateSizedArrayElementReferences(
		final long                        offset  ,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		this.iterateListStructureReferenceRange(offset + SIZED_ARRAY_OFFSET_ELEMENTS, 1, iterator);
	}
	
	public final void iterateListStructureReferenceRange(
		final long                        listOffset          ,
		final int                         referencesPerElement,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		// (29.01.2019 TM)FIXME: priv#70: offset validation
		
		final long elementCount = this.getBinaryListElementCountValidating(
			listOffset,
			referencesPerElement * LENGTH_OID
		);
		final long elementsStartOffset = toBinaryListElementsOffset(listOffset);
		final long elementsBoundOffset = elementsStartOffset + elementCount * referencesPerElement * LENGTH_OID;
		
		// validations have already been done above.
		this.iterateReferenceRangeUnvalidated(elementsStartOffset, elementsBoundOffset, iterator);
	}
	
	public final void iterateKeyValueEntriesReferences(
		final long                        listOffset,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		this.iterateListStructureReferenceRange(listOffset, 2, iterator);
	}

	public final void iterateReferenceRange(
		final long                        startOffset,
		final long                        boundOffset,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		// (01.02.2019 TM)FIXME: priv#70: offset validations
		this.iterateReferenceRangeUnvalidated(startOffset, boundOffset, iterator);
	}
	
	public final void iterateReferenceRangeUnvalidated(
		final long                        startOffset,
		final long                        boundOffset,
		final PersistenceObjectIdAcceptor iterator
	)
	{
		final long startAddress = this.loadItemEntityContentAddress() + startOffset;
		final long boundAddress = this.loadItemEntityContentAddress() + boundOffset;
		
		for(long address = startAddress; address < boundAddress; address += LENGTH_OID)
		{
			iterator.acceptObjectId(this.get_longFromAddress(address));
		}
	}

	public final void storeArray_byte(final long typeId, final long objectId, final byte[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength(array.length);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_bytesToAddress(toBinaryListElementsOffset(this.address), array);
	}

	public final void storeArray_boolean(
		final long      typeId  ,
		final long      objectId,
		final boolean[] array
	)
	{
		final long totalByteLength = toBinaryListTotalByteLength(array.length);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_booleansToAddress(toBinaryListElementsOffset(this.address), array);
	}
	
	public final void storeArray_short(final long typeId, final long objectId, final short[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Short.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_shortsToAddress(toBinaryListElementsOffset(this.address), array);
	}

	public final void storeArray_char(final long typeId, final long objectId, final char[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Character.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_charsToAddress(toBinaryListElementsOffset(this.address), array);
	}

	public final void storeArray_int(final long typeId, final long objectId, final int[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Integer.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_intsToAddress(toBinaryListElementsOffset(this.address), array);
	}

	public final void storeArray_float(final long typeId, final long objectId, final float[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Float.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_floatsToAddress(toBinaryListElementsOffset(this.address),array);
	}

	public final void storeArray_long(final long typeId, final long objectId, final long[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Long.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_longsToAddress(toBinaryListElementsOffset(this.address), array);
	}

	public final void storeArray_double(final long typeId, final long objectId, final double[] array)
	{
		final long totalByteLength = toBinaryListTotalByteLength((long)array.length * Double.BYTES);
		this.storeEntityHeader(totalByteLength, typeId, objectId);

		this.store_long(LIST_OFFSET_BYTE_LENGTH, totalByteLength);
		this.store_long(LIST_OFFSET_ELEMENT_COUNT, array.length);
		this.store_doublesToAddress(toBinaryListElementsOffset(this.address), array);
	}
	
	public final void storeByte(final long typeId, final long objectId, final byte value)
	{
		this.storeEntityHeader(Byte.BYTES, typeId, objectId);
		this.store_byte(value);
	}

	public final void storeBoolean(final long typeId, final long objectId, final boolean value)
	{
		// where is Boolean.BYTES? Does a boolean not have a binary objectId? JDK pros... .
		this.storeEntityHeader(Byte.BYTES, typeId, objectId);
		this.store_boolean(value);
	}

	public final void storeShort(final long typeId, final long objectId, final short value)
	{
		this.storeEntityHeader(Short.BYTES, typeId, objectId);
		this.store_short(value);
	}

	public final void storeCharacter(final long typeId, final long objectId, final char value)
	{
		this.storeEntityHeader(Character.BYTES, typeId, objectId);
		this.store_char(value);
	}

	public final void storeInteger(final long typeId, final long objectId, final int value)
	{
		this.storeEntityHeader(Integer.BYTES, typeId, objectId);
		this.store_int(value);
	}

	public final void storeFloat(final long typeId, final long objectId, final float value)
	{
		this.storeEntityHeader(Float.BYTES, typeId, objectId);
		this.store_float(value);
	}

	public final void storeLong(final long typeId, final long objectId, final long value)
	{
		this.storeEntityHeader(Long.BYTES, typeId, objectId);
		this.store_long(value);
	}

	public final void storeDouble(final long typeId, final long objectId, final double value)
	{
		this.storeEntityHeader(Double.BYTES, typeId, objectId);
		this.store_double(value);
	}

	public final void storeStateless(final long typeId, final long objectId)
	{
		this.storeEntityHeader(0L, typeId, objectId); // so funny :D
	}
	
	public final void storeStringValue(
		final long   typeId  ,
		final long   objectId,
		final String string
	)
	{
		// since Java 9, there is no sane way to store the string's internal data directly
		this.storeStringValue(typeId, objectId, XChars.readChars(string));
	}
	
	public final void storeStringValue(
		final long   typeId  ,
		final long   objectId,
		final char[] chars
	)
	{
		this.storeStringValue(typeId, objectId, chars, 0, chars.length);
	}

	public final void storeStringValue(
		final long   typeId  ,
		final long   objectId,
		final char[] chars   ,
		final int    offset  ,
		final int    length
	)
	{
		this.storeEntityHeader(
			calculateBinaryLengthChars(chars.length),
			typeId,
			objectId
		);
		this.storeCharsAsList(0, chars, offset, length);
	}
	
	public final void storeReferences(
		final long                    typeId      ,
		final long                    objectId    ,
		final long                    binaryOffset,
		final PersistenceStoreHandler idResolver  ,
		final Object[]                array
	)
	{
		this.storeReferences(typeId, objectId, binaryOffset, idResolver, array, 0, array.length);
	}

	public final void storeReferences(
		final long                    typeId      ,
		final long                    objectId    ,
		final long                    binaryOffset,
		final PersistenceStoreHandler idResolver  ,
		final Object[]                array       ,
		final int                     arrayOffset ,
		final int                     arrayLength
	)
	{
		this.storeEntityHeader(
			binaryOffset + calculateReferenceListTotalBinaryLength(arrayLength),
			typeId,
			objectId
		);

		this.storeReferencesAsList(binaryOffset, idResolver, array, arrayOffset, arrayLength);
	}
	
	public final void storeFixedSize(
		final PersistenceStoreHandler handler      ,
		final long                    contentLength,
		final long                    typeId       ,
		final long                    objectId     ,
		final Object                  instance     ,
		final long[]                  memoryOffsets,
		final BinaryValueStorer[]     storers
	)
	{
		this.storeEntityHeader(contentLength, typeId, objectId);
		long address = this.address;
		for(int i = 0; i < memoryOffsets.length; i++)
		{
			address = storers[i].storeValueFromMemory(instance, memoryOffsets[i], address, handler);
		}
	}

	public final String[] buildStrings(final long stringsListOffset)
	{
		// validation is done on each single string
		final long stringsCount = this.getBinaryListElementCountUnvalidating(stringsListOffset);
		final String[] array = new String[X.checkArrayRange(stringsCount)];

		long stringsOffset = toBinaryListElementsOffset(stringsListOffset); // first element address
		for(int i = 0; i < array.length; i++)
		{
			array[i] = String.valueOf(this.buildArray_char(stringsOffset)); // build string element
			stringsOffset += this.getBinaryListTotalByteLength(stringsOffset); // scroll to next element
		}

		// as this is an offset-based public method, it must return an offset, not an absolute address
		return array;
	}
	
	public final Byte buildByte()
	{
		/*
		 * Deprecated with Java 9, with the hint "It is rarely appropriate to use this constructor".
		 * 
		 * The problem is: This is one of those occasions where it is appropriate.
		 * Cached value instances are already covered by using their unique objectId,
		 * but there are occasions where instance identity matters, hence a loading/building
		 * process must stay true to what it received during storing, otherwise it
		 * can potentially (or WILL in respective cases) change the program behavior.
		 * Whether or not that is a good idea in the first place is not the point, here.
		 * It is a decision of the application developer and a library like this has to
		 * transparently handle whatever he/she decided to do (maybe for a good reason of a certain special case).
		 * A library that alters design choices of a using developer and changes the application's behavior
		 * is nothing but bugged.
		 * That must be avoided as thoroughly as possible.
		 * In this case, that means to maintain the identities of the received application instances, no matter what.
		 * 
		 * Sadly, using JDK 9 causes a deprecation warning here. Suppressing that causes an unnecessary suppression
		 * in JDK < 9 which apparently cannot be suppressed itself (except with deactivating compiler settings).
		 * So either way, there will be a warning somewhere.
		 * 
		 * Should the constructor really disappear in a future version,
		 * then either valueOf() can be used (since there will be no more way to even create instances explicitely)
		 * or yet another hack has to be applied by low-level-instantiating an instance and low-level setting
		 * its value into the final field.
		 */
		return new Byte(this.get_byteFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Boolean buildBoolean()
	{
		// see comment in #buildByte()
		return new Boolean(this.get_booleanFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Short buildShort()
	{
		// see comment in #buildByte()
		return new Short(this.get_shortfromAddress(this.loadItemEntityContentAddress()));
	}

	public final Character buildCharacter()
	{
		// see comment in #buildByte()
		return new Character(this.get_charFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Integer buildInteger()
	{
		// see comment in #buildByte()
		return new Integer(this.get_intFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Float buildFloat()
	{
		// decimal value instances are not chached, so #valueOf() can be used safely.
		return Float.valueOf(this.get_floatFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Long buildLong()
	{
		// see comment in #buildByte()
		return new Long(this.get_longFromAddress(this.loadItemEntityContentAddress()));
	}

	public final Double buildDouble()
	{
		// decimal value instances are not chached, so #valueOf() can be used safely.
		return Double.valueOf(this.get_doubleFromAddress(this.loadItemEntityContentAddress()));
	}
	
	public final byte[] buildArray_byte()
	{
		final long elementCount = this.getBinaryListElementCountValidating(0, Byte.BYTES);
		final byte[] array;
		this.update_bytesFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array = new byte[X.checkArrayRange(elementCount)]
		);
		return array;
	}

	public final byte[] createArray_byte()
	{
		return new byte[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Byte.BYTES))];
	}

	public final void updateArray_byte(final byte[] array)
	{
		this.validateLoadItemContentLength(array.length);
		this.update_bytesFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final boolean[] createArray_boolean()
	{
		// because Boolean couldn't get a BYTES constant. Too much to ask.
		return new boolean[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Byte.BYTES))];
	}

	public final void updateArray_boolean(final boolean[] array)
	{
		this.validateLoadItemContentLength(array.length);
		this.update_booleansFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final short[] createArray_short()
	{
		return new short[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Short.BYTES))];
	}

	public final void updateArray_short(final short[] array)
	{
		this.validateLoadItemContentLength(array.length * Short.BYTES);
		this.update_shortsFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final char[] createArray_char()
	{
		return this.createArray_char(0);
	}
	
	public final char[] createArray_char(final long listOffset)
	{
		return new char[X.checkArrayRange(this.getBinaryListElementCountValidating(listOffset, Character.BYTES))];
	}

	public final void updateArray_char(final char[] array)
	{
		this.validateLoadItemContentLength(array.length * Short.BYTES);
		this.updateArray_charUnvalidating(array, 0);
	}
		
	public final String buildString()
	{
		// since Java 9, there is no sane way to build a string without copying the loaded data multiple times.
		return String.valueOf(this.buildArray_char());
	}

	public final char[] buildArray_char()
	{
		return this.buildArray_char(0);
	}

	public final char[] buildArray_char(final long listOffset)
	{
		// (01.02.2019 TM)FIXME: priv#70: offset validation
		final char[] array = this.createArray_char(listOffset);
		this.updateArray_charUnvalidating(array, listOffset);
		
		return array;
	}

	public final long[] buildArray_long()
	{
		return this.buildArray_long(0);
	}

	public final long[] buildArray_long(final long listOffset)
	{
		// (01.02.2019 TM)FIXME: priv#70: offset validation
		final long[] array = this.createArray_long_unvalidating(listOffset);
		this.update_longsFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress() + listOffset),
			array
		);
		
		return array;
	}

	public final int[] createArray_int()
	{
		return new int[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Integer.BYTES))];
	}

	public final void updateArray_int(final int[] array)
	{
		this.validateLoadItemContentLength(array.length * Integer.BYTES);
		this.update_intsFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final float[] createArray_float()
	{
		return new float[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Float.BYTES))];
	}

	public final void updateArray_float(final float[] array)
	{
		this.validateLoadItemContentLength(array.length * Float.BYTES);
		this.update_floatsFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final long[] createArray_long()
	{
		return this.createArray_long_unvalidating(0);
	}
	
	final long[] createArray_long_unvalidating(final long offset)
	{
		return new long[X.checkArrayRange(this.getBinaryListElementCountValidating(offset, Long.BYTES))];
	}

	public final void updateArray_long(final long[] array)
	{
		this.validateLoadItemContentLength(array.length * Long.BYTES);
		this.update_longsFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final double[] createArray_double()
	{
		return new double[X.checkArrayRange(this.getBinaryListElementCountValidating(0, Double.BYTES))];
	}

	public final void updateArray_double(final double[] array)
	{
		this.validateLoadItemContentLength(array.length * Double.BYTES);
		this.update_doublesFromAddress(
			toBinaryListElementsOffset(this.loadItemEntityContentAddress()),
			array
		);
	}

	public final void updateArrayObjectReferences(
		final long                        binaryOffset,
		final PersistenceObjectIdResolver oidResolver ,
		final Object[]                    array       ,
		final int                         arrayOffset ,
		final int                         arrayLength
	)
	{
		final long elementCount = this.getListElementCountReferences(binaryOffset);
		if(elementCount < arrayLength)
		{
			throw new BinaryPersistenceExceptionStateArrayLength(
				array,
				X.checkArrayRange(elementCount)
			);
		}
		
		final long binaryElementsStartAddress = this.binaryListElementsAddress(binaryOffset);
		for(int i = 0; i < arrayLength; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			array[arrayOffset + i] = oidResolver.lookupObject(
				this.get_longFromAddress(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public final void collectElementsIntoArray(
		final long                        binaryOffset,
		final PersistenceObjectIdResolver oidResolver ,
		final Object[]                    target
	)
	{
		final long binaryElementsStartAddress = this.binaryListElementsAddress(binaryOffset);
		for(int i = 0; i < target.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			target[i] = oidResolver.lookupObject(
				this.get_longFromAddress(binaryElementsStartAddress + referenceBinaryLength(i))
			);
		}
	}

	public final int collectListObjectReferences(
		final long                        binaryOffset,
		final PersistenceObjectIdResolver oidResolver ,
		final Consumer<Object>            collector
	)
	{
		final int size = X.checkArrayRange(this.getListElementCountReferences(binaryOffset));
		this.collectObjectReferences(
			binaryOffset,
			size        ,
			oidResolver ,
			collector
		);
		return size;
	}

	public final void collectObjectReferences(
		final long                        binaryOffset,
		final int                         length      ,
		final PersistenceObjectIdResolver oidResolver ,
		final Consumer<Object>            collector
	)
	{
		final long binaryElementsStartAddress = this.binaryListElementsAddress(binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				oidResolver.lookupObject(
					this.get_longFromAddress(binaryElementsStartAddress + referenceBinaryLength(i))
				)
			);
		}
	}

	public final int collectKeyValueReferences(
		final long                        binaryOffset,
		final int                         length      ,
		final PersistenceObjectIdResolver oidResolver ,
		final BiConsumer<Object, Object>  collector
	)
	{
		final long binaryElementsStartAddress = this.binaryListElementsAddress(binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				// key (on every 2nth objectId position)
				oidResolver.lookupObject(
					this.get_longFromAddress(binaryElementsStartAddress + referenceBinaryLength(i << 1))
				),
				// value (on every (2n + 1)th objectId position)
				oidResolver.lookupObject(
					this.get_longFromAddress(binaryElementsStartAddress + referenceBinaryLength(i << 1) + LENGTH_OID)
				)
			);
		}
		return length;
	}
	
	public final void updateFixedSize(
		final Object                      instance     ,
		final BinaryValueSetter[]         setters      ,
		final long[]                      memoryOffsets,
		final PersistenceObjectIdResolver idResolver
	)
	{
		long address = this.loadItemEntityContentAddress();
		for(int i = 0; i < setters.length; i++)
		{
			address = setters[i].setValueToMemory(address, instance, memoryOffsets[i], idResolver);
		}
	}
			
	public abstract long iterateReferences(
		BinaryReferenceTraverser[]  traversers,
		PersistenceObjectIdAcceptor acceptor
	);

	public final long storeCharsAsList(
		final long   memoryOffset,
		final char[] chars       ,
		final int    offset      ,
		final int    length
	)
	{
		// total binary length is header length plus content length
		final long elementsBinaryLength = length * Character.BYTES;
		final long elementsDataAddress  = this.address + memoryOffset + LIST_OFFSET_ELEMENTS;
		this.storeListHeader(memoryOffset, elementsBinaryLength, length);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			this.set_charToAddress(elementsDataAddress + (i << 1), chars[i]);
		}

		return elementsDataAddress + elementsBinaryLength;
	}
	
	public final void storeCharsDirect(
		final long   address,
		final char[] chars  ,
		final int    offset ,
		final int    length
	)
	{
		this.store_charsToAddress(address, chars, offset, length);
	}
	
	public final void readCharsDirect(
		final long   address,
		final char[] chars  ,
		final int    offset ,
		final int    length
	)
	{
		this.update_charsFromAddress(address, chars, offset, length);
	}
		
	public final void copyMemory(
		final ByteBuffer          directByteBuffer,
		final long                offset          ,
		final BinaryValueSetter[] setters         ,
		final long[]              targetOffsets
	)
	{
		final long targetAddress = this.calculateAddress(directByteBuffer, offset);
		
		long address = this.loadItemEntityContentAddress();
		for(int i = 0; i < setters.length; i++)
		{
			address = setters[i].setValueToMemory(address, null, targetAddress + targetOffsets[i], null);
		}
	}

	public final void storeListHeader(
		final long offset              ,
		final long elementsBinaryLength,
		final long elementsCount
	)
	{
		this.store_long(offset + LIST_OFFSET_BYTE_LENGTH, toBinaryListTotalByteLength(elementsBinaryLength));
		this.store_long(offset + LIST_OFFSET_ELEMENT_COUNT, elementsCount);
	}
	
	public final void storeIterableContentAsList(
		final long                offset      ,
		final PersistenceFunction persister   ,
		final Iterable<?>         elements    ,
		final long                elementCount
	)
	{
		final long referenceLength     = referenceBinaryLength(1);
		final long elementsBinaryRange = elementCount * referenceLength;
		final long elementsDataAddress = this.address + offset + LIST_OFFSET_ELEMENTS;
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;
		
		this.storeListHeader(offset, elementsBinaryRange, elementCount);

		/*
		 * Must check elementCount on every element because under no circumstances may the memory be set
		 * beyond the reserved range (e.g. concurrent modification of the passed collection)
		 */
		final Iterator<?> iterator = elements.iterator();
		long a = elementsDataAddress;
		while(a < elementsBinaryBound && iterator.hasNext())
		{
			final Object element = iterator.next();
			this.set_longToAddress(a, persister.apply(element));
			a += referenceLength;
		}

		validatePostIterationState(a, elementsBinaryBound, iterator, elementCount, referenceLength);
	}

	public final void storeStringsAsList(
		final long     storeAddress                    ,
		final long     precalculatedContentBinaryLength,
		final String[] strings
	)
	{
		this.storeStringsAsList(storeAddress, precalculatedContentBinaryLength, strings, 0, strings.length);
	}

	public final void storeStringsAsList(
		final long     memoryOffset                    ,
		final long     precalculatedContentBinaryLength,
		final String[] strings                         ,
		final int      offset                          ,
		final int      length
	)
	{
		this.storeListHeader(memoryOffset, precalculatedContentBinaryLength, length);
		
		long elementsDataAddress = this.address + memoryOffset + LIST_OFFSET_ELEMENTS;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			elementsDataAddress = this.storeCharsAsListToAddress(elementsDataAddress, XChars.readChars(strings[i]));
		}
	}

	// (04.10.2019 TM)FIXME: priv#111: consistently rename internal and external methods with read/get and store/set

	public final void storeKeyValuesAsEntries(
		final long                               offset      ,
		final PersistenceFunction                persister   ,
		final Iterable<? extends KeyValue<?, ?>> elements    ,
		final long                               elementCount
	)
	{
		final long referenceLength     = referenceBinaryLength(1);
		final long entryLength         = Binary.referenceBinaryLength(2); // two references per entry
		final long elementsBinaryRange = elementCount * entryLength;
		final long elementsDataAddress = this.address + offset + LIST_OFFSET_ELEMENTS;
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;
		
		this.storeListHeader(offset, elementsBinaryRange, elementCount);

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		final Iterator<? extends KeyValue<?, ?>> iterator = elements.iterator();
		long a = elementsDataAddress;
		while(a < elementsBinaryBound && iterator.hasNext())
		{
			final KeyValue<?, ?> element = iterator.next();
			this.set_longToAddress(a                  , persister.apply(element.key())  );
			this.set_longToAddress(a + referenceLength, persister.apply(element.value()));
			a += entryLength; // advance index for both in one step
		}

		validatePostIterationState(a, elementsBinaryBound, iterator, elementCount, entryLength);
	}
	
	public final void storeReferencesAsList(
		final long                    memoryOffset,
		final PersistenceStoreHandler persister   ,
		final Object[]                array       ,
		final int                     offset      ,
		final int                     length
	)
	{
		this.storeListHeader(
			memoryOffset,
			referenceBinaryLength(length),
			length
		);

		final long elementsDataAddress = this.address + memoryOffset + LIST_OFFSET_ELEMENTS;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			this.set_longToAddress(elementsDataAddress + referenceBinaryLength(i), persister.apply(array[i]));
		}
	}
	
	
	
	public final void store_byte(final long offset, final byte value)
	{
		this.set_byteToAddress(this.address + offset, value);
	}

	public final void store_boolean(final long offset, final boolean value)
	{
		this.set_booleanToAddress(this.address + offset, value);
	}

	public final void store_short(final long offset, final short value)
	{
		this.set_shortToAddress(this.address + offset, value);
	}

	public final void store_char(final long offset, final char value)
	{
		this.set_charToAddress(this.address + offset, value);
	}

	public final void store_int(final long offset, final int value)
	{
		this.set_intToAddress(this.address + offset, value);
	}

	public final void store_float(final long offset, final float value)
	{
		this.set_floatToAddress(this.address + offset, value);
	}

	public final void store_long(final long offset, final long value)
	{
		this.set_longToAddress(this.address + offset, value);
	}

	public final void store_double(final long offset, final double value)
	{
		this.set_doubleToAddress(this.address + offset, value);
	}
	


	public final void store_byte(final byte value)
	{
		this.set_byteToAddress(this.address, value);
	}

	public final void store_boolean(final boolean value)
	{
		this.set_booleanToAddress(this.address, value);
	}

	public final void store_short(final short value)
	{
		this.set_shortToAddress(this.address, value);
	}

	public final void store_char(final char value)
	{
		this.set_charToAddress(this.address, value);
	}

	public final void store_int(final int value)
	{
		this.set_intToAddress(this.address, value);
	}

	public final void store_float(final float value)
	{
		this.set_floatToAddress(this.address, value);
	}

	public final void store_long(final long value)
	{
		this.set_longToAddress(this.address, value);
	}

	public final void store_double(final double value)
	{
		this.set_doubleToAddress(this.address, value);
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// internal methods //
	/////////////////////
	
	final long getEntityBoundAddress()
	{
		// (06.09.2014)TODO: test and comment if " + 0" gets eliminated by JIT
		return this.loadItemEntityAddress() + this.get_longFromAddress(this.loadItemEntityAddress() + OFFSET_LEN);
	}
	
	void storeEntityHeaderToAddress(
		final long entityAddress    ,
		final long entityTotalLength,
		final long entityTypeId     ,
		final long entityObjectId
	)
	{
		setEntityHeaderRawValuesToAddress(entityAddress, entityTotalLength, entityTypeId, entityObjectId);
	}
	
	
	final long binaryListElementsAddress(final long binaryListOffset)
	{
		return this.loadItemEntityContentAddress() + toBinaryListElementsOffset(binaryListOffset);
	}
			
	abstract long loadItemEntityContentAddress();
	
	private long loadItemEntityAddress()
	{
		return entityAddressFromContentAddress(this.loadItemEntityContentAddress());
	}
	
	public abstract void modifyLoadItem(
		ByteBuffer directByteBuffer ,
		long       offset           ,
		long       entityTotalLength,
		long       entityTypeId     ,
		long       entityObjectId
	);
	
	private void validateLoadItemContentLength(final long contentLength)
	{
		if(this.isValidLoadItemContentLength(XMath.positive(contentLength)))
		{
			return;
		}
		
		// (08.02.2019 TM)EXCP: proper exception
		throw new PersistenceException(
			"Binary load item bounds violation: " + contentLength
				+ " > " + this.getLoadItemAvailableContentLength()
		);
	}
	
	private boolean isValidLoadItemContentLength(final long contentLength)
	{
		return contentLength <= this.getLoadItemAvailableContentLength();
	}
	
	private static void validatePostIterationState(
		final long        address            ,
		final long        elementsBinaryBound,
		final Iterator<?> iterator           ,
		final long        elementCount       ,
		final long        entryLength
	)
	{
		/*
		 * If there are fewer OR more elements than specified, it is an error.
		 * The element count must match exactely, no matter what.
		 */
		// (19.03.2019 TM)NOTE: added "|| iterator.hasNext()" check
		if(address != elementsBinaryBound || iterator.hasNext())
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element count: specified " + elementCount
				+ " vs. iterated " + elementsBinaryBound / entryLength
			);
		}
	}

	final long storeCharsAsListToAddress(final long address, final char[] chars)
	{
		return this.storeCharsAsListToAddress(address, chars, 0, chars.length);
	}
	
	final long storeCharsAsListToAddress(
		final long   address,
		final char[] chars  ,
		final int    offset ,
		final int    length
	)
	{
		// total binary length is header length plus content length
		final long elementsBinaryLength = length * Character.BYTES;
		final long elementsDataAddress  = address + LIST_OFFSET_ELEMENTS;

		this.set_longToAddress(address + LIST_OFFSET_BYTE_LENGTH, toBinaryListTotalByteLength(elementsBinaryLength));
		this.set_longToAddress(address + LIST_OFFSET_ELEMENT_COUNT, length);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			this.set_charToAddress(elementsDataAddress + (i << 1), chars[i]);
		}

		return elementsDataAddress + elementsBinaryLength;
	}
	
	final void updateArray_char(final char[] array, final long offset)
	{
		this.validateLoadItemContentLength(offset + array.length * Short.BYTES);
		this.updateArray_charUnvalidating(array, offset);
	}
	
	final void updateArray_charUnvalidating(final char[] array, final long offset)
	{
		this.update_charsFromAddress(
			this.binaryListElementsAddress(offset),
			array
		);
	}
	
	
	long calculateAddress(final ByteBuffer byteBuffer, final long offset)
	{
		if(byteBuffer == null)
		{
			return offset;
		}
		
		if(offset > byteBuffer.capacity())
		{
			// (10.10.2019 TM)EXCP: proper exception
			throw new RuntimeException(
				"Specified offset exceeds buffer capacity: " + offset + " > " + byteBuffer.capacity()
			);
		}
		
		return PlatformInternals.getDirectBufferAddress(byteBuffer) + X.checkArrayRange(offset);
	}
	
			
	
	///////////////////////////////////////////////////////////////////////////
	// byte order handling //
	////////////////////////
		
	final byte get_byteFromAddress(final long address)
	{
		return XMemory.get_byte(address);
	}

	final boolean get_booleanFromAddress(final long address)
	{
		return XMemory.get_boolean(address);
	}

	short get_shortfromAddress(final long address)
	{
		return XMemory.get_short(address);
	}

	char get_charFromAddress(final long address)
	{
		return XMemory.get_char(address);
	}

	int get_intFromAddress(final long address)
	{
		return XMemory.get_int(address);
	}

	float get_floatFromAddress(final long address)
	{
		return XMemory.get_float(address);
	}

	long get_longFromAddress(final long address)
	{
		return XMemory.get_long(address);
	}

	double get_doubleFromAddress(final long address)
	{
		return XMemory.get_double(address);
	}
	

	
	final void set_byteToAddress(final long address, final byte value)
	{
		XMemory.set_byte(address, value);
	}
	
	final void set_booleanToAddress(final long address, final boolean value)
	{
		XMemory.set_boolean(address, value);
	}
	
	void set_shortToAddress(final long address, final short value)
	{
		XMemory.set_short(address, value);
	}
	
	void set_charToAddress(final long address, final char value)
	{
		XMemory.set_char(address, value);
	}
	
	void set_intToAddress(final long address, final int value)
	{
		XMemory.set_int(address, value);
	}
	
	void set_floatToAddress(final long address, final float value)
	{
		XMemory.set_float(address, value);
	}
	
	void set_longToAddress(final long address, final long value)
	{
		XMemory.set_long(address, value);
	}
	
	void set_doubleToAddress(final long address, final double value)
	{
		XMemory.set_double(address, value);
	}
	
		
	
	final void update_bytesFromAddress(final long address, final byte[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	final void update_booleansFromAddress(final long address, final boolean[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_shortsFromAddress(final long address, final short[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_charsFromAddress(final long address, final char[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_charsFromAddress(final long address, final char[] target, final int offset, final int length)
	{
		XMemory.copyRangeToArray(address, target, offset, length);
	}

	void update_intsFromAddress(final long address, final int[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_floatsFromAddress(final long address, final float[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_longsFromAddress(final long address, final long[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}

	void update_doublesFromAddress(final long address, final double[] target)
	{
		XMemory.copyRangeToArray(address, target);
	}
	
	
	
	final void store_bytesToAddress(final long address, final byte[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	final void store_booleansToAddress(final long address, final boolean[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_shortsToAddress(final long address, final short[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_charsToAddress(final long address, final char[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_charsToAddress(final long address, final char[] values, final int offset, final int length)
	{
		XMemory.copyArrayToAddress(values, offset, length, address);
	}
	
	void store_intsToAddress(final long address, final int[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_floatsToAddress(final long address, final float[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_longsToAddress(final long address, final long[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	
	void store_doublesToAddress(final long address, final double[] values)
	{
		XMemory.copyArrayToAddress(values, address);
	}
	


	///////////////////////////////////////////////////////////////////////////
	// Helper //
	///////////
	
	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * fail or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 */
	public final synchronized void registerHelper(final Object key, final Object helper)
	{
		// no hash table since the amount of helper objects is assumed to be tiny, anyway.
		for(HelperEntry e = this.helperEntry; e != null; e = e.next)
		{
			if(e.key == key)
			{
				e.helper = helper;
				return;
			}
		}
		
		this.helperEntry = new HelperEntry(key, helper, this.helperEntry);
	}

	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 */
	public final synchronized Object getHelper(final Object key)
	{
		for(HelperEntry e = this.helperEntry; e != null; e = e.next)
		{
			if(e.key == key)
			{
				return e.helper;
			}
		}
		
		return null;
	}
	
	static final class HelperEntry
	{
		Object      key   ;
		Object      helper;
		HelperEntry next  ;
		
		HelperEntry(final Object key, final Object helper, final HelperEntry next)
		{
			super();
			this.key    = key  ;
			this.helper = helper;
			this.next   = next  ;
		}
		
	}
	
}
//CHECKSTYLE.ON: AbstractClassName
