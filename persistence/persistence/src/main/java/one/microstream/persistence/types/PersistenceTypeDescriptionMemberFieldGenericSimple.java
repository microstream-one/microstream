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

import static one.microstream.X.mayNull;
import static one.microstream.X.notNull;
import static one.microstream.math.XMath.positive;

public interface PersistenceTypeDescriptionMemberFieldGenericSimple
extends PersistenceTypeDescriptionMemberFieldGeneric
{
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericSimple createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericSimple m1,
		final PersistenceTypeDescriptionMemberFieldGenericSimple m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericSimple m1,
		final PersistenceTypeDescriptionMemberFieldGenericSimple m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	

	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return New(typeName, null, name, isReference, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericSimple.Default New(
		final String  typeName               ,
		final String  qualifier              ,
		final String  name                   ,
		final boolean isReference            ,
		final long    persistentMinimumLength,
		final long    persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericSimple.Default(
			 notNull(typeName)               ,
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			         isReference             ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}
	
	public final class Default
	extends PersistenceTypeDescriptionMemberFieldGeneric.Abstract
	implements PersistenceTypeDescriptionMemberFieldGenericSimple
	{
		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String  typeName           ,
			final String  qualifier          ,
			final String  name               ,
			final boolean isReference        ,
			final long    persistentMinLength,
			final long    persistentMaxLength
		)
		{
			super(
				typeName,
				qualifier,
				name,
				isReference,
				!isReference,
				isReference,
				persistentMinLength,
				persistentMaxLength
			);
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
