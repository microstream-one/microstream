package net.jadoth.persistence.types;

import net.jadoth.collections.HashEnum;
import net.jadoth.collections.types.XEnum;

public interface PersistenceRefactoringTypeIdentifierBuilder
{
	public String buildTypeIdentifier(PersistenceTypeDescription typeDescription);
	
	
	
	public static XEnum<? extends PersistenceRefactoringTypeIdentifierBuilder> createDefaultRefactoringLegacyTypeIdentifierBuilders()
	{
		/* Identifier builders in descending order of priority:
		 * - [TypeId]:[TypeName]
		 * - [TypeName]
		 * Note that the first one is the only one that is unambiguous for all cases.
		 */
		return HashEnum.New(
			PersistenceTypeDescription::buildTypeIdentifier,
			PersistenceTypeDescription::typeName
		);
	}
	
}
