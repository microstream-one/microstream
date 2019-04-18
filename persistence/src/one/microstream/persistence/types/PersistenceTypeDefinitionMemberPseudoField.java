package one.microstream.persistence.types;



public interface PersistenceTypeDefinitionMemberPseudoField
extends PersistenceTypeDefinitionMember, PersistenceTypeDescriptionMemberPseudoField
{
	// only typing interface so far
	
	public PersistenceTypeDefinitionMemberPseudoField copyForName(String name);
}
