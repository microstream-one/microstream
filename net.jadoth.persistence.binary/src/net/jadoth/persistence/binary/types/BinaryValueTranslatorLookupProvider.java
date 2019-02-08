package net.jadoth.persistence.binary.types;

import net.jadoth.typing.TypeMappingLookup;

public interface BinaryValueTranslatorLookupProvider
{
	public TypeMappingLookup<BinaryValueSetter> mapping(boolean switchByteOrder);
}
