package net.jadoth.persistence.binary.types;

import net.jadoth.X;
import net.jadoth.collections.XArrays;
import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.functional._longProcedure;
import net.jadoth.low.XVM;
import net.jadoth.persistence.binary.exceptions.BinaryPersistenceExceptionStateArrayLength;
import net.jadoth.persistence.binary.internal.AbstractBinaryHandlerNative;
import net.jadoth.persistence.types.PersistenceTypeDefinitionMemberPseudoField;
import net.jadoth.swizzling.types.SwizzleBuildLinker;
import net.jadoth.swizzling.types.SwizzleFunction;
import net.jadoth.typing.KeyValue;

public final class BinaryCollectionHandling
{
	///////////////////////////////////////////////////////////////////////////
	// constants        //
	/////////////////////

	/* sized array binary layout:
	 * [entity header][8 byte array length][list of elements without arrays' trailing nulls]
	 */
	private static final long
		SIZED_ARRAY_OFFSET_LENGTH   = 0L                       , // length is the first (and only) header value
		SIZED_ARRAY_LENGTH_HEADER   = XVM.byteSize_long()   , // header only consists of length
		SIZED_ARRAY_OFFSET_ELEMENTS = SIZED_ARRAY_LENGTH_HEADER  // element list begins after header
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
		final SwizzleFunction persister
	)
	{
		// store entity header including the complete content size (8 + elements)
		final long contentAddress = bytes.storeEntityHeader(
			headerOffset + SIZED_ARRAY_LENGTH_HEADER + BinaryPersistence.calculateReferenceListTotalBinaryLength(size),
			tid,
			oid
		);

		// store specific header (only consisting of array capacity value)
		XVM.set_long(contentAddress + headerOffset + SIZED_ARRAY_OFFSET_LENGTH, array.length);

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
		final SwizzleFunction persister
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
		final SwizzleFunction                    persister
	)
	{
		// store entity header including the complete content size (headerOffset + entries)
		final long contentAddress = bytes.storeEntityHeader(
			headerOffset + BinaryPersistence.calculateReferenceListTotalBinaryLength(size * 2),
			tid,
			oid
		);

		// store entries
		BinaryPersistence.storeKeyValuesAsEntries(contentAddress + headerOffset, persister, keyValues, size);

		// return contentAddress to allow calling context to fill in 'headerOffset' amount of bytes
		return contentAddress;
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
		final Binary         bytes       ,
		final long           headerOffset,
		final Object[]       array       ,
		final SwizzleBuildLinker builder
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
			builder,
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
		return X.checkArrayRange(
			XVM.get_long(bytes.buildItemAddress() + headerOffset + SIZED_ARRAY_OFFSET_LENGTH)
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
