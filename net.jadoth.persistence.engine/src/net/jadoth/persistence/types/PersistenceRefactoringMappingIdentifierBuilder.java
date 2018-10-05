package net.jadoth.persistence.types;

@FunctionalInterface
public
interface PersistenceRefactoringMappingIdentifierBuilder
{
	public String buildTypeIdentifier(PersistenceTypeDefinition<?> type);
	
	public default String buildMemberIdentifier(
		final PersistenceTypeDefinition<?>     type  ,
		final PersistenceTypeDescriptionMember member
	)
	{
		return this.buildTypeIdentifier(type) + member.uniqueName();
	}
}