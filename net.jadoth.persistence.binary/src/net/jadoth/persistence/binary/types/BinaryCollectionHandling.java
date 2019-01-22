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
		final Binary          bytes       ,
		final long            tid         ,
		final long            oid         ,
		final long            headerOffset,
		final Object[]        array       ,
		final int             size        ,
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
		final PersistenceFunction                    persister
	)
	{
		// store entity header including the complete content size (headerOffset + entries)
		final long contentAddress = bytes.storeEntityHeader(
			headerOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(keyValueReferenceCount(size)),
			tid,
			oid
		);

		// store entries
		BinaryPersistence.storeKeyValuesAsEntries(contentAddress + headerOffset, persister, keyValues, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
	}
	
	public static long keyValueReferenceCount(final long elementCount)
	{
		// obviously 2 references: the key and the value.
		return elementCount * 2;
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
		final int size = X.checkArrayRange(getSizedArrayElementCount(bytes, headerOffset));
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

	public static final void copyContent(final Object[] source, final Object[] target, final int size)
	{
		System.arraycopy(source, 0, target, 0, size);
		for(int i = size; i < target.length; i++)
		{
			target[i] = null;
		}
	}

	public static final int getSizedArrayLength(final Binary bytes, final long headerOffset)
	{
		/* (23.11.2018 TM)FIXME: Security: prevent array bombs
		 * A forged binary form array could specifiy a max integer length despite actually
		 * having very few or even no elements. The naive code below returns the array size
		 * in good faith without checking its validity.
		 * A validation would be very simple: the specified element count times the array element size
		 * plus the header length must yield the total binary length of the binary form array.
		 * Meaning in order for the receiver to accept and creae a 2 billion element array, there actually has to
		 * be data for 2 billion elements.
		 * Of course an attacker could still send an actual 2-billion-elements array, but it wouldn't be much
		 * of an attack and it could be easily recognized and denied (e.g. no chunk received from outside
		 * may be longer than X bytes.)
		 */
		return X.checkArrayRange(
			XMemory.get_long(bytes.buildItemAddress() + headerOffset + SIZED_ARRAY_OFFSET_LENGTH)
		);
	}
	
	public static final long getListElementCount(
		final Binary bytes          ,
		final long   listStartOffset
	)
	{
		return BinaryPersistence.getListElementCountNEW(
			BinaryPersistence.entityAddressFromContentAddress(bytes.entityContentAddress),
			listStartOffset,
			keyValueReferenceCount(1)
		);
	}

	public static final long getSizedArrayElementsAddress(final Binary bytes, final long headerOffset)
	{
		return BinaryPersistence.getListElementsAddress(bytes, headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public static final long getSizedArrayElementCount(final Binary bytes, final long headerOffset)
	{
		return BinaryPersistence.getListElementCount(bytes, headerOffset + SIZED_ARRAY_OFFSET_ELEMENTS);
	}

	public static final void validateArrayLength(final Object[] array, final Binary bytes, final long headerOffset)
	{
		if(array.length == BinaryPersistence.getListElementCount(bytes, headerOffset))
		{
			return;
		}
		throw new BinaryPersistenceExceptionStateArrayLength(
			array,
			X.checkArrayRange(BinaryPersistence.getListElementCount(bytes, headerOffset))
		);
	}

	public static final void iterateSizedArrayElementReferences(
		final Binary         bytes   ,
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		BinaryPersistence.iterateListElementReferencesAtAddress(
			BinaryPersistence.getListElementsAddress(bytes, offset + SIZED_ARRAY_OFFSET_ELEMENTS),
			BinaryPersistence.getListElementCount(bytes, offset + SIZED_ARRAY_OFFSET_ELEMENTS),
			iterator
		);
	}

	public static final void iterateKeyValueEntriesReferences(
		final Binary         bytes   ,
		final long           offset  ,
		final _longProcedure iterator
	)
	{
		// the *2 is design-wise a bit hacky but technically absolutely correct
		BinaryPersistence.iterateListElementReferencesAtAddress(
			BinaryPersistence.getListElementsAddress(bytes, offset),
			BinaryPersistence.getListElementCount(bytes, offset) * 2,
			iterator
		);
	}



	private BinaryCollectionHandling()
	{
		// static only
		throw new UnsupportedOperationException();
	}

}
