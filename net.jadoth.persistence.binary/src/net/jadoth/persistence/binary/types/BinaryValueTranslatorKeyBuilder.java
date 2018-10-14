package net.jadoth.persistence.binary.types;

import net.jadoth.persistence.types.PersistenceTypeDefinition;
import net.jadoth.persistence.types.PersistenceTypeDescriptionMember;
import net.jadoth.persistence.types.PersistenceTypeHandler;

public interface BinaryValueTranslatorKeyBuilder
{
	public String buildTranslatorLookupKey(
		PersistenceTypeDefinition      sourceLegacyType ,
		PersistenceTypeDescriptionMember  sourceMember     ,
		PersistenceTypeHandler<Binary, ?> targetCurrentType,
		PersistenceTypeDescriptionMember  targetMember
	);
}
