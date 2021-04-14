package one.microstream.persistence.binary.types;

import one.microstream.persistence.types.PersistenceTypeDefinition;
import one.microstream.persistence.types.PersistenceTypeDescriptionMember;
import one.microstream.persistence.types.PersistenceTypeHandler;

@FunctionalInterface
public interface BinaryValueTranslatorKeyBuilder
{
	public String buildTranslatorLookupKey(
		PersistenceTypeDefinition         sourceLegacyType ,
		PersistenceTypeDescriptionMember  sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDescriptionMember  targetMember
	);
}
