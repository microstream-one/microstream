package one.microstream.persistence.types;

import java.lang.reflect.Field;

import one.microstream.collections.types.XGettingSequence;
import one.microstream.reflect.XReflect;


public interface PersistenceFieldLengthResolver
{
	public default long resolveMinimumLengthFromField(final Field t)
	{
		return this.resolveMinimumLengthFromType(t.getType());
	}

	public default long resolveMaximumLengthFromField(final Field t)
	{
		return this.resolveMaximumLengthFromType(t.getType());
	}

	public default long resolveMinimumLengthFromDictionary(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		if(PersistenceTypeDictionary.isVariableLength(typeName))
		{
			return this.variableLengthTypeMinimumLength(declaringTypeName, memberName, typeName);
		}

		if(XReflect.isPrimitiveTypeName(typeName))
		{
			return this.resolveMinimumLengthFromPrimitiveType(
				XReflect.tryResolvePrimitiveType(typeName)
			);
		}

		// everything else (neither variable length nor primitive) must be a reference value
		return this.referenceMinimumLength();
	}

	public default long resolveMaximumLengthFromDictionary(
		final String declaringTypeName,
		final String memberName       ,
		final String typeName
	)
	{
		if(PersistenceTypeDictionary.isVariableLength(typeName))
		{
			return this.variableLengthTypeMaximumLength(declaringTypeName, memberName, typeName);
		}

		if(XReflect.isPrimitiveTypeName(typeName))
		{
			return this.resolveMaximumLengthFromPrimitiveType(
				XReflect.tryResolvePrimitiveType(typeName)
			);
		}

		// everything else (neither variable length nor primitive) must be a reference value
		return this.referenceMaximumLength();
	}

	public default long resolveMinimumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()
			? this.resolveMinimumLengthFromPrimitiveType(type)
			: this.referenceMinimumLength()
		;
	}

	public default long resolveMaximumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()
			? this.resolveMaximumLengthFromPrimitiveType(type)
			: this.referenceMaximumLength()
		;
	}

	public default long referenceMinimumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Persistence.objectIdType());
	}

	public default long referenceMaximumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Persistence.objectIdType());
	}

	public long variableLengthTypeMinimumLength(
		String declaringTypeName,
		String memberName       ,
		String typeName
	);

	public long variableLengthTypeMaximumLength(
		String declaringTypeName,
		String memberName       ,
		String typeName
	);

	public long resolveMinimumLengthFromPrimitiveType(Class<?> primitiveType);

	public long resolveMaximumLengthFromPrimitiveType(Class<?> primitiveType);

	public long resolveComplexMemberMinimumLength(
		String                                                                  memberName   ,
		String                                                                  typeName     ,
		XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	);

	public long resolveComplexMemberMaximumLength(
		String                                                                  memberName   ,
		String                                                                  typeName     ,
		XGettingSequence<? extends PersistenceTypeDescriptionMemberFieldGeneric> nestedMembers
	);

}
