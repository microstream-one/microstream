package one.microstream.persistence.types;

import java.lang.reflect.Field;

public interface PersistenceTypeDefinitionMemberField
extends PersistenceTypeDefinitionMember, PersistenceTypeDescriptionMemberField
{
	// (10.07.2019 TM)FIXME: MS-156: fill as needed
		
	public default Field field()
	{
		/*
		 * This is actually technically superfluous and just a mere usability helper for
		 * developers who don't want to distinct between field members and generic field members.
		 */
		return null;
	}
}
