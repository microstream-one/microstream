package one.microstream.persistence.types;

/*-
 * #%L
 * microstream-persistence
 * %%
 * Copyright (C) 2019 - 2022 MicroStream Software
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

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
