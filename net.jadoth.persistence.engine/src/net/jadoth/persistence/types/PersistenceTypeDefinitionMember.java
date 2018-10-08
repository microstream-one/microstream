package net.jadoth.persistence.types;


public interface PersistenceTypeDefinitionMember extends PersistenceTypeDescriptionMember
{
	/**
	 * The runtime type used by this description member, if possible. Otherwise <code>null</code>.
	 * 
	 * @return
	 */
	public Class<?> type();

}
