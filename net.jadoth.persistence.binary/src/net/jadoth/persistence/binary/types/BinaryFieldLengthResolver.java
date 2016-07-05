package net.jadoth.persistence.binary.types;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.memory.Memory;
import net.jadoth.persistence.types.PersistenceFieldLengthResolver;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMemberPseudoField;

public interface BinaryFieldLengthResolver extends PersistenceFieldLengthResolver
{
	@Override
	public default long resolveMinimumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return Memory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long resolveMaximumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return Memory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long variableLengthTypeMinimumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return BinaryPersistence.binaryArrayMinimumLength();
	}

	@Override
	public default long variableLengthTypeMaximumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return BinaryPersistence.binaryArrayMaximumLength();
	}

	@Override
	public default long resolveComplexMemberMinimumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> nestedMembers
	)
	{
		return BinaryPersistence.binaryArrayMinimumLength();
	}

	@Override
	public default long resolveComplexMemberMaximumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> nestedMembers
	)
	{
		return BinaryPersistence.binaryArrayMaximumLength();
	}



	public final class Implementation implements BinaryFieldLengthResolver
	{
		// empty default implementation. Something is missing in the new default method concept
	}

}
