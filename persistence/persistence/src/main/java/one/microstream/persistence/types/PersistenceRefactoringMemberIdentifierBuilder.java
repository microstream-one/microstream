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

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;
import one.microstream.reflect.XReflect;

public interface PersistenceRefactoringMemberIdentifierBuilder
{
	public String buildMemberIdentifier(PersistenceTypeDefinition typeDef, PersistenceTypeDescriptionMember member);
	
	
	public static XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> createDefaultRefactoringLegacyMemberIdentifierBuilders()
	{
		return HashEnum.New(
			PersistenceRefactoringMemberIdentifierBuilder::toTypeIdIdentifier      ,
			PersistenceRefactoringMemberIdentifierBuilder::toGlobalNameIdentifier  ,
			PersistenceRefactoringMemberIdentifierBuilder::toTypeInternalIdentifier
		);
	}
	
	public static XEnum<? extends PersistenceRefactoringMemberIdentifierBuilder> createDefaultRefactoringCurrentMemberIdentifierBuilders()
	{
		return HashEnum.New(
			PersistenceRefactoringMemberIdentifierBuilder::toTypeIdIdentifier           ,
			PersistenceRefactoringMemberIdentifierBuilder::toGlobalNameIdentifier       ,
			PersistenceRefactoringMemberIdentifierBuilder::toTypeInternalIdentifier     ,
			PersistenceRefactoringMemberIdentifierBuilder::toUniqueUnqualifiedIdentifier
		);
	}
	
	public static char memberDelimiter()
	{
		return XReflect.fieldIdentifierDelimiter();
	}
	
	public static String toTypeIdIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return typeDefinition.toTypeIdentifier() + memberDelimiter() + toTypeInternalIdentifier(member);
	}
	
	public static String toGlobalNameIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return typeDefinition.typeName() + memberDelimiter() + toTypeInternalIdentifier(member);
	}
	
	public static String toTypeInternalIdentifier(
		final PersistenceTypeDefinition     typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		return toTypeInternalIdentifier(member);
	}
	
	public static String toTypeInternalIdentifier(final PersistenceTypeDescriptionMember member)
	{
		return member.identifier();
	}
	
	public static String toUniqueUnqualifiedIdentifier(
		final PersistenceTypeDefinition        typeDefinition,
		final PersistenceTypeDescriptionMember member
	)
	{
		final String memberSimpleName = member.name();
		
		for(final PersistenceTypeDescriptionMember m : typeDefinition.allMembers())
		{
			if(m == member)
			{
				continue;
			}
			
			// if the simple name is not unique, it cannot be used as a mapping target
			if(m.name().equals(memberSimpleName))
			{
				return null;
			}
		}
		
		return memberDelimiter() + memberSimpleName;
	}
	
}
