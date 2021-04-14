package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.binary.types.BinaryPersistence;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderPrimitiveCharList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderPrimitiveCharList(final PersistenceTypeDefinitionMember typeDefinition)
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

		final Object[] values = new Object[elementCount];
		for(int i = 0; i < elementCount; i++)
		{
			values[i] = binary.read_char(listOffset);
			listOffset += BinaryPersistence.resolveFieldBinaryLength(char.class);
		}

		return values;

	}
}
