package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderReferenceList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderReferenceList(final PersistenceTypeDefinitionMember typeDefinition)
	{
		super(typeDefinition);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{
		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final Object references[] = new Object[elementCount];
		for(int i = 0; i < elementCount; i++)
		{
			references[i] = new ObjectReferenceWrapper(ViewerBinaryPrimitivesReader.readReference(binary, listOffset));
			listOffset += Binary.objectIdByteLength();
		}

		return references;
	}

	@Override
	public long getBinarySize(final Binary binary, final long offset)
	{
		return binary.getBinaryListTotalByteLength(offset);
	}

}
