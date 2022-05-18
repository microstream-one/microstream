package one.microstream.persistence.binary.types;

/*-
 * #%L
 * microstream-persistence-binary
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
