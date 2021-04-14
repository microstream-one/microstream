package one.microstream.storage.restadapter.types;

import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;

public class ValueReaderStringList extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderStringList(final PersistenceTypeDefinitionMember member)
	{
		super(member);
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{
		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final String strings[] = new String[elementCount];

		for(int i = 0; i < elementCount; i++)
		{
			final long stringLength = binary.getBinaryListTotalByteLength(listOffset);
			final char[] array = binary.build_chars(listOffset);

			listOffset += stringLength;
			strings[i] = new String(array);
		}

		return strings;
	}
}
