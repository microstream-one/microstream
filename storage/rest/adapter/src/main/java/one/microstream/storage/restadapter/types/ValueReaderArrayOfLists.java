package one.microstream.storage.restadapter.types;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDefinitionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public class ValueReaderArrayOfLists extends ValueReaderVariableLength
{
	///////////////////////////////////////////////////////////////////////////
	// instance fields //
	////////////////////

	private final ValueReader readers[];

	///////////////////////////////////////////////////////////////////////////
	// constructors //
	/////////////////

	public ValueReaderArrayOfLists(final PersistenceTypeDefinitionMember typeDefinition)
	{
		super(typeDefinition);

		final PersistenceTypeDefinitionMemberFieldGenericComplex.Default instance = (PersistenceTypeDefinitionMemberFieldGenericComplex.Default) typeDefinition;
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> instanceMembers = instance.members();

		this.readers = new ValueReader[instanceMembers.intSize()];
		for(int i = 0; i< instanceMembers.intSize(); i++)
		{
			this.readers[i] = ValueReader.deriveValueReader((PersistenceTypeDefinitionMember) instanceMembers.at(i));
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// methods //
	////////////

	@Override
	public Object readValue(final Binary binary, final long offset)
	{

		long listOffset = Binary.toBinaryListElementsOffset(offset);
		final int elementCount = (int) binary.getBinaryListElementCountUnvalidating(offset);

		final Object lists[] = new Object[elementCount];

		for(int j = 0; j < elementCount; j++)
		{
			final Object[] objectValues = new Object[this.readers.length];
			for(int i = 0; i < this.readers.length; i++)
			{
				objectValues[i] = this.readers[i].readValue(binary, listOffset);
				final long size = this.readers[i].getBinarySize(binary, listOffset);
				listOffset += size;
			}

			lists[j] = objectValues;
		}

		return lists;
	}
}
