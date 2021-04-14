package one.microstream.persistence.types;



public interface PersistenceTypeDefinitionMemberFieldGeneric
extends PersistenceTypeDefinitionMemberField, PersistenceTypeDescriptionMemberFieldGeneric
{
	public default PersistenceTypeDefinitionMemberFieldGeneric copyForName(final String name)
	{
		return this.copyForName(this.qualifier(), name);
	}
	
	public PersistenceTypeDefinitionMemberFieldGeneric copyForName(String qualifier, String name);
}
