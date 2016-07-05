package net.jadoth.persistence.types;

import java.lang.reflect.Field;

import net.jadoth.collections.types.XGettingSequence;
import net.jadoth.reflect.JadothReflect;
import net.jadoth.swizzling.types.Swizzle;


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

		if(JadothReflect.isPrimitiveTypeName(typeName))
		{
			try
			{
				return this.resolveMinimumLengthFromPrimitiveType(JadothReflect.classForName(typeName));
			}
			catch(LinkageError | ClassNotFoundException e)
			{
				/* can really never happen as primitive type name is checked before and
				 * gets resolved by hardcoded switch. The only way this exception can ever occur
				 * is if the two called methods are ruined.
				 */
				throw new Error("impossible error occured while resolving primitive type " + typeName, e);
			}
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

		if(JadothReflect.isPrimitiveTypeName(typeName))
		{
			try
			{
				return this.resolveMaximumLengthFromPrimitiveType(JadothReflect.classForName(typeName));
			}
			catch(LinkageError | ClassNotFoundException e)
			{
				/* can really never happen as primitive type name is checked before and
				 * gets resolved by hardcoded switch. The only way this exception can ever occur
				 * is if the two called methods are ruined.
				 */
				throw new Error("impossible error occured while resolving primitive type " + typeName, e);
			}
		}

		// everything else (neither variable length nor primitive) must be a reference value
		return this.referenceMaximumLength();
	}

	public default long resolveMinimumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()  ? this.resolveMinimumLengthFromPrimitiveType(type)  : this.referenceMinimumLength();
	}

	public default long resolveMaximumLengthFromType(final Class<?> type)
	{
		return type.isPrimitive()  ? this.resolveMaximumLengthFromPrimitiveType(type)  : this.referenceMaximumLength();
	}

	public default long referenceMinimumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Swizzle.objectIdType());
	}

	public default long referenceMaximumLength()
	{
		return this.resolveMinimumLengthFromPrimitiveType(Swizzle.objectIdType());
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
		XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> nestedMembers
	);

	public long resolveComplexMemberMaximumLength(
		String                                                                  memberName   ,
		String                                                                  typeName     ,
		XGettingSequence<? extends PersistenceTypeDescriptionMemberPseudoField> nestedMembers
	);

}
