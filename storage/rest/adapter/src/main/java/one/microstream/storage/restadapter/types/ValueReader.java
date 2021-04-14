package one.microstream.storage.restadapter.types;

import java.util.ArrayList;
import java.util.List;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.persistence.binary.types.Binary;
import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDefinitionMember;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGenericComplex;
import one.microstream.persistence.types.PersistenceTypeDictionary.Symbols;

public interface ValueReader
{
	///////////////////////////////////////////////////////////////////////////
	// interface methods //
	////////////

	public Object readValue(Binary binary, long offset);
	public long getBinarySize(final Binary binary, final long offset);

	public default long getVariableLength(final Binary binary, final long offset)
	{
		return -1;
	}


	///////////////////////////////////////////////////////////////////////////
	// static methods //
	///////////////////

	public static ValueReader[] deriveValueReaders(final PersistenceTypeDefinition td)
	{
		final XGettingSequence<? extends PersistenceTypeDefinitionMember> members = td.instanceMembers();
		final ValueReader[] valueReaders = new ValueReader[members.intSize()];

		int i = 0;
		for(final PersistenceTypeDefinitionMember member : td.instanceMembers())
		{
			valueReaders[i++] = deriveValueReader(member);
		}

		return valueReaders;
	}

	public static ValueReader deriveValueReader(final PersistenceTypeDefinitionMember member)
	{
		/*
		 * select from a variety of stateless ValueReader implementations:
		 * - wrap a primitive value in an instance of its respective Wrapper type
		 * - wrap a reference in a generic "Reference" type holding the objectId (for displaying later)
		 * - wrap a "[char]" member's data in a String
		 * - wrap a "[list]" member's data in a primitive array if there's only a single primitive nested member
		 * - wrap a "[list]" member's data in an Object[] if there's only a single non-primitive nested member
		 * - wrap a "[list]" member's data in an Object[][] if there's more than one nested member.
		 *
		 * Note that non-referential native types (primitive wrappers, String, Date, primitive arrays, etc.) should
		 * be handled by their TypeHandler directly instead of analyzed generically.
		 */

		if(member.isPrimitive())
		{
			return new ValueReaderPrimitive(member);
		}

		if(member.isReference())
		{
			return new ValueReaderReference();
		}

		if(member.isVariableLength())
		{
			return deriveVariableLengthValueReader(member);
		}

		throw new one.microstream.meta.NotImplementedYetError();
	}

	public static ValueReader deriveVariableLengthValueReader(final PersistenceTypeDefinitionMember member)
	{

		if(member.typeName().contentEquals(Symbols.typeChars()))
		{
			return new ValueReaderPrimitiveCharList(member);
		}

		if(member.typeName().contentEquals(Symbols.typeBytes()))
		{
			return new ValueReaderPrimitiveByteList(member);
		}

		if(member.typeName().contentEquals(Symbols.typeComplex()))
		{
			return deriveComplexVariableLengthValueReader(member);
		}

		throw new one.microstream.meta.NotImplementedYetError();

	}

	public static ValueReader deriveComplexVariableLengthValueReader(final PersistenceTypeDefinitionMember member)
	{
		final PersistenceTypeDescriptionMemberFieldGenericComplex memberComplex = (PersistenceTypeDescriptionMemberFieldGenericComplex) member;
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members = memberComplex.members();

		if(members.size() == 1)
		{
			final PersistenceTypeDescriptionMemberFieldGeneric listMember = members.first();

			if(listMember.isReference())
			{
				return new ValueReaderReferenceList(member);
			}

			if(listMember.isPrimitive())
			{
				return new ValueReaderPrimitiveList((PersistenceTypeDefinitionMember) listMember);
			}

			if(listMember.typeName().contentEquals(Symbols.typeChars()))
			{
				return new ValueReaderStringList((PersistenceTypeDefinitionMember) listMember);
			}

			if(listMember.typeName().contentEquals(Symbols.typeBytes()))
			{
				return new ValueReaderPrimitiveByteList((PersistenceTypeDefinitionMember) listMember);
			}
		}
		else
		{
			return new ValueReaderArrayOfLists(member);
		}

		throw new one.microstream.meta.NotImplementedYetError();
	}

	public static void readObjectValues(
			final Binary        binary      ,
			final ValueReader[] valueReaders,
			final long[]        valueOffsets,
			final ObjectDescription  objectDescription)
	{
		final Object[] objectValues = new Object[valueReaders.length];

		final List<Long> variableLength = new ArrayList<>();
		long offset = 0;
		for(int i = 0; i < objectValues.length; i++)
		{
			objectValues[i] = valueReaders[i].readValue(binary, offset);
			final long size = valueReaders[i].getBinarySize(binary, offset);
			final long variableSize = valueReaders[i].getVariableLength(binary, offset);
			offset += size;

			if(variableSize > -1)
			{
				variableLength.add(variableSize);
			}
		}

		objectDescription.setLength(objectValues.length - variableLength.size());
		objectDescription.setVariableLength(variableLength.size() > 0 ? variableLength.toArray(new Long[0]) : null);


		objectDescription.setValues(objectValues);
	}
}
