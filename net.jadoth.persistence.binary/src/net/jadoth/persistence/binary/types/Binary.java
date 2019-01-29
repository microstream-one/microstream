package net.jadoth.persistence.binary.types;

import java.nio.ByteBuffer;

import net.jadoth.X;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.typing.KeyValue;

// CHECKSTYLE.OFF: AbstractClassName: this is kind of a hacky solution to improve readability on the use site
public abstract class Binary implements Chunk
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////
	
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
	
	private static final long KEY_VALUE_BINARY_LENGTH = KEY_VALUE_REFERENCE_COUNT * BinaryPersistence.oidLength();
	
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
	
	public abstract long storeSizedKeyValuesAsEntries(
		long                               tid         ,
		long                               oid         ,
		long                               headerOffset,
		Iterable<? extends KeyValue<?, ?>> keyValues   ,
		long                               size        ,
		PersistenceFunction                persister
	);
	
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
			headerOffset + SIZED_ARRAY_LENGTH_HEADER + BinaryPersistence.calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store specific header (only consisting of array capacity value)
		XMemory.set_long(contentAddress + headerOffset + SIZED_ARRAY_OFFSET_LENGTH, array.length);

		// store content: array content up to size, trailing nulls are cut off.
		BinaryPersistence.storeArrayContentAsList(
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
			headerOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store elements
		BinaryPersistence.storeIterableContentAsList(contentAddress + headerOffset, persister, elements, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}


		

	
	public int getSizedArrayElementCount(final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		return X.checkArrayRange(BinaryPersistence.getBinaryListElementCountValidating(
			this                                      ,
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			BinaryPersistence.oidLength()
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
		
		BinaryPersistence.updateArrayObjectReferences(
			this,
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
		
		return BinaryPersistence.binaryListElementsAddress(this, headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public final void validateArrayLength(final Object[] array, final long headerOffset)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		if(array.length == BinaryPersistence.getListElementCountReferences(this, headerOffset))
		{
			return;
		}
		
		throw new BinaryPersistenceExceptionStateArrayLength(
			array,
			X.checkArrayRange(BinaryPersistence.getListElementCountReferences(this, headerOffset))
		);
	}

	public final void iterateSizedArrayElementReferences(
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		// (29.01.2019 TM)FIXME: JET-49: offset validation
		
		final long elementCount = BinaryPersistence.getBinaryListElementCountValidating(
			this,
			offset + SIZED_ARRAY_OFFSET_ELEMENTS,
			BinaryPersistence.oidLength()
		);
		
		BinaryPersistence.iterateReferenceRange(
			BinaryPersistence.binaryListElementsAddress(this, offset + SIZED_ARRAY_OFFSET_ELEMENTS),
			elementCount,
			iterator
		);
	}
	
}
//CHECKSTYLE.ON: AbstractClassName
