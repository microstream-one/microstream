package one.microstream.persistence.types;



public interface PersistenceTypeDefinitionMemberPseudoField
extends PersistenceTypeDefinitionMember, PersistenceTypeDescriptionMemberFieldGeneric
{
	public default PersistenceTypeDefinitionMemberPseudoField copyForName(final String name)
	{
		return this.copyForName(null, name);
	}
	
	public PersistenceTypeDefinitionMemberPseudoField copyForName(String qualifier, String name);
}
