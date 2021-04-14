package one.microstream.persistence.types;

import one.microstream.collections.HashEnum;
import one.microstream.collections.types.XEnum;

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
