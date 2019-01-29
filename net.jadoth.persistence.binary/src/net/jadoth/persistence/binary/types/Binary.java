package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionInvalidList;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionInvalidListElements;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceObjectIdResolver;
import net.jadoth.persistence.types.PersistenceStoreHandler;
import net.jadoth.typing.KeyValue;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunk
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
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
		SIZED_ARRAY_LENGTH_HEADER   = XMemory.byteSize_long()  , // the header only consists of the length
		SIZED_ARRAY_OFFSET_ELEMENTS = SIZED_ARRAY_LENGTH_HEADER  // the element list begins after the header
	;
	
	/**
	 * Obviously 2 references: the key and the value.
	 */
	private static final int KEY_VALUE_REFERENCE_COUNT = 2;
	
	private static final long KEY_VALUE_BINARY_LENGTH = KEY_VALUE_REFERENCE_COUNT * BinaryPersistence.oidByteLength();
	
	public static final int keyValueReferenceCount()
	{
		return KEY_VALUE_REFERENCE_COUNT;
	}
	
	public static final long keyValueBinaryLength()
	{
		return KEY_VALUE_BINARY_LENGTH;
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////
	
	/**
	 * Depending on the deriving class, this is either a single entity's address for reading data
	 * or the beginning of a store chunk for storing multiple entities in a row (for efficiency reasons).
	 */
	long address;

	/**
	 * Needed in single-entity {@link BuildItem2} anyway and negligible in mass-entity implementations.
	 */
	private Object helper;
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////
	
	protected Binary()
	{
		super();
	}
	
	
	
	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////
		
	// (25.01.2019 TM)NOTE: kept with JET-49

	@Override
	public abstract ByteBuffer[] buffers();
	
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
	 *
	 * @param subject
	 * @param helper
	 * @return
	 */
	public final synchronized void setHelper(final Object helper)
	{
		if(this.helper instanceof HelperAnchor)
		{
			HelperAnchor anchor = (HelperAnchor)this.helper;
			while(anchor.actualHelper instanceof HelperAnchor)
			{
				anchor = (HelperAnchor)anchor.actualHelper;
			}
			anchor.actualHelper = helper;
		}
		else
		{
			this.helper = helper;
		}
	}

	/**
	 * Helper instances can be used as temporary additional state for the duration of the building process.
	 * E.g.: JDK hash collections cannot properly collect elements during the building process as the element instances
	 * might still be in an initialized state without their proper data, so hashing and equality comparisons would
	 * failt or result in all elements being "equal". So building JDK hash collections required to pre-collect
	 * their elements in an additional helper structure and defer the actual elements collecting to the completion.
	 * <p>
	 * Similar problems with other or complex custom handlers are conceivable.
	 *<p>
	 * Only one helper object can be registered per subject instance (the instance to be built).
	 *
	 * @param subject
	 * @return
	 */
	public final synchronized Object getHelper()
	{
		if(this.helper instanceof HelperAnchor)
		{
			HelperAnchor anchor = (HelperAnchor)this.helper;
			while(anchor.actualHelper instanceof HelperAnchor)
			{
				anchor = (HelperAnchor)anchor.actualHelper;
			}
			return anchor.actualHelper;
		}
		
		return this.helper;
	}
	
	public final synchronized void anchorHelper(final Object anchorSubject)
	{
		this.helper = new HelperAnchor(anchorSubject, this.helper);
	}
	
	/**
	 * In rare cases (legacy type mapping), a direct byte buffer must be "anchored" in order to not get gc-collected
	 * and cause its memory to be deallocated. Anchoring means it just has to be referenced by anything that lives
	 * until the end of the entity loading/building process. It never has to be dereferenced again.
	 * In order to not need another fixed field, which would needlessly occupy memory for EVERY entity in almost every
	 * case, a "helper anchor" is used: a nifty instance that is clamped in between the actual load item and the actual
	 * helper instance.
	 * 
	 * @author TM
	 *
	 */
	static final class HelperAnchor
	{
		final Object anchorSubject;
		      Object actualHelper;
		
		HelperAnchor(final Object anchorSubject, final Object actualHelper)
		{
			super();
			this.anchorSubject = anchorSubject;
			this.actualHelper  = actualHelper;
		}
		
	}
	
	
	
	// (25.01.2019 TM)FIXME: temporary for JET-49
	
	/**
	 * Writes the header (etc...).
	 * <p>
	 * Returns a memory address that is guaranteed to be safe for writing {@literal len} bytes.
	 * Writing any more bytes will lead to unpredictable results, from (most likely) destroying
	 * the byte stream's consistency up to crashing the VM immediately or at some point in the future.
	 * <p>
	 * DO NOT WRITE MORE THEN {@literal len} BYTES TO THE RETURNED ADDRESS!
	 *
	 * @param entityContentLength
	 * @param entityTypeId
	 * @param entityObjectId
	 * @return
	 */
	public abstract long storeEntityHeader(
		final long entityContentLength,
		final long entityTypeId       ,
		final long entityObjectId
	);
	
	public abstract long loadItemEntityContentAddress();
	
	public abstract void setLoadItemEntityContentAddress(long entityContentAddress);
	

	/* (25.01.2019 TM)FIXME: JET-49: excplizit sets to a memory address should not be possible
	 * better: a working address and offset-less setters with internal address advancing.
	 */

	public byte get_byte(final long offset)
	{
		return XMemory.get_byte(this.address + offset);
	}
	
	public boolean get_boolean(final long offset)
	{
		return XMemory.get_boolean(this.address + offset);
	}
	
	public short get_short(final long offset)
	{
		return XMemory.get_short(this.address + offset);
	}
	
	public char get_char(final long offset)
	{
		return XMemory.get_char(this.address + offset);
	}
	
	public int get_int(final long offset)
	{
		return XMemory.get_int(this.address + offset);
	}
	
	public float get_float(final long offset)
	{
		return XMemory.get_float(this.address + offset);
	}
	
	public long get_long(final long offset)
	{
		return XMemory.get_long(this.address + offset);
	}
	
	public double get_double(final long offset)
	{
		return XMemory.get_double(this.address + offset);
	}
	
	
	
	public void set_byte(final long offset, final byte value)
	{
		XMemory.set_byte(this.address + offset, value);
	}
	
	public void set_boolean(final long offset, final boolean value)
	{
		XMemory.set_boolean(this.address + offset, value);
	}
	
	public void set_short(final long offset, final short value)
	{
		XMemory.set_short(this.address + offset, value);
	}
	
	public void set_char(final long offset, final char value)
	{
		XMemory.set_char(this.address + offset, value);
	}
	
	public void set_int(final long offset, final int value)
	{
		XMemory.set_int(this.address + offset, value);
	}
	
	public void set_float(final long offset, final float value)
	{
		XMemory.set_float(this.address + offset, value);
	}
	
	public void set_long(final long offset, final long value)
	{
		XMemory.set_long(this.address + offset, value);
	}
	
	public void set_double(final long offset, final double value)
	{
		XMemory.set_double(this.address + offset, value);
	}
	

	
	// (25.01.2019 TM)NOTE: new with JET-49
	
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
	
	public abstract Binary channelChunk(int channelIndex);
	
	public abstract int channelCount();
	
	public abstract void iterateEntityData(BinaryEntityDataReader reader);
		
	public abstract void iterateKeyValueEntriesReferences(
		long           offset  ,
		_longProcedure iterator
	);
	
	public final long storeSizedKeyValuesAsEntries(
		final long                               tid         ,
		final long                               oid         ,
		final long                               headerOffset,
		final Iterable<? extends KeyValue<?, ?>> keyValues   ,
		final long                               size        ,
		final PersistenceFunction                persister
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		// store entity header including the complete content size (headerOffset + entries)
		final long contentAddress = this.storeEntityHeader(
			headerOffset + Binary.calculateReferenceListTotalBinaryLength(size * keyValueReferenceCount()),
			tid,
			oid
		);

		// store entries
		Binary.storeKeyValuesAsEntries(contentAddress + headerOffset, persister, keyValues, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}
	
	public abstract long getListElementCountKeyValue(long listStartOffset);
	
	
	
	// (29.01.2019 TM)FIXME: -----------------------
	
	public final long storeSizedArray(
		final long                tid         ,
		final long                oid         ,
		final long                headerOffset,
		final Object[]            array       ,
		final int                 size        ,
		final PersistenceFunction persister
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		// store entity header including the complete content size (8 + elements)
		final long contentAddress = this.storeEntityHeader(
			headerOffset + SIZED_ARRAY_LENGTH_HEADER + calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store specific header (only consisting of array capacity value)
		XMemory.set_long(contentAddress + headerOffset + SIZED_ARRAY_OFFSET_LENGTH, array.length);

		// store content: array content up to size, trailing nulls are cut off.
		Binary.storeArrayContentAsList(
			contentAddress + headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			persister,
			array,
			0,
			size
		);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}

	public final long storeSizedIterableAsList(
		final long            tid         ,
		final long            oid         ,
		final long            headerOffset,
		final Iterable<?>     elements    ,
		final long            size        ,
		final PersistenceFunction persister
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		// store entity header including the complete content size (headerOffset + elements)
		final long contentAddress = this.storeEntityHeader(
			headerOffset + calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store elements
		Binary.storeIterableContentAsList(contentAddress + headerOffset, persister, elements, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}


		

	
	public int getSizedArrayElementCount(final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		return X.checkArrayRange(this.getBinaryListElementCountValidating(
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			BinaryPersistence.oidByteLength()
		));
	}

	/**
	 * Updates the passed array up to the size defined by the binary data, returns the size.
	 *
	 * @param bytes
	 * @param headerOffset
	 * @param array
	 * @return
	 */
	public final int updateSizedArrayObjectReferences(
		final long                   headerOffset,
		final Object[]               array       ,
		final PersistenceLoadHandler handler
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		final int size = this.getSizedArrayElementCount(headerOffset);
		if(array.length < size)
		{
			throw new IllegalArgumentException(); // (23.10.2013 TM)EXCP: proper exception
		}
		
		this.updateArrayObjectReferences(
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			handler,
			array,
			0,
			size
		);
		
		return size;
	}

	public final int getSizedArrayLength(final long sizedArrayOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
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
			XMemory.get_long(this.loadItemEntityContentAddress() + sizedArrayOffset + SIZED_ARRAY_OFFSET_LENGTH)
		);
	}

	public final long getSizedArrayElementsAddress(final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		return this.binaryListElementsAddressRelative(headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public final void validateArrayLength(final Object[] array, final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		if(array.length == this.getListElementCountReferences(headerOffset))
		{
			return;
		}
		
		throw new BinaryPersistenceExceptionStateArrayLength(
			array,
			X.checkArrayRange(this.getListElementCountReferences(headerOffset))
		);
	}

	public final void iterateSizedArrayElementReferences(
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		final long elementCount = this.getBinaryListElementCountValidating(
			offset + SIZED_ARRAY_OFFSET_ELEMENTS,
			BinaryPersistence.oidByteLength()
		);
		
		BinaryPersistence.iterateReferenceRange(
			this.binaryListElementsAddressRelative(offset + SIZED_ARRAY_OFFSET_ELEMENTS),
			elementCount,
			iterator
		);
	}
	
	public static final int binaryListHeaderLength()
	{
		return LIST_HEADER_LENGTH;
	}
	
	public static final long binaryListMinimumLength()
	{
		return LIST_HEADER_LENGTH;
	}

	public static final long binaryListMaximumLength()
	{
		return Long.MAX_VALUE;
	}

	public static final long calculateBinaryListByteLength(final long binaryListElementsByteLength)
	{
		return binaryListHeaderLength() + binaryListElementsByteLength;
	}
	
	// binary list byte length //
	
	// (29.01.2019 TM)FIXME: JET-49: review and remove/rename ~Absolute methods
	
	public final long getBinaryListByteLengthRelative(final long listOffset)
	{
		final long entityAddress  = BinaryPersistence.entityAddressFromContentAddress(this.loadItemEntityContentAddress());
		final long listByteLength = getBinaryListByteLengthAbsolute(this.loadItemEntityContentAddress() + listOffset);
		
		// validation for safety AND security(!) reasons. E.g. to prevent reading beyond the entity data in memory.
		if(this.loadItemEntityContentAddress() + listOffset + listByteLength
			>
			entityAddress + BinaryPersistence.getEntityLength(entityAddress)
		){
			throw new BinaryPersistenceExceptionInvalidList(
				BinaryPersistence.getEntityLength(entityAddress),
				BinaryPersistence.getEntityObjectId(entityAddress),
				BinaryPersistence.getEntityTypeId(entityAddress),
				listOffset,
				listByteLength
			);
		}
				
		return listByteLength;
	}

	public static final long getBinaryListByteLengthAbsolute(final long binaryListAddress)
	{
		return XMemory.get_long(binaryListByteLengthAddress(binaryListAddress));
	}
	
	static final long binaryListByteLengthAddress(final long binaryListAddress)
	{
		return binaryListAddress + LIST_OFFSET_BYTE_LENGTH;
	}
	

	public final long getBinaryListElementCountValidating(
		final long   listOffset   ,
		final long   elementLength
	)
	{
		// note: does not reuse getBinaryListByteLength() intentionally since the exception here has more information
		
		final long entityAddress    = BinaryPersistence.entityAddressFromContentAddress(this.loadItemEntityContentAddress());
		final long listByteLength   = getBinaryListByteLengthAbsolute(this.loadItemEntityContentAddress() + listOffset);
		final long listElementCount = getBinaryListElementCount(this.loadItemEntityContentAddress() + listOffset);
		
		// validation for safety AND security(!) reasons. E.g. to prevent "Array Bombs", lists with fake element count.
		if(this.loadItemEntityContentAddress() + listOffset + listByteLength
			> entityAddress + BinaryPersistence.getEntityLength(entityAddress)
			|| listElementCount * elementLength != listByteLength
		)
		{
			throw new BinaryPersistenceExceptionInvalidListElements(
				BinaryPersistence.getEntityLength(entityAddress),
				BinaryPersistence.getEntityObjectId(entityAddress),
				BinaryPersistence.getEntityTypeId(entityAddress),
				listOffset,
				listByteLength,
				listElementCount,
				elementLength
			);
		}
		
		return listElementCount;
	}

	// (29.01.2019 TM)FIXME: JET-49: Why does this exist instead of a Binary-related version?
	public static final long getBinaryListElementCount(final long binaryListAddress)
	{
		return XMemory.get_long(binaryListElementCountAddress(binaryListAddress));
	}
	
	static final long binaryListElementCountAddress(final long binaryListAddress)
	{
		return binaryListAddress + LIST_OFFSET_ELEMENT_COUNT;
	}

	// binary list elements address //

	public static final long binaryListElementsAddressAbsolute(final long binaryListAddress)
	{
		return binaryListAddress + LIST_OFFSET_ELEMENTS;
	}

	public final long binaryListElementsAddressRelative(final long binaryListOffset)
	{
		return binaryListElementsAddressAbsolute(this.loadItemEntityContentAddress() + binaryListOffset);
	}
	

	
	public final long getListElementCount(
		final long   listStartOffset,
		final int    elementLength
	)
	{
		return this.getBinaryListElementCountValidating(
			listStartOffset,
			elementLength
		);
	}
		
	public final long getListElementCountReferences(
		final long   listStartOffset
	)
	{
		return this.getBinaryListElementCountValidating(
			listStartOffset,
			BinaryPersistence.oidByteLength()
		);
	}
	
	public static final long calculateReferenceListTotalBinaryLength(final long count)
	{
		return calculateBinaryListByteLength(BinaryPersistence.referenceBinaryLength(count)); // 8 bytes per reference
	}

	public static final long calculateStringListContentBinaryLength(final String[] strings)
	{
		// precise size for each string (char list header plus 2 byte per char)
		long listContentBinaryLength = 0;
		for(final String string : strings)
		{
			listContentBinaryLength += calculateBinaryLengthChars(string.length());
		}

		return listContentBinaryLength;
	}

	public static final long calculateBinaryLengthChars(final long count)
	{
		return calculateBinaryListByteLength(count << 1);  // header plus 2 bytes per char
	}
	

	public final void iterateListElementReferences(
		final long           listOffset,
		final _longProcedure iterator
	)
	{
		BinaryPersistence.iterateReferenceRange(
			this.binaryListElementsAddressRelative(listOffset),
			this.getListElementCountReferences(listOffset),
			iterator
		);
	}

	public final void iterateBinaryReferences(
		final long           startOffset,
		final long           boundOffset,
		final _longProcedure iterator
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: check offsets. Or is this method on its own reasonable in the first place?
		
		final long startAddress = this.loadItemEntityContentAddress() + startOffset;
		final long boundAddress = this.loadItemEntityContentAddress() + boundOffset;
		final long oidLength    = BinaryPersistence.oidByteLength();
		
		for(long address = startAddress; address < boundAddress; address += oidLength)
		{
			iterator.accept(XMemory.get_long(address));
		}
	}

	public final void storeArray_byte(final long tid, final long oid, final byte[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength(array.length);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_boolean(
		final long      tid  ,
		final long      oid  ,
		final boolean[] array
	)
	{
		final long totalByteLength = calculateBinaryListByteLength(array.length);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_short(final long tid, final long oid, final short[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Short.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_char(final long tid, final long oid, final char[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Character.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_int(final long tid, final long oid, final int[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Integer.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_float(final long tid, final long oid, final float[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Float.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_long(final long tid, final long oid, final long[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Long.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}

	public final void storeArray_double(final long tid, final long oid, final double[] array)
	{
		final long totalByteLength = calculateBinaryListByteLength((long)array.length * Double.BYTES);
		final long storeAddress    = this.storeEntityHeader(totalByteLength, tid, oid);

		XMemory.set_long(binaryListByteLengthAddress(storeAddress), totalByteLength);
		XMemory.set_long(binaryListElementCountAddress(storeAddress), array.length);
		XMemory.copyArrayToAddress(
			array,
			binaryListElementsAddressAbsolute(storeAddress)
		);
	}
	

	public static final void storeByte(final Binary bytes, final long tid, final long oid, final byte value)
	{
		XMemory.set_byte(bytes.storeEntityHeader(Byte.BYTES, tid, oid), value);
	}

	public static final void storeBoolean(final Binary bytes, final long tid, final long oid, final boolean value)
	{
		// where is Boolean.BYTES? Does a boolean not have a binary size? JDK pros... .
		XMemory.set_boolean(bytes.storeEntityHeader(Byte.BYTES, tid, oid), value);
	}

	public static final void storeShort(final Binary bytes, final long tid, final long oid, final short value)
	{
		XMemory.set_short(bytes.storeEntityHeader(Short.BYTES, tid, oid), value);
	}

	public static final void storeCharacter(final Binary bytes, final long tid, final long oid, final char value)
	{
		XMemory.set_char(bytes.storeEntityHeader(Character.BYTES, tid, oid), value);
	}

	public static final void storeInteger(final Binary bytes, final long tid, final long oid, final int value)
	{
		XMemory.set_int(bytes.storeEntityHeader(Integer.BYTES, tid, oid), value);
	}

	public static final void storeFloat(final Binary bytes, final long tid, final long oid, final float value)
	{
		XMemory.set_float(bytes.storeEntityHeader(Float.BYTES, tid, oid), value);
	}

	public static final void storeLong(final Binary bytes, final long tid, final long oid, final long value)
	{
		XMemory.set_long(bytes.storeEntityHeader(Long.BYTES, tid, oid), value);
	}

	public static final void storeDouble(final Binary bytes, final long tid, final long oid, final double value)
	{
		XMemory.set_double(bytes.storeEntityHeader(Double.BYTES, tid, oid), value);
	}

	public final void storeStateless(final long tid, final long oid)
	{
		this.storeEntityHeader(0L, tid, oid); // so funny :D
	}

	public final void storeStringValue(final long tid, final long oid, final String string)
	{
		final char[] chars = XMemory.accessChars(string); // thank god they fixed that stupid String storage mess
		storeChars(
			this.storeEntityHeader(
				calculateBinaryLengthChars(chars.length),
				tid,
				oid
			),
			chars,
			0,
			chars.length
		);
	}

	public static final long storeArrayContentAsList(
		final Binary          bytes       ,
		final long            typeId      ,
		final long            objectId    ,
		final long            binaryOffset,
		final PersistenceFunction persister   ,
		final Object[]        array       ,
		final int             offset      ,
		final int             length
	)
	{
		final long contentAddress = bytes.storeEntityHeader(
			binaryOffset + Binary.calculateReferenceListTotalBinaryLength(array.length),
			typeId,
			objectId
		);

		storeArrayContentAsList(contentAddress + binaryOffset, persister, array, offset, length);

		return contentAddress;
	}

	// (29.01.2019 TM)FIXME: consolidate/transform into instance method
	public static final void storeArrayContentAsList(
		final long            storeAddress,
		final PersistenceFunction persister   ,
		final Object[]        array       ,
		final int             offset      ,
		final int             length
	)
	{
		final long elementsDataAddress = storeListHeader(
			storeAddress,
			BinaryPersistence.referenceBinaryLength(length),
			length
		);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			XMemory.set_long(elementsDataAddress + BinaryPersistence.referenceBinaryLength(i), persister.apply(array[i]));
		}
	}

	public static final long storeListHeader(
		final long storeAddress        ,
		final long elementsBinaryLength,
		final long elementsCount
	)
	{
		XMemory.set_long(storeAddress + LIST_OFFSET_BYTE_LENGTH, calculateBinaryListByteLength(elementsBinaryLength));
		XMemory.set_long(storeAddress + LIST_OFFSET_ELEMENT_COUNT , elementsCount);
		return storeAddress + LIST_OFFSET_ELEMENTS;
	}

	public static final void storeIterableContentAsList(
		final long            storeAddress,
		final PersistenceFunction persister   ,
		final Iterable<?>     elements    ,
		final long            elementCount
	)
	{
		final Iterator<?> iterator = elements.iterator();

		final long referenceLength     = BinaryPersistence.referenceBinaryLength(1);
		final long elementsBinaryRange = elementCount * referenceLength;
		final long elementsDataAddress = storeListHeader(storeAddress, elementsBinaryRange, elementCount);
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;

		long address = elementsDataAddress;

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		while(address < elementsBinaryBound && iterator.hasNext())
		{
			final Object element = iterator.next();
			XMemory.set_long(address, persister.apply(element));
			address += referenceLength;
		}

		/* if there are fewer elements than specified, it is an error just the same.
		 * The element count must match exactely, no matter what.
		 */
		if(address != elementsBinaryBound)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element count: specified " + elementCount
				+ " vs. iterated " + elementsBinaryBound / referenceLength
			);
		}
	}

	public static final void storeKeyValuesAsEntries(
		final long                               storeAddress,
		final PersistenceFunction                    persister   ,
		final Iterable<? extends KeyValue<?, ?>> elements    ,
		final long                               elementCount
	)
	{
		final Iterator<? extends KeyValue<?, ?>> iterator = elements.iterator();

		final long referenceLength = BinaryPersistence.referenceBinaryLength(1);

		// two references per entry
		final long entryLength = 2 * referenceLength;

		final long elementsBinaryRange = elementCount * entryLength;
		final long elementsDataAddress = storeListHeader(storeAddress, elementsBinaryRange, elementCount);
		final long elementsBinaryBound = elementsDataAddress + elementsBinaryRange;

		long address = elementsDataAddress;

		/*
		 * must check elementCount on every element because under no circumstances may the memory be set
		 * longer than the elementCount indicates (e.g. concurrent modification of the passed collection)
		 */
		while(address < elementsBinaryBound && iterator.hasNext())
		{
			final KeyValue<?, ?> element = iterator.next();
			XMemory.set_long(address                  , persister.apply(element.key())  );
			XMemory.set_long(address + referenceLength, persister.apply(element.value()));
			address += entryLength; // advance index for both in one step
		}

		/* if there are fewer elements than specified, it is an error just the same.
		 * The element count must match exactely, no matter what.
		 */
		if(address != elementsBinaryBound)
		{
			// (22.04.2016 TM)EXCP: proper exception
			throw new RuntimeException(
				"Inconsistent element count: specified " + elementCount
				+ " vs. iterated " + elementsBinaryBound / entryLength
			);
		}
	}



	public static final void storeStringsAsList(
		final long     storeAddress            ,
		final long     precalculatedContentBinaryLength,
		final String[] strings
	)
	{
		storeStringsAsList(storeAddress, precalculatedContentBinaryLength, strings, 0, strings.length);
	}

	public static final void storeStringsAsList(
		final long     storeAddress                    ,
		final long     precalculatedContentBinaryLength,
		final String[] strings                         ,
		final int      offset                          ,
		final int      length
	)
	{
		long elementsDataAddress = storeListHeader(storeAddress, precalculatedContentBinaryLength, length);

		// (23.02.2015 TM)NOTE: old
//		Memory.set_long(storeAddress + LIST_OFFSET_LENGTH, precalculatedTotalLength);
//		Memory.set_long(storeAddress + LIST_OFFSET_COUNT , length                  );
//		long elementDataAddress = storeAddress + LIST_OFFSET_ELEMENTS;

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			elementsDataAddress = storeChars(elementsDataAddress, XMemory.accessChars(strings[i]));
		}
	}

	public static final long storeChars(final long storeAddress, final char[] chars)
	{
		return storeChars(storeAddress, chars, 0, chars.length);
	}

	public static final long storeChars(
		final long   storeAddress,
		final char[] chars       ,
		final int    offset      ,
		final int    length
	)
	{
		// total binary length is header length plus content length
		final long elementsBinaryLength = length * XMemory.byteSize_char();
		final long elementsDataAddress  = storeListHeader(storeAddress, elementsBinaryLength, length);

		final int bound = offset + length;
		for(int i = offset; i < bound; i++)
		{
			XMemory.set_char(elementsDataAddress + (i << 1), chars[i]);
		}

		return elementsDataAddress + elementsBinaryLength;
	}
	

	public final void storeFixedSize(
		final PersistenceStoreHandler  handler      ,
		final long                     contentLength,
		final long                     typeId       ,
		final long                     objectId     ,
		final Object                   instance     ,
		final long[]                   memoryOffsets,
		final BinaryValueStorer[]      storers
	)
	{
		long address = this.storeEntityHeader(contentLength, typeId, objectId);
		for(int i = 0; i < memoryOffsets.length; i++)
		{
			address = storers[i].storeValueFromMemory(instance, memoryOffsets[i], address, handler);
		}
	}
	

	public static final long buildStrings(
		final Binary   bytes ,
		final long     offset,
		final String[] target
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: overhaul to only use Binary-relative offsets, not absolute addresses
		throw new net.jadoth.meta.NotImplementedYetError();
//		final long listAddress  = bytes.loadItemEntityContentAddress() + offset;
//		final long elementCount = Binary.getBinaryListElementCount(listAddress);
//
//		if(target.length != elementCount)
//		{
//			throw new RuntimeException(); // (22.10.2013 TM)EXCP: proper exception
//		}
//
//		long elementAddress = Binary.binaryListElementsAddressAbsolute(listAddress); // first element address
//		for(int i = 0; i < target.length; i++)
//		{
//			target[i] = XMemory.wrapCharsAsString(Binary.rawBuildArray_char(elementAddress)); // build element
//			elementAddress += Binary.getBinaryListByteLengthAbsolute(elementAddress); // scroll to next element
//		}
//
//		// as this is an offset-based public method, it must return an offset, not an absolute address
//		return elementAddress - bytes.loadItemEntityContentAddress();
	}
	
	// (29.01.2019 TM)FIXME: JET-49: old version, delete after overhaul
//	public static final long buildStrings(
//		final Binary   bytes ,
//		final long     offset,
//		final String[] target
//	)
//	{
//		final long listAddress  = bytes.loadItemEntityContentAddress() + offset;
//		final long elementCount = Binary.getBinaryListElementCount(listAddress);
//
//		if(target.length != elementCount)
//		{
//			throw new RuntimeException(); // (22.10.2013 TM)EXCP: proper exception
//		}
//
//		long elementAddress = Binary.binaryListElementsAddressAbsolute(listAddress); // first element address
//		for(int i = 0; i < target.length; i++)
//		{
//			target[i] = XMemory.wrapCharsAsString(Binary.rawBuildArray_char(elementAddress)); // build element
//			elementAddress += Binary.getBinaryListByteLengthAbsolute(elementAddress); // scroll to next element
//		}
//
//		// as this is an offset-based public method, it must return an offset, not an absolute address
//		return elementAddress - bytes.loadItemEntityContentAddress();
//	}

	public static final Byte buildByte(final Binary bytes)
	{
		return new Byte(XMemory.get_byte(bytes.loadItemEntityContentAddress()));
	}

	public static final Boolean buildBoolean(final Binary bytes)
	{
		return new Boolean(XMemory.get_boolean(null, bytes.loadItemEntityContentAddress()));
	}

	public static final Short buildShort(final Binary bytes)
	{
		return new Short(XMemory.get_short(bytes.loadItemEntityContentAddress()));
	}

	public static final Character buildCharacter(final Binary bytes)
	{
		return new Character(XMemory.get_char(bytes.loadItemEntityContentAddress()));
	}

	public static final Integer buildInteger(final Binary bytes)
	{
		return new Integer(XMemory.get_int(bytes.loadItemEntityContentAddress()));
	}

	public static final Float buildFloat(final Binary bytes)
	{
		return new Float(XMemory.get_float(bytes.loadItemEntityContentAddress()));
	}

	public static final Long buildLong(final Binary bytes)
	{
		return new Long(XMemory.get_long(bytes.loadItemEntityContentAddress()));
	}

	public static final Double buildDouble(final Binary bytes)
	{
		return new Double(XMemory.get_double(bytes.loadItemEntityContentAddress()));
	}
	
	public static final byte[] buildArray_byte(final Binary bytes)
	{
		final long elementCount = getBinaryListElementCount(bytes.loadItemEntityContentAddress());
		final byte[] array;
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array = new byte[X.checkArrayRange(elementCount)]
		);
		return array;
	}

	public final char[] buildArray_char()
	{
		return rawBuildArray_char(this.loadItemEntityContentAddress());
	}

	public final char[] buildArray_char(final long addressOffset)
	{
		return rawBuildArray_char(this.loadItemEntityContentAddress() + addressOffset);
	}

	static final char[] rawBuildArray_char(final long valueAddress)
	{
		final long elementCount = getBinaryListElementCount(valueAddress);
		final char[] array;
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(valueAddress),
			array = new char[X.checkArrayRange(elementCount)]
		);
		return array;
	}

	// array restrukturierung marker //

	public static final byte[] createArray_byte(final Binary bytes)
	{
		return new byte[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_byte(final byte[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final boolean[] createArray_boolean(final Binary bytes)
	{
		return new boolean[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_boolean(final boolean[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final short[] createArray_short(final Binary bytes)
	{
		return new short[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_short(final short[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final char[] createArray_char(final Binary bytes)
	{
		return new char[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_char(final char[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final int[] createArray_int(final Binary bytes)
	{
		return new int[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_int(final int[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final float[] createArray_float(final Binary bytes)
	{
		return new float[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_float(final float[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public static final long[] createArray_long(final Binary bytes)
	{
		return new long[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_long(final long[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()), array);
	}

	public static final double[] createArray_double(final Binary bytes)
	{
		return new double[X.checkArrayRange(getBinaryListElementCount(bytes.loadItemEntityContentAddress()))];
	}

	public static final void updateArray_double(final double[] array, final Binary bytes)
	{
		XMemory.copyRangeToArray(
			binaryListElementsAddressAbsolute(bytes.loadItemEntityContentAddress()),
			array
		);
	}

	public final String buildString()
	{
		// perfectly reasonable example use of the wrapping method.
		return XMemory.wrapCharsAsString(this.buildArray_char());
	}



		
	// special crazy sh*t negative offsets
	private static final long
		CONTENT_ADDRESS_NEGATIVE_OFFSET_TID = BinaryPersistence.OFFSET_TID - BinaryPersistence.LENGTH_ENTITY_HEADER,
		CONTENT_ADDRESS_NEGATIVE_OFFSET_OID = BinaryPersistence.OFFSET_OID - BinaryPersistence.LENGTH_ENTITY_HEADER
	;
	
	// (25.01.2019 TM)FIXME: JET-49: should not be public and won't be after moving the logic to Binary
	@Deprecated
	public static final long contentAddressNegativeOffsetOid()
	{
		return CONTENT_ADDRESS_NEGATIVE_OFFSET_OID;
	}
	
	public final long getBuildItemContentLength()
	{
		return XMemory.get_long(this.loadItemEntityContentAddress() - BinaryPersistence.LENGTH_ENTITY_HEADER)
			- BinaryPersistence.LENGTH_ENTITY_HEADER
		;
	}
	
	public final long getBuildItemTypeId()
	{
		return XMemory.get_long(this.loadItemEntityContentAddress() + CONTENT_ADDRESS_NEGATIVE_OFFSET_TID);
	}

	public final long getBuildItemObjectId()
	{
		return XMemory.get_long(this.loadItemEntityContentAddress() + CONTENT_ADDRESS_NEGATIVE_OFFSET_OID);
	}
	

	public static final <T> void updateInstanceReferences(
		final Binary                    bytes           ,
		final T                         instance        ,
		final int[]                     referenceOffsets,
		final PersistenceObjectIdResolver oidResolver
	)
	{
		final long referencesBinaryOffset = bytes.loadItemEntityContentAddress();
		for(int i = 0; i < referenceOffsets.length; i++)
		{
			XMemory.setObject(
				instance,
				referenceOffsets[i],
				oidResolver.lookupObject(
					XMemory.get_long(referencesBinaryOffset + BinaryPersistence.referenceBinaryLength(i))
				)
			);
		}
	}


	public final void updateArrayObjectReferences(
		final long                        binaryOffset,
		final PersistenceObjectIdResolver oidResolver ,
		final Object[]                    array       ,
		final int                         offset      ,
		final int                         length
	)
	{
		final long elementCount = this.getListElementCountReferences(binaryOffset);
		if(elementCount < length)
		{
			throw new BinaryPersistenceExceptionStateArrayLength(
				array,
				X.checkArrayRange(elementCount)
			);
		}
		
		final long binaryElementsStartAddress = this.binaryListElementsAddressRelative(binaryOffset);
		for(int i = 0; i < length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			array[offset + i] = oidResolver.lookupObject(
				XMemory.get_long(binaryElementsStartAddress + BinaryPersistence.referenceBinaryLength(i))
			);
		}
	}

	public static final void collectElementsIntoArray(
		final Binary                    bytes       ,
		final long                      binaryOffset,
		final PersistenceObjectIdResolver oidResolver ,
		final Object[]                  target
	)
	{
		final long binaryElementsStartAddress = bytes.binaryListElementsAddressRelative(binaryOffset);
		for(int i = 0; i < target.length; i++)
		{
			// bounds-check eliminated array setting has about equal performance as manual unsafe putting
			target[i] = oidResolver.lookupObject(
				XMemory.get_long(binaryElementsStartAddress + BinaryPersistence.referenceBinaryLength(i))
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
		final long                      binaryOffset,
		final int                       length      ,
		final PersistenceObjectIdResolver oidResolver ,
		final Consumer<Object>          collector
	)
	{
		final long binaryElementsStartAddress = this.binaryListElementsAddressRelative(binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				oidResolver.lookupObject(
					XMemory.get_long(binaryElementsStartAddress + BinaryPersistence.referenceBinaryLength(i))
				)
			);
		}
	}

	public static final int collectKeyValueReferences(
		final Binary                     bytes       ,
		final long                       binaryOffset,
		final int                        length      ,
		final PersistenceObjectIdResolver  oidResolver ,
		final BiConsumer<Object, Object> collector
	)
	{
		final long binaryElementsStartAddress = bytes.binaryListElementsAddressRelative(binaryOffset);
		for(int i = 0; i < length; i++)
		{
			collector.accept(
				// key (on every nth oid position)
				oidResolver.lookupObject(
					XMemory.get_long(binaryElementsStartAddress + BinaryPersistence.referenceBinaryLength(i << 1))
				),
				// value (on every (n + 1)th oid position)
				oidResolver.lookupObject(
					XMemory.get_long(binaryElementsStartAddress + BinaryPersistence.referenceBinaryLength(i << 1) + BinaryPersistence.oidByteLength())
				)
			);
		}
		return length;
	}

	
	public static final short get_short(final Binary bytes, final long offset)
	{
		return XMemory.get_short(bytes.loadItemEntityContentAddress() + offset);
	}

	public static final float get_float(final Binary bytes, final long offset)
	{
		return XMemory.get_float(bytes.loadItemEntityContentAddress() + offset);
	}

	public static final int get_int(final Binary bytes, final long offset)
	{
		return XMemory.get_int(bytes.loadItemEntityContentAddress() + offset);
	}

	/* (28.10.2013 TM)XXX: move all these xxx(Binary, ...) methods to Binary class directly? Possible objections?
	 * Aaan that's where JET-49 comes in.
	 * Only 5 years later.
	 */
	public static final long get_long(final Binary bytes, final long offset)
	{
		return XMemory.get_long(bytes.loadItemEntityContentAddress() + offset);
	}
	
}
//CHECKSTYLE.ON: AbstractClassName
