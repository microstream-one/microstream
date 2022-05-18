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

import one.microstream.collections.types.XGettingSequence;
import one.microstream.collections.types.XImmutableSequence;

public interface PersistenceTypeDescriptionMemberFieldGenericComplex
extends PersistenceTypeDescriptionMemberFieldGenericVariableLength
{
	public XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members();

	
	
	@Override
	public default boolean equalsDescription(final PersistenceTypeDescriptionMember other)
	{
		// does NOT call #equalsStructure to avoid redundant member iteration
		return PersistenceTypeDescriptionMember.equalTypeAndNameAndQualifier(this, other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalDescriptions(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	@Override
	public default boolean equalsStructure(final PersistenceTypeDescriptionMember other)
	{
		return PersistenceTypeDescriptionMemberFieldGenericVariableLength.super.equalsStructure(other)
			&& other instanceof PersistenceTypeDescriptionMemberFieldGenericComplex
			&& PersistenceTypeDescriptionMember.equalStructures(
				this.members(),
				((PersistenceTypeDescriptionMemberFieldGenericComplex)other).members()
			)
		;
	}
	
	public static boolean equalDescription(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalDescription(m1, m2);
	}
	
	public static boolean equalStructure(
		final PersistenceTypeDescriptionMemberFieldGenericComplex m1,
		final PersistenceTypeDescriptionMemberFieldGenericComplex m2
	)
	{
		return PersistenceTypeDescriptionMember.equalStructure(m1, m2);
	}
	
	@Override
	public default PersistenceTypeDefinitionMemberFieldGenericComplex createDefinitionMember(
		final PersistenceTypeDefinitionMemberCreator creator
	)
	{
		return creator.createDefinitionMember(this);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return New(null, name, members, persistentMinimumLength, persistentMaximumLength);
	}
	
	public static PersistenceTypeDescriptionMemberFieldGenericComplex New(
		final String                                                         qualifier              ,
		final String                                                         name                   ,
		final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
		final long                                                           persistentMinimumLength,
		final long                                                           persistentMaximumLength
	)
	{
		return new PersistenceTypeDescriptionMemberFieldGenericComplex.Default(
			 mayNull(qualifier)              ,
			 notNull(name)                   ,
			 notNull(members)                ,
			positive(persistentMinimumLength),
			positive(persistentMaximumLength)
		);
	}

	public class Default
	extends PersistenceTypeDescriptionMemberFieldGenericVariableLength.Default
	implements PersistenceTypeDescriptionMemberFieldGenericComplex
	{
		///////////////////////////////////////////////////////////////////////////
		// instance fields //
		////////////////////

		final XImmutableSequence<PersistenceTypeDescriptionMemberFieldGeneric> members;



		///////////////////////////////////////////////////////////////////////////
		// constructors //
		/////////////////

		Default(
			final String                                                         qualifier              ,
			final String                                                         name                   ,
			final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members                ,
			final long                                                           persistentMinimumLength,
			final long                                                           persistentMaximumLength
		)
		{
			super(
				PersistenceTypeDictionary.Symbols.TYPE_COMPLEX,
				qualifier,
				name,
				PersistenceTypeDescriptionMember.determineHasReferences(members),
				persistentMinimumLength,
				persistentMaximumLength
			);
			this.members = members.immure();
		}



		///////////////////////////////////////////////////////////////////////////
		// methods //
		////////////

		@Override
		public final XGettingSequence<PersistenceTypeDescriptionMemberFieldGeneric> members()
		{
			return this.members;
		}

		@Override
		public void assembleTypeDescription(final PersistenceTypeDescriptionMemberAppender assembler)
		{
			assembler.appendTypeMemberDescription(this);
		}

	}

}
