package net.jadoth.persistence.types;

public interface PersistenceRefactoringMemberIdentifierBuilder
{
	public String buildMemberIdentifier(PersistenceTypeDefinition<?> typeDef, PersistenceTypeDescriptionMember member);
}
