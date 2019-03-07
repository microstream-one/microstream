package one.microstream.persistence.types;

import java.lang.reflect.Field;

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
	
	public default Field field()
	{
		/*
		 * This is actually technically superfluous and just a mere usability helper for
		 * developers who don't want to distinct between field members and pseudo field members.
		 */
		return null;
	}

}
