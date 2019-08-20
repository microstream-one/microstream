package one.microstream.persistence.types;

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
