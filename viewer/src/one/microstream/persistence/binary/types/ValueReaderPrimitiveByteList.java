package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderPrimitiveByteList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderPrimitiveByteList(final PersistenceTypeDefinitionMember typeDefinition)
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
			values[i] = binary.read_byte(listOffset);
			listOffset += BinaryPersistence.resolveFieldBinaryLength(byte.class);
		}

		return values;
	}
}