package one.microstream.persistence.binary.types;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.memory.XMemory;
import one.microstream.persistence.types.PersistenceFieldLengthResolver;
import one.microstream.persistence.types.PersistenceTypeDescriptionMemberFieldGeneric;

public interface BinaryFieldLengthResolver extends PersistenceFieldLengthResolver
{
	@Override
	public default long resolveMinimumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return XMemory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long resolveMaximumLengthFromPrimitiveType(final Class<?> primitiveType)
	{
		// binary length is equal to memory byte size
		return XMemory.byteSizePrimitive(primitiveType);
	}

	@Override
	public default long variableLengthTypeMinimumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return Binary.binaryListMinimumLength();
	}

	@Override
	public default long variableLengthTypeMaximumLength(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		return Binary.binaryListMaximumLength();
	}

	@Override
	public default long resolveComplexMemberMinimumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	)
	{
		return Binary.binaryListMinimumLength();
	}

	@Override
	public default long resolveComplexMemberMaximumLength(
		final String                                                                  memberName   ,
		final String                                                                  typeName     ,
		final XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	)
	{
		return Binary.binaryListMaximumLength();
	}



	public final class Default implements BinaryFieldLengthResolver
	{
		// empty default implementation. Something is missing in the new default method concept
	}

}
