package one.microstream.persistence.types;

public interface PersistenceTypeDefinitionMember extends PersistenceTypeDescriptionMember
{
	/**
	 * The runtime type used by this description member, if possible. Otherwise <code>null</code>.
	 * 
	 * @return
	 */
	public Class<?> type();
	
	public default String runtimeQualifier()
	{
		return null;
	}

}
