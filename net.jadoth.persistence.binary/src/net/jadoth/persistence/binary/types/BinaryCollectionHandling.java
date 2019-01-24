package net.jadoth.persistence.binary.types;

import net.jadoth.X;
import net.jadoth.collections.XArrays;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.memory.XMemory;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.types.PersistenceFunction;
import net.jadoth.persistence.types.PersistenceLoadHandler;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import net.jadoth.typing.KeyValue;

public final class BinaryCollectionHandling
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



	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> sizedArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return elementsPseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.pseudoField(long.class, "capacity")
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> elementsPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerNative.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.complex("elements",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "element")
				)
			)
		);
	}

	public static final XGettingSequence<? extends PersistenceTypeDefinitionMemberPseudoField> simpleArrayPseudoFields(
		final PersistenceTypeDefinitionMemberPseudoField... preHeaderFields)
	{
		return AbstractBinaryHandlerNative.pseudoFields(
			XArrays.add(
				preHeaderFields,
				AbstractBinaryHandlerNative.complex("elements",
					AbstractBinaryHandlerNative.pseudoField(Object.class, "element")
				)
			)
		);
	}



	public static final long storeSizedArray(
		final Binary              bytes       ,
		final long                tid         ,
		final long                oid         ,
		final long                headerOffset,
		final Object[]            array       ,
		final int                 size        ,
		final PersistenceFunction persister
	)
	{
		// store entity header including the complete content size (8 + elements)
		final long contentAddress = bytes.storeEntityHeader(
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

	public static final long storeSizedIterableAsList(
		final Binary          bytes       ,
		final long            tid         ,
		final long            oid         ,
		final long            headerOffset,
		final Iterable<?>     elements    ,
		final long            size        ,
		final PersistenceFunction persister
	)
	{
		// store entity header including the complete content size (headerOffset + elements)
		final long contentAddress = bytes.storeEntityHeader(
			headerOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store elements
		BinaryPersistence.storeIterableContentAsList(contentAddress + headerOffset, persister, elements, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}

	public static final long storeSizedKeyValuesAsEntries(
		final Binary                             bytes       ,
		final long                               tid         ,
		final long                               oid         ,
		final long                               headerOffset,
		final Iterable<? extends KeyValue<?, ?>> keyValues   ,
		final long                               size        ,
		final PersistenceFunction                persister
	)
	{
		// store entity header including the complete content size (headerOffset + entries)
		final long contentAddress = bytes.storeEntityHeader(
			headerOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(size * keyValueReferenceCount()),
			tid,
			oid
		);

		// store entries
		BinaryPersistence.storeKeyValuesAsEntries(contentAddress + headerOffset, persister, keyValues, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}
	
	public static final void copyContent(final Object[] source, final Object[] target, final int size)
	{
		System.arraycopy(source, 0, target, 0, size);
		for(int i = size; i < target.length; i++)
		{
			target[i] = null;
		}
	}
	
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
	
	public static int getSizedArrayElementCount(final Binary bytes, final long headerOffset)
	{
		return X.checkArrayRange(BinaryPersistence.getBinaryListElementCountValidating(
			bytes                                     ,
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
	public static final int updateSizedArrayObjectReferences(
		final Binary                 bytes       ,
		final long                   headerOffset,
		final Object[]               array       ,
		final PersistenceLoadHandler handler
	)
	{
		final int size = getSizedArrayElementCount(bytes, headerOffset);
		if(array.length < size)
		{
			throw new IllegalArgumentException(); // (23.10.2013 TM)EXCP: proper exception
		}
		
		BinaryPersistence.updateArrayObjectReferences(
			bytes,
			headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS,
			handler,
			array,
			0,
			size
		);
		
		return size;
	}

	public static final int getSizedArrayLength(final Binary bytes, final long sizedArrayOffset)
	{
		/* Note on validation for "array bombs" prevention
		 * (see BinaryPersistence#getBinaryListElementCountValidating)
		 * That kind of validation cannot be done here.
		 * Consider the following scenario, which would be perfectly correct:
		 * - An ArrayList is created with max int capacity and only one element (size is 1).
		 * - That ArrayList instance is serialized.
		 * - The array length value in binary form would be max int, the following binary list would contain only 1 element.
		 * - This means the resulting total length would indeed be tiny (currently 56 bytes).
		 * - The ArrayList instance created from that information would, however, be around 8/16 GB in size.
		 * All that would be perfectly correct. It is just an incredibly efficient binary form compression that cannot
		 * be validated against the binary form of the sent instance.
		 * 
		 * This means the actual security rationale at this point is:
		 * Are external senders allowed to send "big" instances?
		 * If they are, they must be trustworthy or controlled otherwise to not cause harm.
		 * 
		 * Should this cause problems, there are a number of actions that can be taken to prevent harm:
		 * 1.) Specifically deny all types that are known to contain sized arrays (size and capacity).
		 * 2.) Or write handlers for them that ignore the provided array length but always allocate an array
		 *     that is "just" big enough to hold the sent data. However, caution: This can alter program behavior.
		 * 3.) Handle out of memory errors by terminating the communication, thus releasing the occupied memory.
		 * 
		 */
		return X.checkArrayRange(
			XMemory.get_long(bytes.entityContentAddress() + sizedArrayOffset + SIZED_ARRAY_OFFSET_LENGTH)
		);
	}
	
	public static final long getListElementCountKeyValue(
		final Binary bytes          ,
		final long   listStartOffset
	)
	{
		return BinaryPersistence.getBinaryListElementCountValidating(
			bytes,
			listStartOffset,
			keyValueBinaryLength()
		);
	}

	public static final long getSizedArrayElementsAddress(final Binary bytes, final long headerOffset)
	{
		return BinaryPersistence.binaryListElementsAddress(bytes, headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public static final void validateArrayLength(final Object[] array, final Binary bytes, final long headerOffset)
	{
		if(array.length == BinaryPersistence.getListElementCountReferences(bytes, headerOffset))
		{
			return;
		}
		throw new BinaryPersistenceExceptionStateArrayLength(
			array,
			X.checkArrayRange(BinaryPersistence.getListElementCountReferences(bytes, headerOffset))
		);
	}

	public static final void iterateSizedArrayElementReferences(
		final Binary         bytes   ,
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		final long elementCount = BinaryPersistence.getBinaryListElementCountValidating(
			bytes,
			offset + SIZED_ARRAY_OFFSET_ELEMENTS,
			BinaryPersistence.oidLength()
		);
		
		BinaryPersistence.iterateReferenceRange(
			BinaryPersistence.binaryListElementsAddress(bytes, offset + SIZED_ARRAY_OFFSET_ELEMENTS),
			elementCount,
			iterator
		);
	}

	public static final void iterateKeyValueEntriesReferences(
		final Binary         bytes   ,
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		final long elementCount = BinaryPersistence.getBinaryListElementCountValidating(
			bytes,
			offset,
			keyValueBinaryLength()
		);

		BinaryPersistence.iterateReferenceRange(
			BinaryPersistence.binaryListElementsAddress(bytes, offset),
			keyValueReferenceCount() * elementCount,
			iterator
		);
	}



	private BinaryCollectionHandling()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
